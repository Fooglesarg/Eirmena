package com.byfooglesframes.eirmena.educacion

import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Relacion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

sealed class Bloque {
    data class Titulo(val texto: String) : Bloque()
    data class Parrafo(val texto: String) : Bloque()
    data class Lista(val items: List<String>) : Bloque()
    data class Tabla(val encabezados: List<String>, val filas: List<List<String>>) : Bloque()
    /** Gráfico de barras de la duración de los ciclos (lo dibuja el PDF; en Markdown va como tabla). */
    data class BarrasCiclos(val ciclos: List<CicloResumen>) : Bloque()
}

data class Informe(val titulo: String, val subtitulo: String, val bloques: List<Bloque>, val pie: String)

data class OpcionesInforme(
    val meses: Int = 6,
    val nombre: String = "",
    val incluirNotas: Boolean = false,
    /** Relaciones sexuales y anticoncepción de emergencia: datos sensibles, apagado por defecto. */
    val incluirSexo: Boolean = false,
)

/** Arma el informe para llevar a una consulta, solo con lo que la persona eligió incluir. */
object Informes {
    private val corta = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private fun f(d: LocalDate) = corta.format(d)

    fun armar(datos: Datos, o: OpcionesInforme, hoy: LocalDate = LocalDate.now()): Informe {
        val desde = hoy.minusMonths(o.meses.toLong())
        val est = Estadisticas.calcular(datos, desde, hoy)
        val p = Ciclos.predecir(datos, hoy)
        val a = datos.ajustes
        val b = mutableListOf<Bloque>()

        b += Bloque.Titulo("Resumen")
        b += Bloque.Lista(buildList {
            add("Etapa: ${a.etapa.nombre}.")
            if (est.promedio != null) {
                add("Duración promedio del ciclo: ${est.promedio} días (de ${est.ciclos.size} ciclos completos; " +
                    "más corto ${est.minimo}, más largo ${est.maximo}).")
                add("Duración promedio del período: ${est.periodoPromedio} días.")
                add("Regularidad: ${p.regularidad.nombre.lowercase()}" + if (p.variacion > 0) " (variación de ${p.variacion} días)." else ".")
            } else add("Todavía no hay ciclos completos anotados en este período.")
            p.ultimoInicio?.let { add("Inicio del último período: ${f(it)}.") }
            if (a.etapa != EtapaVida.EMBARAZO) p.proximoInicio?.let { add("Próximo período estimado: ${f(it)} (± ${p.rango} días).") }
            p.ovulacion?.let { add("Ovulación de este ciclo detectada ${it.fuente.nombre.removePrefix("por tu ").let { s -> "por $s" }}: ${f(it.fecha)}.") }
            add("Días con registros en el período del informe: ${est.diasAnotados}.")
        })

        val ac = a.anticonceptivo
        if (ac.metodo != Metodo.NINGUNO) {
            b += Bloque.Titulo("Método anticonceptivo")
            b += Bloque.Parrafo(ac.metodo.nombre +
                (if (ac.metodo == Metodo.PASTILLA) " (${ac.esquema.nombre})" else "") +
                (ac.inicio?.let { if (ac.metodo.tieneVencimiento) ", colocado el ${f(it)}" else ", fecha de referencia ${f(it)}" } ?: "") +
                (ac.vence?.let { ", vence el ${f(it)}" } ?: "") + ".")
        }

        a.embarazo?.let { e ->
            b += Bloque.Titulo("Embarazo")
            b += Bloque.Parrafo(
                if (e.fin == null) "En curso. FUM: ${f(e.fum)}. Edad gestacional: ${Gestacion.semanas(e.fum, hoy)} semanas y " +
                    "${Gestacion.diasExtra(e.fum, hoy)} días. Fecha probable de parto: ${f(e.fechaProbable)}."
                else "Último embarazo: FUM ${f(e.fum)}, finalizado el ${f(e.fin)}" + (e.resultado?.let { " (${it.nombre.lowercase()})" } ?: "") + "."
            )
        }

        if (est.ciclos.isNotEmpty()) {
            b += Bloque.Titulo("Ciclos")
            b += Bloque.BarrasCiclos(est.ciclos)
            b += Bloque.Tabla(
                listOf("Inicio", "Ciclo (días)", "Período (días)", "Días abundantes"),
                est.ciclos.reversed().map { listOf(f(it.inicio), "${it.duracion}", "${it.periodo}", "${it.diasAbundantes}") },
            )
        }

        if (est.sintomas.isNotEmpty()) {
            b += Bloque.Titulo("Síntomas más frecuentes")
            b += Bloque.Lista(est.sintomas.take(8).map { (s, n) -> "${s.nombre}: $n días" + patron(est.sintomasPorFase, s.nombre) })
        }
        if (est.animos.isNotEmpty()) {
            b += Bloque.Titulo("Ánimo")
            b += Bloque.Lista(est.animos.take(6).map { (an, n) -> "${an.nombre}: $n días" + patron(est.animoPorFase, an.nombre) })
        }

        val alertas = Alertas.calcular(datos, hoy)
        if (alertas.isNotEmpty()) {
            b += Bloque.Titulo("Patrones que Eirmena sugirió consultar")
            b += Bloque.Lista(alertas.map { "${it.titulo}: ${it.texto}" })
        }

        if (o.incluirSexo) {
            val dias = datos.dias.values.filter { it.fecha in desde..hoy }
            val sinPreservativo = dias.filter { Relacion.SIN_PRESERVATIVO in it.relaciones }.map { it.fecha }.sorted()
            val emergencia = dias.filter { it.pastillaEmergencia }.map { it.fecha }.sorted()
            val positivos = dias.filter { it.testEmbarazo != null }.sortedBy { it.fecha }
            b += Bloque.Titulo("Salud sexual")
            b += Bloque.Lista(buildList {
                add("Relaciones sin preservativo: ${if (sinPreservativo.isEmpty()) "no se anotaron" else sinPreservativo.joinToString { f(it) }}.")
                add("Anticoncepción de emergencia: ${if (emergencia.isEmpty()) "no se anotó" else emergencia.joinToString { f(it) }}.")
                if (positivos.isNotEmpty()) add("Tests de embarazo: " + positivos.joinToString { "${f(it.fecha)} ${it.testEmbarazo!!.nombre.lowercase()}" } + ".")
            })
        }

        if (o.incluirNotas) {
            val notas = datos.dias.values.filter { it.fecha in desde..hoy && it.nota.isNotBlank() }.sortedBy { it.fecha }
            if (notas.isNotEmpty()) {
                b += Bloque.Titulo("Notas")
                b += Bloque.Lista(notas.map { "${f(it.fecha)}: ${it.nota}" })
            }
        }

        return Informe(
            titulo = "Informe del ciclo" + if (o.nombre.isNotBlank()) " — ${o.nombre.trim()}" else "",
            subtitulo = "Del ${f(desde)} al ${f(hoy)} · generado el ${f(hoy)}",
            bloques = b,
            pie = "Datos registrados por la persona en la app Eirmena. Las fechas estimadas son cálculos estadísticos; " +
                "Eirmena no es un dispositivo médico ni hace diagnósticos.",
        )
    }

    /** " (sobre todo en la fase lútea)" si un síntoma se concentra en una fase. */
    private fun patron(filas: List<FilaFase>, nombre: String): String {
        val fila = filas.firstOrNull { it.nombre == nombre } ?: return ""
        val (i, v) = fila.valores.withIndex().filter { it.value != null }.maxByOrNull { it.value!! } ?: return ""
        if (v == null || v < 0.3f) return ""
        return " (sobre todo en: ${Estadisticas.fases[i].nombre.lowercase()}, ${(v * 100).roundToInt()} % de esos días)"
    }

    fun markdown(i: Informe): String = buildString {
        appendLine("# ${i.titulo}")
        appendLine()
        appendLine("_${i.subtitulo}_")
        i.bloques.forEach { b ->
            appendLine()
            when (b) {
                is Bloque.Titulo -> appendLine("## ${b.texto}")
                is Bloque.Parrafo -> appendLine(b.texto)
                is Bloque.Lista -> b.items.forEach { appendLine("- $it") }
                is Bloque.Tabla -> {
                    appendLine("| ${b.encabezados.joinToString(" | ")} |")
                    appendLine("|${b.encabezados.joinToString("|") { " --- " }}|")
                    b.filas.forEach { appendLine("| ${it.joinToString(" | ")} |") }
                }
                is Bloque.BarrasCiclos -> Unit // en Markdown alcanza con la tabla que sigue
            }
        }
        appendLine()
        appendLine("---")
        appendLine("_${i.pie}_")
    }
}
