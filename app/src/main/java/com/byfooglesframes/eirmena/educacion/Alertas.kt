package com.byfooglesframes.eirmena.educacion

import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Periodo
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Sintoma
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Alerta(
    val id: String,
    val titulo: String,
    /** Qué se vio en los datos. */
    val texto: String,
    val queHacer: String,
)

/**
 * Patrones en los datos que conviene consultar con un profesional. Los límites siguen la definición
 * de sangrado uterino normal de FIGO (ciclos de 24 a 38 días, variación de hasta 7–9 días, períodos de
 * hasta 8 días). No diagnostica: solo señala algo para llevar a la consulta.
 */
object Alertas {
    /** Una alerta que la persona marcó como vista no vuelve a aparecer en Hoy durante este tiempo. */
    const val DIAS_OCULTA = 60L

    fun calcular(datos: Datos, hoy: LocalDate = LocalDate.now()): List<Alerta> {
        val etapa = datos.ajustes.etapa
        if (etapa == EtapaVida.EMBARAZO) return emptyList()
        val periodos = Ciclos.periodos(datos.dias)
        if (periodos.isEmpty()) return emptyList()
        // En la perimenopausia los ciclos cambian solos: no se los juzga. En los primeros ciclos, lo normal es más amplio (21 a 45 días).
        val peri = etapa == EtapaVida.PERIMENOPAUSIA
        val primeros = etapa == EtapaVida.PRIMEROS_CICLOS
        val minimo = if (primeros) 21 else 24
        val maximo = if (primeros) 45 else 38
        val terminoEmbarazo = datos.ajustes.embarazo?.fin?.let { ChronoUnit.DAYS.between(it, hoy) < 270 } == true
        val ciclos = Ciclos.ciclos(periodos).mapNotNull { it.duracion }.filter { it <= Datos.CICLO_MAX }
        val ultimos3 = ciclos.takeLast(3)
        val metodo = datos.ajustes.anticonceptivo.metodo
        val hormonal = metodo.suprimeOvulacion || metodo == Metodo.DIU_HORMONAL
        val terminados = periodos.filter { ChronoUnit.DAYS.between(it.fin, hoy) > 2 }.takeLast(3)
        val lista = mutableListOf<Alerta>()

        val cortos = ultimos3.filter { it < minimo }
        if (!hormonal && !peri && cortos.size >= 2) lista += Alerta(
            "ciclos-cortos", "Ciclos cortos",
            "Varios de tus últimos ciclos duraron menos de $minimo días (${cortos.joinToString(" y ")} días).",
            "Mencionalo en tu próximo control ginecológico. Puede tener que ver con la tiroides, con la etapa de la vida " +
                "o con otras causas que se pueden estudiar.",
        )

        val largos = ultimos3.filter { it > maximo }
        if (!hormonal && !peri && largos.size >= 2) lista += Alerta(
            "ciclos-largos", "Ciclos largos",
            "Varios de tus últimos ciclos duraron más de $maximo días (${largos.joinToString(" y ")} días).",
            "Consultalo. Una causa común es el síndrome de ovario poliquístico (SOP), pero también el estrés, cambios de " +
                "peso, la tiroides o la prolactina.",
        )

        val recientes = ciclos.takeLast(6)
        if (!hormonal && !peri && !primeros && recientes.size >= 4 && recientes.max() - recientes.min() > 9) lista += Alerta(
            "irregulares", "Ciclos irregulares",
            "Entre tu ciclo más corto y el más largo hay ${recientes.max() - recientes.min()} días de diferencia. " +
                "Se considera irregular cuando la diferencia pasa de 7 a 9 días.",
            "Vale la pena consultarlo, sobre todo si además tenés acné, mucho vello o te cuesta quedar embarazada.",
        )

        val ultimo = periodos.last()
        val sinPeriodo = ChronoUnit.DAYS.between(ultimo.inicio, hoy)
        val sinPeriodoEsperable = metodo == Metodo.IMPLANTE || metodo == Metodo.INYECCION || metodo == Metodo.DIU_HORMONAL
        if (sinPeriodo > 90 && !sinPeriodoEsperable && !peri && !terminoEmbarazo) lista += Alerta(
            "sin-periodo", "Más de 3 meses sin período",
            "Tu último período anotado empezó hace $sinPeriodo días.",
            "Si no fue que dejaste de anotar, hacete un test de embarazo y consultá: la falta de período por 3 meses " +
                "o más tiene causas que conviene estudiar.",
        )

        // Sangrado después de 12 meses o más sin período: siempre hay que consultarlo.
        val posmenopausia = periodos.zipWithNext().lastOrNull { (antes, despues) ->
            ChronoUnit.DAYS.between(antes.fin, despues.inicio) >= 365 && ChronoUnit.DAYS.between(despues.inicio, hoy) <= 180
        }
        if (peri && posmenopausia != null) lista += Alerta(
            "posmenopausia", "Sangrado después de la menopausia",
            "Anotaste un sangrado después de más de un año sin período.",
            "Pedí un turno pronto. Muchas veces la causa es leve, pero el sangrado después de la menopausia siempre se tiene que revisar.",
        )

        val largosP = terminados.filter { it.dias > 8 }
        if (largosP.size >= 2) lista += Alerta(
            "periodo-largo", "Períodos largos",
            "Varios de tus últimos períodos duraron más de 8 días.",
            "Consultalo, sobre todo si además el sangrado es abundante: puede causar anemia.",
        )

        val abundantes = terminados.filter { p -> diasCon(datos, p) { it == Flujo.ABUNDANTE } >= 3 }
        if (abundantes.size >= 2) lista += Alerta(
            "abundante", "Sangrado abundante",
            "En varios períodos anotaste 3 días o más de sangrado abundante.",
            "Si cambiás la toalla o el tampón cada 1 o 2 horas, tenés coágulos grandes o sentís mucho cansancio " +
                "o mareos, consultá: el sangrado abundante tiene tratamiento y puede causar anemia.",
        )

        val entre = sangradoEntrePeriodos(datos, periodos, hoy)
        if (entre.size >= 3 && entre.map { it.monthValue }.distinct().size >= 2) lista += Alerta(
            "entre-periodos", "Sangrado entre períodos",
            "En los últimos 3 meses anotaste ${entre.size} días de sangrado o manchado fuera del período.",
            if (hormonal) "En los primeros meses de un método hormonal es común. Si sigue después de 3 meses, o aparece " +
                "después de tener relaciones, consultalo."
            else "Consultalo, sobre todo si aparece después de tener relaciones. Aprovechá para hacer tu PAP o test de VPH si te toca.",
        )

        val conColicos = terminados.count { p -> diasConSintoma(datos, p, Sintoma.COLICOS) >= 3 }
        if (terminados.size >= 3 && conColicos >= 3) lista += Alerta(
            "dolor", "Cólicos en cada período",
            "En tus últimos 3 períodos anotaste cólicos durante 3 días o más.",
            "El dolor menstrual es común, pero no debería impedirte hacer tu vida. Si no se calma con analgésicos comunes, " +
                "o duele al tener relaciones o al ir al baño, consultá: puede ser endometriosis y tiene tratamiento.",
        )

        if (premenstrualRecurrente(datos, periodos)) lista += Alerta(
            "spm", "Tu ánimo cambia antes del período",
            "En tus últimos 3 ciclos anotaste tristeza, irritabilidad, ansiedad o cambios de humor sobre todo en la semana " +
                "previa al período.",
            "Es el síndrome premenstrual (SPM) y es muy común. Si te afecta mucho en el trabajo, el estudio o tus vínculos, " +
                "contalo en la consulta: puede ser un trastorno disfórico premenstrual (TDPM) y se puede tratar.",
        )
        return lista
    }

    /** Las alertas que todavía no se marcaron como vistas (o se vieron hace mucho). */
    fun visibles(alertas: List<Alerta>, ocultas: Map<String, LocalDate>, hoy: LocalDate = LocalDate.now()): List<Alerta> =
        alertas.filter { a -> ocultas[a.id]?.let { ChronoUnit.DAYS.between(it, hoy) >= DIAS_OCULTA } ?: true }

    private fun diasCon(datos: Datos, p: Periodo, cond: (Flujo) -> Boolean): Int =
        dias(p.inicio, p.fin).count { f -> datos.dias[f]?.flujo?.let(cond) == true }

    private fun diasConSintoma(datos: Datos, p: Periodo, s: Sintoma): Int =
        dias(p.inicio.minusDays(1), p.fin).count { f -> datos.dias[f]?.sintomas?.contains(s) == true }

    private fun sangradoEntrePeriodos(datos: Datos, periodos: List<Periodo>, hoy: LocalDate): List<LocalDate> =
        datos.dias.values
            .filter { it.flujo != null && ChronoUnit.DAYS.between(it.fecha, hoy) in 0..90 }
            .map { it.fecha }
            // Los días pegados a un período (antes o después) no cuentan: suelen ser su principio o su final.
            .filter { f -> periodos.none { f >= it.inicio.minusDays(2) && f <= it.fin.plusDays(2) } }

    private val NEGATIVOS = setOf(Animo.TRISTEZA, Animo.IRRITABILIDAD, Animo.ANSIEDAD, Animo.CAMBIOS)

    private fun premenstrualRecurrente(datos: Datos, periodos: List<Periodo>): Boolean {
        if (periodos.size < 4) return false
        return periodos.takeLast(4).zipWithNext().all { (antes, despues) ->
            val semanaPrevia = dias(despues.inicio.minusDays(7), despues.inicio.minusDays(1))
            val mitadDelCiclo = dias(antes.fin.plusDays(1), antes.fin.plusDays(7))
            val negPrevia = semanaPrevia.count { f -> datos.dias[f]?.animo?.any { it in NEGATIVOS } == true }
            val negMitad = mitadDelCiclo.count { f -> datos.dias[f]?.animo?.any { it in NEGATIVOS } == true }
            negPrevia >= 2 && negMitad <= 1
        }
    }

    private fun dias(desde: LocalDate, hasta: LocalDate): List<LocalDate> =
        generateSequence(desde) { it.plusDays(1) }.takeWhile { it <= hasta }.toList()
}
