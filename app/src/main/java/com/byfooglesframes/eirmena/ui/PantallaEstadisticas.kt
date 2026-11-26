package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.educacion.CicloResumen
import com.byfooglesframes.eirmena.educacion.Estadisticas
import com.byfooglesframes.eirmena.educacion.FilaFase
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaEstadisticas(vm: EirmenaViewModel, alVolver: () -> Unit, abrirInforme: () -> Unit) {
    val datos by vm.datos.collectAsState()
    var meses by remember { mutableIntStateOf(6) }
    val hoy = LocalDate.now()
    val est = remember(datos, meses) { Estadisticas.calcular(datos, if (meses == 0) LocalDate.MIN.plusYears(1) else hoy.minusMonths(meses.toLong()), hoy) }

    Subpantalla("Estadísticas", alVolver) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3 to "3 meses", 6 to "6 meses", 12 to "1 año", 0 to "Todo").forEach { (m, t) -> Chip(t, meses == m) { meses = m } }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Numero("Ciclo promedio", est.promedio?.let { "$it días" } ?: "—", Modifier.weight(1f))
            Numero("Período promedio", est.periodoPromedio?.let { "$it días" } ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Numero("Más corto · más largo", if (est.minimo != null) "${est.minimo} · ${est.maximo}" else "—", Modifier.weight(1f))
            Numero("Días anotados", "${est.diasAnotados}", Modifier.weight(1f))
        }

        Tarjeta {
            Text("Duración de tus ciclos", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            if (est.ciclos.size < 2) {
                Vacio("Con al menos dos ciclos completos vas a ver cómo varían.")
            } else {
                BarrasCiclos(est.ciclos, est.promedio)
            }
        }

        Tarjeta {
            Text("Síntomas más frecuentes", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            if (est.sintomas.isEmpty()) Vacio("Anotá síntomas en la hoja del día para ver cuáles se repiten.")
            else BarrasHorizontales(est.sintomas.take(8).map { it.first.nombre to it.second }, "días")
        }

        if (est.sintomasPorFase.isNotEmpty()) {
            Tarjeta {
                Text("Síntomas según la fase", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text("En qué parte del ciclo aparece cada uno: más oscuro, más días.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
                MapaDeCalor(est.sintomasPorFase, porcentaje = true)
            }
        }
        if (est.animoPorFase.isNotEmpty()) {
            Tarjeta {
                Text("Ánimo según la fase", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                MapaDeCalor(est.animoPorFase, porcentaje = true)
            }
        }
        if (est.nivelesPorFase.isNotEmpty()) {
            Tarjeta {
                Text("Energía, sueño y deseo según la fase", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text("Promedio de lo que anotaste: más oscuro, más alto.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
                MapaDeCalor(est.nivelesPorFase, porcentaje = false)
            }
        }

        Tarjeta(androidx.compose.ui.Modifier, color = Colores.Terracota) {
            Text("Informe para la consulta", style = MaterialTheme.typography.titleMedium, color = Colores.Crema)
            Text("Un PDF con todo esto para mostrarle a tu médica o médico.", style = MaterialTheme.typography.bodyMedium, color = Colores.Crema)
            androidx.compose.material3.Button(
                onClick = abrirInforme,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Colores.Crema, contentColor = Colores.Vino),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Armar informe") }
        }
    }
}

@Composable
private fun Numero(titulo: String, valor: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(Colores.Tarjeta).padding(14.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
        Text(valor, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
    }
}

@Composable
private fun Vacio(texto: String) = Text(texto, style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)

/** Una barra por ciclo, franja normal sombreada y línea de promedio. Tocar una barra muestra su detalle. */
@Composable
private fun BarrasCiclos(ciclos: List<CicloResumen>, promedio: Int?) {
    val medidor = rememberTextMeasurer()
    val estilo = TextStyle(fontSize = 11.sp, color = Colores.TextoSuave, fontFamily = Outfit)
    var elegido by remember(ciclos) { mutableIntStateOf(ciclos.lastIndex) }
    Canvas(
        Modifier.fillMaxWidth().height(180.dp).pointerInput(ciclos) {
            detectTapGestures { o -> elegido = (o.x / (size.width / ciclos.size)).toInt().coerceIn(0, ciclos.lastIndex) }
        },
    ) {
        val arriba = 16.dp.toPx()
        val abajo = size.height - 20.dp.toPx()
        val maximo = maxOf(ciclos.maxOf { it.duracion }, 40).toFloat()
        fun y(v: Float) = abajo - v / maximo * (abajo - arriba)
        drawRect(Colores.SalviaClara.copy(alpha = 0.55f), Offset(0f, y(38f)), Size(size.width, y(24f) - y(38f)))
        val paso = size.width / ciclos.size
        val anchoBarra = minOf(paso * 0.6f, 28.dp.toPx())
        ciclos.forEachIndexed { i, c ->
            val x = i * paso + (paso - anchoBarra) / 2
            drawRoundRect(
                if (i == elegido) Colores.Vino else Colores.Terracota,
                Offset(x, y(c.duracion.toFloat())), Size(anchoBarra, abajo - y(c.duracion.toFloat())),
                CornerRadius(4.dp.toPx()),
            )
        }
        drawLine(Colores.TextoSuave, Offset(0f, abajo), Offset(size.width, abajo), 1.dp.toPx())
        promedio?.let { pr ->
            drawLine(Colores.Texto, Offset(0f, y(pr.toFloat())), Offset(size.width, y(pr.toFloat())), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
            val m = medidor.measure("promedio $pr", estilo.copy(color = Colores.Texto))
            drawText(m, topLeft = Offset(size.width - m.size.width, y(pr.toFloat()) - m.size.height - 2f))
        }
        val m1 = medidor.measure(Fechas.corta(ciclos.first().inicio), estilo)
        drawText(m1, topLeft = Offset(0f, abajo + 3.dp.toPx()))
        val m2 = medidor.measure(Fechas.corta(ciclos.last().inicio), estilo)
        drawText(m2, topLeft = Offset(size.width - m2.size.width, abajo + 3.dp.toPx()))
    }
    val c = ciclos[elegido]
    Text(
        "Ciclo del ${Fechas.larga(c.inicio)}: ${c.duracion} días, con ${Fechas.dias(c.periodo)} de período.",
        style = MaterialTheme.typography.bodyMedium,
    )
    Text("La franja verde es el rango normal (24 a 38 días). Tocá una barra para ver su detalle.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
}

@Composable
private fun BarrasHorizontales(filas: List<Pair<String, Int>>, unidad: String) {
    val maximo = filas.maxOf { it.second }.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        filas.forEach { (nombre, n) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(nombre, Modifier.width(130.dp), style = MaterialTheme.typography.bodyMedium)
                Box(Modifier.weight(1f).height(14.dp)) {
                    Box(Modifier.fillMaxWidth(n / maximo).height(14.dp).clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)).background(Colores.Terracota))
                }
                Text("$n", Modifier.width(32.dp), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End)
            }
        }
        Text("Cantidad de $unidad con cada síntoma.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
    }
}

private val COLUMNAS = listOf("Mens.", "Folic.", "Ovul.", "Lútea", "Premens.")

/** Escala de un solo color, de claro a oscuro. Sin datos: gris crema con guion. */
@Composable
private fun MapaDeCalor(filas: List<FilaFase>, porcentaje: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row {
            Box(Modifier.width(110.dp))
            COLUMNAS.forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave, textAlign = TextAlign.Center) }
        }
        filas.forEach { f ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(f.nombre, Modifier.width(110.dp), style = MaterialTheme.typography.bodySmall, maxLines = 2)
                f.valores.forEach { v ->
                    val fondo = v?.let { lerp(Colores.RosaClaro, Colores.TerracotaOscura, it.coerceIn(0f, 1f)) } ?: Colores.CremaOscura
                    Box(
                        Modifier.weight(1f).height(30.dp).padding(horizontal = 1.5.dp).clip(RoundedCornerShape(4.dp)).background(fondo),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            when {
                                v == null -> "–"
                                porcentaje -> "${(v * 100).roundToInt()}%"
                                else -> listOf("bajo", "medio", "alto")[(v * 2).roundToInt().coerceIn(0, 2)]
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if ((v ?: 0f) > 0.5f) Color.White else Colores.Texto,
                        )
                    }
                }
            }
        }
        Text(
            "Mens.: menstruación · Folic.: folicular · Ovul.: ovulación · Premens.: días premenstruales. \"–\": pocos datos.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}
