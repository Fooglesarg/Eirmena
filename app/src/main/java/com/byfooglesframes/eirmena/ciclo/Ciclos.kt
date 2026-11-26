package com.byfooglesframes.eirmena.ciclo

import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.RegistroDia
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class Periodo(val inicio: LocalDate, val fin: LocalDate) {
    val dias: Int get() = (ChronoUnit.DAYS.between(inicio, fin) + 1).toInt()
}

/** Un ciclo va del primer día de un período al día anterior al siguiente. [duracion] es null si está en curso. */
data class Ciclo(val periodo: Periodo, val duracion: Int?) {
    val inicio: LocalDate get() = periodo.inicio
}

enum class Fase(val nombre: String) {
    MENSTRUAL("Menstrual"),
    FOLICULAR("Folicular"),
    FERTIL("Ventana fértil"),
    LUTEA("Lútea"),
}

enum class Regularidad(val nombre: String) {
    POCOS_DATOS("Todavía pocos datos"),
    REGULAR("Regular"),
    VARIABLE("Algo variable"),
    IRREGULAR("Irregular"),
}

enum class Marca { PERIODO, FERTIL, OVULACION }

data class Prediccion(
    val ciclos: List<Ciclo>,
    /** Duración estimada del ciclo y del período, en días. */
    val duracionCiclo: Int,
    val duracionPeriodo: Int,
    /** ± días alrededor de la fecha estimada. */
    val rango: Int,
    val regularidad: Regularidad,
    /** Diferencia entre el ciclo más corto y el más largo de los usados. */
    val variacion: Int,
    val ciclosUsados: Int,
    val ultimoInicio: LocalDate?,
    val proximoInicio: LocalDate?,
    /** Ovulación de este ciclo detectada por temperatura o test (si la hay, la fecha del próximo período sale de ahí). */
    val ovulacion: OvulacionDetectada? = null,
)

data class Hoy(
    val diaDelCiclo: Int,
    val fase: Fase,
    /** Días de período si está en curso (1 = primer día), o null. */
    val diaDelPeriodo: Int?,
    val diasParaProximo: Int,
    /** Días después de la fecha estimada sin que llegue el período (0 si no hay retraso). */
    val retraso: Int,
    /** true si el retraso ya superó el rango habitual de la persona. */
    val retrasoFueraDeRango: Boolean,
)

object Ciclos {
    /** Días sin sangrado que se toleran dentro de un mismo período (p. ej. un día que no se anotó). */
    private const val HUECO_MAXIMO = 2
    /** Cantidad de ciclos recientes que pesan en la estimación. */
    private const val CICLOS_RECIENTES = 6
    /** Duración estimada de la fase lútea; la ovulación se estima 14 días antes del próximo período. */
    const val FASE_LUTEA = 14

    fun periodos(dias: Map<LocalDate, RegistroDia>): List<Periodo> {
        val fechas = dias.values.filter { it.flujo?.cuentaComoPeriodo == true }.map { it.fecha }.sorted()
        val resultado = mutableListOf<Periodo>()
        for (f in fechas) {
            val ultimo = resultado.lastOrNull()
            if (ultimo != null && ChronoUnit.DAYS.between(ultimo.fin, f) <= HUECO_MAXIMO + 1) {
                resultado[resultado.lastIndex] = ultimo.copy(fin = f)
            } else {
                resultado += Periodo(f, f)
            }
        }
        return resultado
    }

    fun ciclos(periodos: List<Periodo>): List<Ciclo> = periodos.mapIndexed { i, p ->
        val siguiente = periodos.getOrNull(i + 1)
        Ciclo(p, siguiente?.let { ChronoUnit.DAYS.between(p.inicio, it.inicio).toInt() })
    }

    fun predecir(datos: Datos, hoy: LocalDate = LocalDate.now()): Prediccion {
        val periodos = periodos(datos.dias)
        val ciclos = ciclos(periodos)
        val ajustes = datos.ajustes

        val validos = ciclos.mapNotNull { it.duracion }
            .filter { it in Datos.CICLO_MIN..Datos.CICLO_MAX }
            .takeLast(CICLOS_RECIENTES)

        // Promedio ponderado: el ciclo más reciente pesa más. Con menos de 3 ciclos propios,
        // se completa con la duración habitual que la persona indicó al empezar.
        val valores = mutableListOf<Pair<Double, Double>>() // (duración, peso)
        repeat((3 - validos.size).coerceAtLeast(0)) { valores += ajustes.cicloHabitual.toDouble() to 1.0 }
        validos.forEachIndexed { i, d -> valores += d.toDouble() to (i + 1).toDouble() }
        val pesoTotal = valores.sumOf { it.second }
        val media = valores.sumOf { it.first * it.second } / pesoTotal
        val desvio = sqrt(valores.sumOf { (it.first - media) * (it.first - media) * it.second } / pesoTotal)

        val variacion = if (validos.size >= 2) validos.max() - validos.min() else 0
        val regularidad = when {
            validos.size < 3 -> Regularidad.POCOS_DATOS
            variacion <= 7 -> Regularidad.REGULAR
            variacion <= 14 -> Regularidad.VARIABLE
            else -> Regularidad.IRREGULAR
        }
        val rango = if (validos.size < 3) 3 else desvio.roundToInt().coerceIn(1, 7)

        // El período en curso no cuenta para la duración promedio hasta que termine.
        val terminados = periodos.filter { ChronoUnit.DAYS.between(it.fin, hoy) > HUECO_MAXIMO }
            .map { it.dias }.filter { it in 1..15 }.takeLast(CICLOS_RECIENTES)
        val duracionPeriodo = if (terminados.isEmpty()) ajustes.periodoHabitual
            else terminados.average().roundToInt()

        val duracionCiclo = media.roundToInt()
        val ultimo = periodos.lastOrNull()?.inicio
        // Con la ovulación confirmada, el período llega unos 14 días después y con poco margen.
        val ovulacion = ultimo?.let { Fertilidad.detectar(datos, it.plusDays(6), hoy) }
        if (ovulacion != null) return Prediccion(
            ciclos = ciclos, duracionCiclo = duracionCiclo, duracionPeriodo = duracionPeriodo, rango = 2,
            regularidad = regularidad, variacion = variacion, ciclosUsados = validos.size, ultimoInicio = ultimo,
            proximoInicio = ovulacion.fecha.plusDays(FASE_LUTEA.toLong()), ovulacion = ovulacion,
        )
        return Prediccion(
            ciclos = ciclos,
            duracionCiclo = duracionCiclo,
            duracionPeriodo = duracionPeriodo,
            rango = rango,
            regularidad = regularidad,
            variacion = variacion,
            ciclosUsados = validos.size,
            ultimoInicio = ultimo,
            proximoInicio = ultimo?.plusDays(duracionCiclo.toLong()),
        )
    }

    fun hoy(datos: Datos, p: Prediccion, hoy: LocalDate = LocalDate.now()): Hoy? {
        val inicio = p.ultimoInicio ?: return null
        val proximo = p.proximoInicio ?: return null
        if (hoy < inicio) return null
        val diaDelCiclo = ChronoUnit.DAYS.between(inicio, hoy).toInt() + 1

        val periodo = periodos(datos.dias).last()
        val enCurso = ChronoUnit.DAYS.between(periodo.fin, hoy) in 0..1 // sangró hoy o ayer
        val diaDelPeriodo = if (enCurso) ChronoUnit.DAYS.between(periodo.inicio, hoy).toInt() + 1 else null

        val ovulacion = proximo.minusDays(FASE_LUTEA.toLong())
        val fase = when {
            diaDelPeriodo != null -> Fase.MENSTRUAL
            hoy in ovulacion.minusDays(5)..ovulacion.plusDays(1) -> Fase.FERTIL
            hoy < ovulacion -> if (diaDelCiclo <= p.duracionPeriodo) Fase.MENSTRUAL else Fase.FOLICULAR
            else -> Fase.LUTEA
        }
        val retraso = if (hoy > proximo) ChronoUnit.DAYS.between(proximo, hoy).toInt() else 0
        return Hoy(
            diaDelCiclo = diaDelCiclo,
            fase = fase,
            diaDelPeriodo = diaDelPeriodo,
            diasParaProximo = ChronoUnit.DAYS.between(hoy, proximo).toInt().coerceAtLeast(0),
            retraso = retraso,
            retrasoFueraDeRango = retraso > p.rango,
        )
    }

    /**
     * Días estimados (período, ventana fértil y ovulación) entre [desde] y [hasta].
     * Solo marca días desde hoy en adelante: el pasado se ve con lo que se anotó.
     * Si el período viene con retraso, se lo muestra como posible desde hoy.
     */
    fun marcas(p: Prediccion, desde: LocalDate, hasta: LocalDate, hoy: LocalDate = LocalDate.now()): Map<LocalDate, Marca> {
        val proximo = p.proximoInicio ?: return emptyMap()
        val ultimo = p.ultimoInicio!!
        val marcas = mutableMapOf<LocalDate, Marca>()

        fun marcar(inicioPeriodo: LocalDate) {
            val ovulacion = inicioPeriodo.minusDays(FASE_LUTEA.toLong())
            for (i in -5..1) marcas[ovulacion.plusDays(i.toLong())] = Marca.FERTIL
            marcas[ovulacion] = Marca.OVULACION
            for (i in 0 until p.duracionPeriodo) marcas[inicioPeriodo.plusDays(i.toLong())] = Marca.PERIODO
        }

        // Ventana fértil del ciclo actual (si todavía no pasó) y próximos períodos.
        val primero = if (proximo < hoy) hoy else proximo
        if (proximo >= hoy) marcar(proximo) else {
            for (i in 0 until p.duracionPeriodo) marcas[primero.plusDays(i.toLong())] = Marca.PERIODO
        }
        var siguiente = primero.plusDays(p.duracionCiclo.toLong())
        while (siguiente.minusDays(FASE_LUTEA + 5L) <= hasta) {
            marcar(siguiente)
            siguiente = siguiente.plusDays(p.duracionCiclo.toLong())
        }
        return marcas.filterKeys { it in desde..hasta && it >= hoy && it > ultimo }
    }

    /** Días que faltan (o pasaron, negativo) entre dos fechas, para textos. */
    fun distancia(a: LocalDate, b: LocalDate): Int = abs(ChronoUnit.DAYS.between(a, b).toInt())
}
