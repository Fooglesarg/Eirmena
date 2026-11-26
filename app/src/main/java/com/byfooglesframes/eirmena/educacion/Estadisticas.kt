package com.byfooglesframes.eirmena.educacion

import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Nivel
import com.byfooglesframes.eirmena.datos.Sintoma
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class CicloResumen(val inicio: LocalDate, val duracion: Int, val periodo: Int, val diasAbundantes: Int)

/** Una fila de un mapa de calor: un valor de 0 a 1 por cada fase, o null si no hay datos de esa fase. */
data class FilaFase(val nombre: String, val valores: List<Float?>)

data class ResumenEstadistico(
    val ciclos: List<CicloResumen>,
    val promedio: Int?,
    val minimo: Int?,
    val maximo: Int?,
    val periodoPromedio: Int?,
    val diasAnotados: Int,
    val sintomas: List<Pair<Sintoma, Int>>,
    val animos: List<Pair<Animo, Int>>,
    val sintomasPorFase: List<FilaFase>,
    val animoPorFase: List<FilaFase>,
    val nivelesPorFase: List<FilaFase>,
)

/**
 * Resúmenes de lo anotado. Para ubicar cada día en una fase se usa la duración real de su ciclo
 * (o la estimada si es el ciclo en curso) y las mismas fases que el gráfico de hormonas.
 */
object Estadisticas {
    val fases = Etapa.entries

    fun calcular(datos: Datos, desde: LocalDate, hoy: LocalDate = LocalDate.now()): ResumenEstadistico {
        val periodos = Ciclos.periodos(datos.dias)
        val p = Ciclos.predecir(datos, hoy)
        val ciclos = Ciclos.ciclos(periodos)
            .filter { it.duracion != null && it.duracion in Datos.CICLO_MIN..Datos.CICLO_MAX && it.inicio >= desde }
            .map { c ->
                CicloResumen(c.inicio, c.duracion!!, c.periodo.dias,
                    (0 until c.periodo.dias).count { datos.dias[c.inicio.plusDays(it.toLong())]?.flujo == Flujo.ABUNDANTE })
            }
        val dias = datos.dias.values.filter { it.fecha in desde..hoy }

        // Fase de cada día anotado.
        fun fase(f: LocalDate): Etapa? {
            val i = periodos.indexOfLast { it.inicio <= f }
            if (i < 0) return null
            val inicio = periodos[i].inicio
            val duracion = periodos.getOrNull(i + 1)?.let { ChronoUnit.DAYS.between(inicio, it.inicio).toInt() } ?: p.duracionCiclo
            val dia = ChronoUnit.DAYS.between(inicio, f).toInt() + 1
            if (duracion > Datos.CICLO_MAX || dia > duracion + 10) return null
            return Hormonas.etapa(dia.coerceAtMost(duracion), duracion, periodos[i].dias)
        }
        val porFase = dias.mapNotNull { r -> fase(r.fecha)?.let { it to r } }.groupBy({ it.first }, { it.second })

        val sintomas = Sintoma.entries.map { s -> s to dias.count { s in it.sintomas } }.filter { it.second > 0 }.sortedByDescending { it.second }
        val animos = Animo.entries.map { a -> a to dias.count { a in it.animo } }.filter { it.second > 0 }.sortedByDescending { it.second }

        // Proporción de días de cada fase en que apareció cada síntoma o ánimo.
        fun proporcion(cond: (com.byfooglesframes.eirmena.datos.RegistroDia) -> Boolean): List<Float?> = fases.map { f ->
            val lista = porFase[f].orEmpty()
            if (lista.size < 2) null else lista.count(cond).toFloat() / lista.size
        }
        // Promedio de un nivel (bajo=0, medio=0,5, alto=1) en cada fase.
        fun nivel(valor: (com.byfooglesframes.eirmena.datos.RegistroDia) -> Nivel?): List<Float?> = fases.map { f ->
            val v = porFase[f].orEmpty().mapNotNull(valor)
            if (v.isEmpty()) null else v.map { it.ordinal / 2f }.average().toFloat()
        }

        return ResumenEstadistico(
            ciclos = ciclos.takeLast(12),
            promedio = ciclos.takeIf { it.isNotEmpty() }?.map { it.duracion }?.average()?.roundToInt(),
            minimo = ciclos.minOfOrNull { it.duracion },
            maximo = ciclos.maxOfOrNull { it.duracion },
            periodoPromedio = ciclos.takeIf { it.isNotEmpty() }?.map { it.periodo }?.average()?.roundToInt(),
            diasAnotados = dias.size,
            sintomas = sintomas,
            animos = animos,
            sintomasPorFase = sintomas.take(6).map { (s, _) -> FilaFase(s.nombre, proporcion { s in it.sintomas }) },
            animoPorFase = animos.take(6).map { (a, _) -> FilaFase(a.nombre, proporcion { a in it.animo }) },
            nivelesPorFase = listOf(
                FilaFase("Energía", nivel { it.energia }),
                FilaFase("Sueño", nivel { it.sueno }),
                FilaFase("Deseo", nivel { it.deseo }),
            ).filter { f -> f.valores.any { it != null } },
        )
    }
}
