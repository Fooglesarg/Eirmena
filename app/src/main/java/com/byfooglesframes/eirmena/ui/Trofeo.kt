package com.byfooglesframes.eirmena.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.byfooglesframes.eirmena.datos.Curiosidades
import java.time.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

const val NOMBRE_TROFEO = "Trofeo de Lyfjaberg"

/** Pantalla del logro: trofeo animado, fecha y el dato curioso que se gana. */
@Composable
fun VentanaTrofeo(fecha: LocalDate, recienGanado: Boolean, alCerrar: () -> Unit) {
    val aparicion = remember { Animatable(if (recienGanado) 0f else 1f) }
    LaunchedEffect(Unit) { aparicion.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 120f)) }

    Dialog(onDismissRequest = alCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Colores.Vino) {
            Box(Modifier.fillMaxSize()) {
                if (recienGanado) Confeti()
                Column(
                    Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        if (recienGanado) "¡Completaste la colección!" else "Logro desbloqueado",
                        style = MaterialTheme.typography.titleMedium, color = Colores.OroClaro,
                    )
                    Box(Modifier.size(260.dp).scale(aparicion.value), contentAlignment = Alignment.Center) {
                        Rayos()
                        DibujoTrofeo(Modifier.size(170.dp))
                    }
                    Text(NOMBRE_TROFEO, style = MaterialTheme.typography.headlineMedium, color = Colores.Crema, textAlign = TextAlign.Center)
                    Text(
                        "Descubriste las ${Curiosidades.todas.size} cartas de ¿Sabías que…? y subiste la colina de la curación de Eir.\n" +
                            "Lo ganaste el ${Fechas.larga(fecha)}.",
                        style = MaterialTheme.typography.bodyLarge, color = Colores.RosaClaro, textAlign = TextAlign.Center,
                    )
                    DatoDelTrofeo()
                    Button(onClick = alCerrar, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Guardar en mi colección") }
                }
            }
        }
    }
}

@Composable
private fun DatoDelTrofeo() {
    val d = Curiosidades.datoDelTrofeo
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Colores.Tarjeta).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "DATO DEL TROFEO",
            Modifier.clip(RoundedCornerShape(50)).background(Colores.OroClaro).padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = Colores.Vino,
        )
        Text("¿Sabías que…", style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)
        Text(d.titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        Text(d.texto, style = MaterialTheme.typography.bodyLarge)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Colores.CremaOscura).padding(14.dp)) {
            Text(d.conexion, style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura)
        }
    }
}

/** Fila del trofeo al principio de la colección: bloqueado o ganado. */
@Composable
fun FilaTrofeo(fecha: LocalDate?, faltan: Int, alTocar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (fecha != null) Colores.Vino else Colores.CremaOscura)
            .clickable(enabled = fecha != null, onClick = alTocar)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (fecha != null) DibujoTrofeo(Modifier.size(44.dp))
            else Icon(Icons.Outlined.Lock, null, tint = Colores.TextoSuave)
        }
        Column(Modifier.weight(1f)) {
            Text(NOMBRE_TROFEO, style = MaterialTheme.typography.titleMedium, color = if (fecha != null) Colores.OroClaro else Colores.TextoSuave)
            Text(
                if (fecha != null) "Ganado el ${Fechas.larga(fecha)} · tocá para verlo"
                else "Descubrí todas las cartas para ganarlo (faltan $faltan).",
                style = MaterialTheme.typography.bodySmall,
                color = if (fecha != null) Colores.RosaClaro else Colores.TextoSuave,
            )
        }
    }
}

/** Copa dorada con una luna creciente grabada, dibujada a mano. */
@Composable
fun DibujoTrofeo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val oro = Brush.horizontalGradient(listOf(Colores.OroOscuro, Colores.OroClaro, Colores.Oro, Colores.OroOscuro))
        val asa = Stroke(width = w * 0.06f)

        // Asas
        drawArc(Colores.Oro, 90f, 180f, false, Offset(w * 0.12f, h * 0.14f), Size(w * 0.26f, h * 0.32f), style = asa)
        drawArc(Colores.Oro, -90f, 180f, false, Offset(w * 0.62f, h * 0.14f), Size(w * 0.26f, h * 0.32f), style = asa)

        // Copa
        val copa = Path().apply {
            moveTo(w * 0.24f, h * 0.10f)
            lineTo(w * 0.76f, h * 0.10f)
            cubicTo(w * 0.76f, h * 0.45f, w * 0.64f, h * 0.58f, w * 0.54f, h * 0.60f)
            lineTo(w * 0.46f, h * 0.60f)
            cubicTo(w * 0.36f, h * 0.58f, w * 0.24f, h * 0.45f, w * 0.24f, h * 0.10f)
            close()
        }
        drawPath(copa, oro)
        drawRect(Colores.OroClaro, Offset(w * 0.24f, h * 0.10f), Size(w * 0.52f, h * 0.035f))

        // Pie y base
        drawRect(Colores.OroOscuro, Offset(w * 0.46f, h * 0.60f), Size(w * 0.08f, h * 0.14f))
        drawRoundRect(oro, Offset(w * 0.32f, h * 0.73f), Size(w * 0.36f, h * 0.07f), CornerRadius(w * 0.02f))
        drawRoundRect(Colores.Vino, Offset(w * 0.26f, h * 0.80f), Size(w * 0.48f, h * 0.12f), CornerRadius(w * 0.025f))
        drawRoundRect(Colores.OroClaro, Offset(w * 0.40f, h * 0.84f), Size(w * 0.20f, h * 0.035f), CornerRadius(w * 0.01f))

        // Luna creciente grabada en la copa
        val centro = Offset(w * 0.5f, h * 0.31f)
        val r = w * 0.10f
        drawCircle(Colores.Crema, r, centro)
        drawCircle(Colores.Oro, r * 0.85f, centro + Offset(r * 0.45f, -r * 0.1f))
    }
}

@Composable
private fun Rayos() {
    val giro by rememberInfiniteTransition(label = "rayos").animateFloat(
        0f, 360f, infiniteRepeatable(tween(24_000, easing = LinearEasing)), label = "giro",
    )
    Canvas(Modifier.fillMaxSize()) {
        rotate(giro) {
            val radio = size.minDimension / 2
            for (i in 0 until 12) {
                val a = (i * 30.0) * PI / 180
                val b = (i * 30.0 + 12) * PI / 180
                val p = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x + radio * cos(a).toFloat(), center.y + radio * sin(a).toFloat())
                    lineTo(center.x + radio * cos(b).toFloat(), center.y + radio * sin(b).toFloat())
                    close()
                }
                drawPath(p, Colores.Oro.copy(alpha = 0.18f))
            }
        }
        drawCircle(Brush.radialGradient(listOf(Colores.Oro.copy(alpha = 0.35f), Color.Transparent)), size.minDimension / 2.6f)
    }
}

/** Papelitos que caen en loop mientras está abierta la ventana del trofeo recién ganado. */
@Composable
private fun Confeti() {
    val piezas = remember {
        val azar = Random(7)
        List(50) {
            Pieza(
                x = azar.nextFloat(), retraso = azar.nextFloat(), velocidad = 0.6f + azar.nextFloat() * 0.8f,
                vaiven = azar.nextFloat() * 6f, color = listOf(Colores.Oro, Colores.OroClaro, Colores.Rosa, Colores.Terracota, Colores.Crema)[it % 5],
            )
        }
    }
    val t by rememberInfiniteTransition(label = "confeti").animateFloat(
        0f, 1f, infiniteRepeatable(tween(4_000, easing = LinearEasing), RepeatMode.Restart), label = "t",
    )
    Canvas(Modifier.fillMaxSize()) { piezas.forEach { dibujarPieza(it, t) } }
}

private data class Pieza(val x: Float, val retraso: Float, val velocidad: Float, val vaiven: Float, val color: Color)

private fun DrawScope.dibujarPieza(p: Pieza, t: Float) {
    val avance = ((t * p.velocidad + p.retraso) % 1f)
    val y = avance * (size.height + 40f) - 20f
    val x = p.x * size.width + sin(avance * p.vaiven * 2 * PI).toFloat() * 24f
    rotate(avance * 720f * p.velocidad, Offset(x, y)) {
        drawRect(p.color, Offset(x - 6f, y - 3f), Size(12f, 6f))
    }
}
