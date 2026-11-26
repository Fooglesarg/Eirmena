package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.educacion.Hormonas
import com.byfooglesframes.eirmena.educacion.Niveles

/** Colores de las curvas: validados para daltonismo; cada curva lleva además su nombre al lado. */
private enum class Serie(val nombre: String, val color: Color, val valor: (Niveles) -> Float) {
    ESTROGENO("Estrógeno", Color(0xFFD9623B), { it.estrogeno }),
    PROGESTERONA("Progesterona", Color(0xFF8A4FC4), { it.progesterona }),
    LH("LH", Color(0xFF00A383), { it.lh }),
    FSH("FSH", Color(0xFFD4A017), { it.fsh }),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaHormonas(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val p = remember(datos) { Ciclos.predecir(datos) }
    val duracion = p.duracionCiclo
    val hoyDia = Ciclos.hoy(datos, p)?.diaDelCiclo?.takeIf { it <= duracion }
    var dia by remember { mutableIntStateOf(hoyDia ?: 1) }
    var ocultas by remember { mutableStateOf(setOf<Serie>()) }
    val explicacion = Hormonas.explicacion(dia, duracion, p.duracionPeriodo)
    val niveles = Hormonas.niveles(dia.toFloat(), duracion)
    val metodo = datos.ajustes.anticonceptivo.metodo

    Subpantalla("Tus hormonas", alVolver) {
        Text(
            "Así suben y bajan las hormonas en un ciclo de $duracion días como el tuyo. Tocá o deslizá sobre el gráfico para ver cada día.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Tarjeta {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Serie.entries.forEach { s ->
                    Chip(s.nombre, s !in ocultas) { ocultas = if (s in ocultas) ocultas - s else ocultas + s }
                }
            }
            Grafico(duracion, p.duracionPeriodo, dia, hoyDia, ocultas, alElegir = { dia = it })
            Slider(
                value = dia.toFloat(),
                onValueChange = { dia = it.toInt().coerceIn(1, duracion) },
                valueRange = 1f..duracion.toFloat(),
                steps = duracion - 2,
                colors = SliderDefaults.colors(thumbColor = Colores.Vino, activeTrackColor = Colores.Terracota, inactiveTrackColor = Colores.CremaOscura),
            )
        }

        Tarjeta(color = Colores.CremaOscura) {
            Text(
                "Día $dia${if (dia == hoyDia) " · hoy" else ""} — ${explicacion.etapa.nombre}",
                style = MaterialTheme.typography.titleMedium, color = Colores.Vino,
            )
            // Lectura en palabras de cada curva: la versión "tabla" del gráfico.
            Serie.entries.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                    Text("${s.nombre}: ${nivelEnPalabras(s.valor(niveles))}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Tarjeta {
            Text("Qué pasa adentro", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            Text(explicacion.hormonas, style = MaterialTheme.typography.bodyMedium)
            Text("Cómo te podés sentir", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            Text(explicacion.sensaciones, style = MaterialTheme.typography.bodyMedium)
        }
        if (metodo.suprimeOvulacion) {
            Text(
                "Con ${metodo.nombre.lowercase()} estas curvas no se dan así: el método mantiene las hormonas estables y no hay " +
                    "ovulación. El gráfico muestra el ciclo natural, para entenderlo.",
                style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura,
            )
        }
        Text(
            "Es un esquema ilustrativo con niveles relativos: cada hormona se mide en unidades distintas y los valores reales " +
                "cambian de persona a persona. Para conocer tus niveles hace falta un análisis de sangre.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}

private fun nivelEnPalabras(v: Float) = when {
    v < 0.25f -> "bajo"
    v < 0.5f -> "medio"
    v < 0.8f -> "alto"
    else -> "en su pico"
}

@Composable
private fun Grafico(duracion: Int, periodo: Int, dia: Int, hoyDia: Int?, ocultas: Set<Serie>, alElegir: (Int) -> Unit) {
    val medidor = rememberTextMeasurer()
    val etiqueta = TextStyle(fontSize = 11.sp, color = Colores.TextoSuave, fontFamily = Outfit)
    val ovulacion = Hormonas.diaOvulacion(duracion)

    Canvas(
        Modifier.fillMaxWidth().height(230.dp)
            .pointerInput(duracion) {
                fun aDia(x: Float) = (1 + ((x - 8.dp.toPx()) / (size.width - 16.dp.toPx())) * (duracion - 1)).toInt().coerceIn(1, duracion)
                detectTapGestures { alElegir(aDia(it.x)) }
            }
            .pointerInput(duracion) {
                fun aDia(x: Float) = (1 + ((x - 8.dp.toPx()) / (size.width - 16.dp.toPx())) * (duracion - 1)).toInt().coerceIn(1, duracion)
                detectHorizontalDragGestures { cambio, _ -> alElegir(aDia(cambio.position.x)) }
            },
    ) {
        val margen = 8.dp.toPx()
        val abajo = size.height - 22.dp.toPx()
        val arriba = 18.dp.toPx()
        val ancho = size.width - 2 * margen
        fun x(d: Float) = margen + (d - 1) / (duracion - 1) * ancho
        fun y(v: Float) = abajo - v * (abajo - arriba)

        // Franja del período y línea de ovulación, tenues.
        drawRect(Colores.RosaClaro.copy(alpha = 0.6f), Offset(x(1f), arriba), androidx.compose.ui.geometry.Size(x(periodo + 0.5f) - x(1f), abajo - arriba))
        drawLine(Colores.Linea, Offset(x(ovulacion.toFloat()), arriba), Offset(x(ovulacion.toFloat()), abajo), 1.dp.toPx())
        drawLine(Colores.Linea, Offset(margen, abajo), Offset(size.width - margen, abajo), 1.dp.toPx())

        // Curvas
        Serie.entries.filter { it !in ocultas }.forEach { s ->
            val camino = Path()
            var d = 1f
            while (d <= duracion) {
                val px = x(d)
                val py = y(s.valor(Hormonas.niveles(d, duracion)))
                if (d == 1f) camino.moveTo(px, py) else camino.lineTo(px, py)
                d += 0.25f
            }
            drawPath(camino, s.color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }

        // Nombres directos sobre el pico de cada curva.
        val picos = mapOf(
            Serie.ESTROGENO to (ovulacion - 4f), Serie.PROGESTERONA to (ovulacion + 7f),
            Serie.LH to (ovulacion + 1.2f), Serie.FSH to 3f,
        )
        picos.filterKeys { it !in ocultas }.forEach { (s, d) ->
            val dd = d.coerceIn(1f, duracion.toFloat())
            val texto = medidor.measure(s.nombre, etiqueta.copy(color = Colores.Texto))
            val px = (x(dd) - texto.size.width / 2).coerceIn(0f, size.width - texto.size.width)
            val py = y(s.valor(Hormonas.niveles(dd, duracion))) - texto.size.height - 2.dp.toPx()
            drawText(texto, topLeft = Offset(px, py.coerceAtLeast(0f)))
        }

        // Ejes: día 1, ovulación y último día.
        fun rotulo(t: String, d: Float) {
            val m = medidor.measure(t, etiqueta)
            drawText(m, topLeft = Offset((x(d) - m.size.width / 2).coerceIn(0f, size.width - m.size.width), abajo + 4.dp.toPx()))
        }
        rotulo("Día 1", 1f)
        rotulo("Ovulación", ovulacion.toFloat())
        rotulo("Día $duracion", duracion.toFloat())

        // Día elegido: línea y puntos con anillo del color de fondo.
        val xd = x(dia.toFloat())
        drawLine(Colores.Vino.copy(alpha = 0.5f), Offset(xd, arriba), Offset(xd, abajo), 1.5.dp.toPx())
        if (hoyDia != null && hoyDia != dia) {
            drawCircle(Colores.Vino, 3.dp.toPx(), Offset(x(hoyDia.toFloat()), abajo))
        }
        Serie.entries.filter { it !in ocultas }.forEach { s ->
            val c = Offset(xd, y(s.valor(Hormonas.niveles(dia.toFloat(), duracion))))
            drawCircle(Colores.Tarjeta, 6.dp.toPx(), c)
            drawCircle(s.color, 4.dp.toPx(), c)
        }
    }
}

