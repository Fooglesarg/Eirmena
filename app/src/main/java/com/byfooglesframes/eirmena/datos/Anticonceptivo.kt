package com.byfooglesframes.eirmena.datos

import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class Metodo(val nombre: String, val suprimeOvulacion: Boolean) {
    NINGUNO("Ninguno", false),
    PRESERVATIVO("Preservativo", false),
    PASTILLA("Pastilla", true),
    PARCHE("Parche", true),
    ANILLO("Anillo vaginal", true),
    INYECCION("Inyección", true),
    IMPLANTE("Implante", true),
    DIU_HORMONAL("DIU hormonal", false),
    DIU_COBRE("DIU de cobre", false),
    OTRO("Otro", false);

    /** Métodos con una fecha de colocación y de vencimiento. */
    val tieneVencimiento: Boolean get() = this == IMPLANTE || this == DIU_HORMONAL || this == DIU_COBRE
}

/** Cómo viene el blíster de pastillas. */
enum class Esquema(val nombre: String, val activas: Int, val descanso: Int, val placebo: Boolean) {
    E21_7("21 pastillas y 7 días sin tomar", 21, 7, false),
    E21_7P("21 activas y 7 de placebo", 21, 7, true),
    E24_4("24 activas y 4 de placebo", 24, 4, true),
    E28("28 activas, sin descanso", 28, 0, false);

    val total: Int get() = activas + descanso
}

enum class Frecuencia(val nombre: String, val dias: Int) {
    MENSUAL("Mensual", 30),
    TRIMESTRAL("Trimestral", 90),
}

data class Anticonceptivo(
    val metodo: Metodo = Metodo.NINGUNO,
    /**
     * Pastilla, parche y anillo: primer día de un blíster o ciclo (sirve cualquiera, se cuenta en vueltas).
     * Inyección: la última aplicación. DIU e implante: la colocación.
     */
    val inicio: LocalDate? = null,
    val hora: LocalTime = LocalTime.of(21, 0),
    val esquema: Esquema = Esquema.E21_7,
    val frecuencia: Frecuencia = Frecuencia.MENSUAL,
    val vence: LocalDate? = null,
)

/** Qué le toca hoy a la persona según su método. */
data class EstadoMetodo(
    val titulo: String,
    val detalle: String?,
    /** Hoy hay que tomar una pastilla (activa o placebo). */
    val tomaPastilla: Boolean = false,
    /** Algo que hacer hoy: cambiar parche, sacar anillo, darse la inyección… */
    val tareaHoy: String? = null,
    val atrasado: Boolean = false,
)

object Metodos {
    fun estado(a: Anticonceptivo, fecha: LocalDate): EstadoMetodo? {
        val inicio = a.inicio ?: return null
        return when (a.metodo) {
            Metodo.PASTILLA -> pastilla(a.esquema, inicio, fecha)
            Metodo.PARCHE -> semanal(inicio, fecha, "Parche", poner = "Hoy toca poner un parche nuevo", sacar = "Hoy toca sacar el parche", cambio = true)
            Metodo.ANILLO -> semanal(inicio, fecha, "Anillo", poner = "Hoy toca colocar un anillo nuevo", sacar = "Hoy toca retirar el anillo", cambio = false)
            Metodo.INYECCION -> inyeccion(a.frecuencia, inicio, fecha)
            Metodo.IMPLANTE, Metodo.DIU_HORMONAL, Metodo.DIU_COBRE -> vencimiento(a, fecha)
            else -> null
        }
    }

    /** Día del blíster o del ciclo (1..total), o null si la fecha es anterior al inicio. */
    fun diaDelCiclo(inicio: LocalDate, fecha: LocalDate, total: Int): Int? {
        val d = ChronoUnit.DAYS.between(inicio, fecha)
        return if (d < 0) null else (d % total).toInt() + 1
    }

    private fun pastilla(e: Esquema, inicio: LocalDate, fecha: LocalDate): EstadoMetodo? {
        val dia = diaDelCiclo(inicio, fecha, e.total) ?: return null
        return when {
            dia <= e.activas -> EstadoMetodo(
                "Pastilla $dia de ${e.activas}",
                if (e.descanso == 0) null else "Quedan ${e.activas - dia} activas en este blíster.",
                tomaPastilla = true,
            )
            e.placebo -> EstadoMetodo(
                "Placebo ${dia - e.activas} de ${e.descanso}",
                "Seguí tomando las de placebo: mantienen el hábito.",
                tomaPastilla = true,
            )
            else -> EstadoMetodo(
                "Descanso: día ${dia - e.activas} de ${e.descanso}",
                if (dia == e.total) "Mañana empezás un blíster nuevo." else "Estos días no se toma pastilla.",
            )
        }
    }

    /** Parche (se cambia cada semana) y anillo (queda 3 semanas): ciclo de 28 días con una semana libre. */
    private fun semanal(inicio: LocalDate, fecha: LocalDate, nombre: String, poner: String, sacar: String, cambio: Boolean): EstadoMetodo? {
        val dia = diaDelCiclo(inicio, fecha, 28) ?: return null
        val semana = (dia - 1) / 7 + 1
        val tarea = when {
            dia == 1 -> poner
            cambio && (dia == 8 || dia == 15) -> "Hoy toca cambiar el parche"
            dia == 22 -> sacar
            else -> null
        }
        return if (semana <= 3) {
            EstadoMetodo(
                if (cambio) "$nombre $semana de 3" else "$nombre: semana $semana de 3",
                "Día $dia del ciclo de 28.", tareaHoy = tarea,
            )
        } else {
            EstadoMetodo(
                "Semana libre: día ${dia - 21} de 7",
                if (dia == 28) "Mañana empieza un ciclo nuevo." else "Esta semana va sin ${nombre.lowercase()}.",
                tareaHoy = tarea,
            )
        }
    }

    private fun inyeccion(f: Frecuencia, ultima: LocalDate, fecha: LocalDate): EstadoMetodo {
        val proxima = ultima.plusDays(f.dias.toLong())
        val faltan = ChronoUnit.DAYS.between(fecha, proxima).toInt()
        return when {
            faltan > 0 -> EstadoMetodo("Próxima inyección en ${dias(faltan)}", "Inyección ${f.nombre.lowercase()}.",
                tareaHoy = if (faltan == 2) "En 2 días toca la inyección" else null)
            faltan == 0 -> EstadoMetodo("Hoy toca la inyección", null, tareaHoy = "Hoy toca la inyección")
            else -> EstadoMetodo("La inyección está atrasada ${dias(-faltan)}",
                "Mientras tanto usá preservativo y consultá cómo seguir.", atrasado = true)
        }
    }

    private fun vencimiento(a: Anticonceptivo, fecha: LocalDate): EstadoMetodo {
        val vence = a.vence ?: return EstadoMetodo("${a.metodo.nombre} colocado", "Podés agregar la fecha de vencimiento en Ajustes.")
        val faltan = ChronoUnit.DAYS.between(fecha, vence).toInt()
        return when {
            faltan > 60 -> EstadoMetodo("${a.metodo.nombre} colocado", "Vence en ${(faltan / 30.4).toInt()} meses.")
            faltan >= 0 -> EstadoMetodo("Tu ${a.metodo.nombre.lowercase()} vence pronto", "Vence en ${dias(faltan)}: pedí un turno para el recambio.",
                tareaHoy = if (faltan == 30) "Tu método vence en 30 días" else null)
            else -> EstadoMetodo("Tu ${a.metodo.nombre.lowercase()} venció", "Puede dejar de proteger: consultá para el recambio.", atrasado = true)
        }
    }

    private fun dias(n: Int) = if (n == 1) "1 día" else "$n días"
}
