package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Fertilidad
import com.byfooglesframes.eirmena.ciclo.OvulacionDetectada
import com.byfooglesframes.eirmena.ciclo.Prediccion
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Estado de fertilidad de hoy, en una frase, para Hoy y para la pantalla de fertilidad. */
data class EstadoFertil(val titulo: String, val texto: String)

fun estadoFertil(datos: Datos, p: Prediccion, hoy: LocalDate): EstadoFertil? {
    val proximo = p.proximoInicio ?: return null
    p.ovulacion?.let { o ->
        return EstadoFertil(
            "Ovulaste el ${Fechas.larga(o.fecha)}",
            "Lo detecté ${o.fuente.nombre}. Si hay embarazo, un test ya es confiable desde el ${Fechas.larga(proximo)}, " +
                "el día en que te tendría que venir.",
        )
    }
    val ovulacion = proximo.minusDays(Ciclos.FASE_LUTEA.toLong())
    val inicio = ovulacion.minusDays(5)
    val ultimoMoco = datos.dias.values.filter { it.fecha <= hoy && it.moco != null }.maxByOrNull { it.fecha }
    val lhHoy = datos.dias[hoy]?.testOvulacion == Test.POSITIVO
    return when {
        lhHoy -> EstadoFertil("Test de ovulación positivo", "Vas a ovular en las próximas 24 a 36 horas: hoy y mañana son tus mejores días.")
        hoy in inicio..ovulacion.plusDays(1) -> EstadoFertil(
            "Estás en tu ventana fértil estimada",
            "Va del ${Fechas.larga(inicio)} al ${Fechas.larga(ovulacion.plusDays(1))}. Tener relaciones cada 1 o 2 días estos días da las mejores chances." +
                if (ultimoMoco?.moco?.fertil == true && ultimoMoco.fecha >= hoy.minusDays(1)) " Tu flujo también indica días fértiles." else "",
        )
        hoy < inicio -> EstadoFertil(
            "Tu ventana fértil empieza en ${Fechas.dias(ChronoUnit.DAYS.between(hoy, inicio).toInt())}",
            "Estimada del ${Fechas.larga(inicio)} al ${Fechas.larga(ovulacion.plusDays(1))}. Anotar temperatura, flujo o tests la vuelve más precisa.",
        )
        else -> EstadoFertil(
            "Pasó tu ventana fértil estimada",
            "Si hay embarazo, un test es confiable desde el ${Fechas.larga(proximo)}, el día en que te tendría que venir.",
        )
    }
}

@Composable
fun TarjetaFertil(e: EstadoFertil, alTocar: () -> Unit) {
    Tarjeta(Modifier.clickable(onClick = alTocar), color = Colores.SalviaClara) {
        Text("BUSCAR EMBARAZO", style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(e.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(e.texto, style = MaterialTheme.typography.bodyMedium)
        Text("Ver tu temperatura y consejos →", style = MaterialTheme.typography.labelLarge, color = Colores.Vino)
    }
}

@Composable
fun PantallaFertilidad(vm: EirmenaViewModel, alVolver: () -> Unit, abrirDia: (LocalDate) -> Unit, abrirArticulo: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val hoy = LocalDate.now()
    val p = remember(datos) { Ciclos.predecir(datos, hoy) }
    val inicio = p.ultimoInicio
    val temps = remember(datos) { inicio?.let { Fertilidad.temperaturas(datos, it, hoy) }.orEmpty() }

    Subpantalla("Buscar embarazo", alVolver) {
        estadoFertil(datos, p, hoy)?.let { e ->
            Tarjeta(color = Colores.SalviaClara) {
                Text(e.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text(e.texto, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Tarjeta {
            Text("Temperatura basal de este ciclo", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            if (inicio == null || temps.size < 2) {
                Text(
                    "Anotá tu temperatura cada mañana al despertar, antes de levantarte. Con unos días vas a ver la curva acá.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                GraficoTemperatura(inicio, temps, p.ovulacion)
                Text(
                    p.ovulacion?.lineaBase?.let {
                        "La línea punteada es tu línea base (%.2f °C). La suba por encima de ella confirma la ovulación.".format(it)
                    } ?: "Cuando haya 3 temperaturas seguidas por encima de las 6 anteriores, se confirma la ovulación.",
                    style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                )
            }
            OutlinedButton(onClick = { abrirDia(hoy) }, modifier = Modifier.fillMaxWidth()) { Text("Anotar la de hoy") }
        }

        Tarjeta(color = Colores.CremaOscura) {
            Text("Para tener en cuenta", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            Text(
                "• Tomá 400 microgramos de ácido fólico por día.\n" +
                    "• Consultá si pasan 12 meses sin embarazo (6 meses si tenés 35 años o más).\n" +
                    "• Estas señales no sirven como método anticonceptivo.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        OutlinedButton(onClick = { abrirArticulo("buscar-embarazo") }, modifier = Modifier.fillMaxWidth()) { Text("Buscar un embarazo") }
        OutlinedButton(onClick = { abrirArticulo("temperatura-moco") }, modifier = Modifier.fillMaxWidth()) { Text("Temperatura, flujo y tests") }
    }
}

/** Una sola serie: temperatura por día del ciclo, con la línea base y la ovulación marcadas. */
@Composable
private fun GraficoTemperatura(inicio: LocalDate, temps: List<Pair<LocalDate, Double>>, ov: OvulacionDetectada?) {
    val medidor = rememberTextMeasurer()
    val estilo = TextStyle(fontSize = 11.sp, color = Colores.TextoSuave, fontFamily = Outfit)
    Canvas(Modifier.fillMaxWidth().height(200.dp)) {
        val izq = 40.dp.toPx()
        val der = size.width - 8.dp.toPx()
        val arriba = 10.dp.toPx()
        val abajo = size.height - 22.dp.toPx()
        val dias = temps.map { ChronoUnit.DAYS.between(inicio, it.first).toInt() + 1 }
        val maxDia = maxOf(dias.max(), 14)
        val minT = minOf(temps.minOf { it.second }, ov?.lineaBase ?: 99.0) - 0.15
        val maxT = maxOf(temps.maxOf { it.second }, ov?.lineaBase ?: 0.0) + 0.15
        fun x(d: Int) = izq + (d - 1f) / (maxDia - 1f) * (der - izq)
        fun y(t: Double) = (abajo - (t - minT) / (maxT - minT) * (abajo - arriba)).toFloat()

        drawLine(Colores.Linea, Offset(izq, abajo), Offset(der, abajo), 1.dp.toPx())
        listOf(minT + 0.15, maxT - 0.15).forEach { t ->
            val m = medidor.measure("%.1f°".format(t), estilo)
            drawText(m, topLeft = Offset(0f, y(t) - m.size.height / 2))
        }
        listOf(1, maxDia).forEach { d ->
            val m = medidor.measure("Día $d", estilo)
            drawText(m, topLeft = Offset((x(d) - m.size.width / 2).coerceIn(0f, size.width - m.size.width), abajo + 4.dp.toPx()))
        }
        ov?.lineaBase?.let { base ->
            drawLine(
                Colores.TextoSuave, Offset(izq, y(base)), Offset(der, y(base)), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }
        ov?.let { o ->
            val d = ChronoUnit.DAYS.between(inicio, o.fecha).toInt() + 1
            drawLine(Colores.Salvia, Offset(x(d), arriba), Offset(x(d), abajo), 2.dp.toPx())
            val m = medidor.measure("Ovulación", estilo.copy(color = Colores.Texto))
            drawText(m, topLeft = Offset((x(d) + 4.dp.toPx()).coerceAtMost(size.width - m.size.width), arriba))
        }
        for (i in 1 until temps.size) {
            drawLine(Colores.Terracota, Offset(x(dias[i - 1]), y(temps[i - 1].second)), Offset(x(dias[i]), y(temps[i].second)), 2.dp.toPx(), StrokeCap.Round)
        }
        temps.forEachIndexed { i, (_, t) ->
            drawCircle(Colores.Tarjeta, 5.dp.toPx(), Offset(x(dias[i]), y(t)))
            drawCircle(Colores.Terracota, 4.dp.toPx(), Offset(x(dias[i]), y(t)))
        }
    }
}
