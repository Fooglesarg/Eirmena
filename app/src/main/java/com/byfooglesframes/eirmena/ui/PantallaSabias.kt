package com.byfooglesframes.eirmena.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.datos.Curiosidad
import com.byfooglesframes.eirmena.datos.Curiosidades

/**
 * Minijuego "¿Sabías que…?": cada vez que se entra sale una carta al azar, boca abajo, y puede ser repetida.
 * Al darla vuelta queda en la colección; la siguiente se sortea para la próxima visita.
 * Con el trofeo ganado se entra directo a la colección.
 */
@Composable
fun PantallaSabias(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val vistas = datos.ajustes.curiosidades
    // La carta de esta visita se fija al entrar y no cambia aunque se dé vuelta (ahí se sortea la próxima).
    val idDeHoy = rememberSaveable {
        if (datos.ajustes.trofeo != null) "" else (datos.ajustes.proximaCarta ?: Curiosidades.sortear(null).id)
    }
    val carta = Curiosidades.porId(idDeHoy)
    val repetida = rememberSaveable { idDeHoy in vistas }
    var dadaVuelta by rememberSaveable { mutableStateOf(false) }
    var coleccion by rememberSaveable { mutableStateOf(carta == null) }
    var abierta by remember { mutableStateOf<Curiosidad?>(null) }
    val trofeo = datos.ajustes.trofeo
    // Si el trofeo se gana en esta visita, se festeja solo, un ratito después de dar vuelta la carta.
    val teniaTrofeo = rememberSaveable { trofeo != null }
    var verTrofeo by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(trofeo) {
        if (trofeo != null && !teniaTrofeo) {
            delay(1400)
            verTrofeo = true
        }
    }

    BackHandler {
        if (coleccion && carta != null) coleccion = false else alVolver()
    }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
            Text(if (coleccion) "Tu colección" else "¿Sabías que…?", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Progreso(vistas.count { id -> Curiosidades.todas.any { it.id == id } }, Curiosidades.todas.size)

            if (coleccion) {
                FilaTrofeo(trofeo, Curiosidades.todas.count { it.id !in vistas }, alTocar = { verTrofeo = true })
                if (carta == null) {
                    Text(
                        "Cuando Eirmena se actualice pueden aparecer cartas nuevas.",
                        style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave,
                    )
                }
                Curiosidades.todas.forEachIndexed { i, c ->
                    FilaColeccion(i + 1, c, descubierta = c.id in vistas, alTocar = { abierta = c })
                }
                if (carta != null) {
                    OutlinedButton(onClick = { coleccion = false }, Modifier.fillMaxWidth()) { Text("Volver a la carta de hoy") }
                }
            } else if (carta != null) {
                CartaGiratoria(carta, dadaVuelta, repetida, alDescubrir = { dadaVuelta = true; vm.descubrir(carta.id) })
                if (dadaVuelta && trofeo != null && !teniaTrofeo) {
                    Button(onClick = { verTrofeo = true }, Modifier.fillMaxWidth()) { Text("¡Completaste la colección! Ver tu trofeo") }
                } else if (dadaVuelta) {
                    Text(
                        if (repetida) "Repetida: esta ya la tenías. Probá suerte en otro momento."
                        else "¡Carta nueva para tu colección! Volvé en otro momento para sacar otra.",
                        Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Colores.TextoSuave,
                        textAlign = TextAlign.Center,
                    )
                }
                Button(onClick = { coleccion = true }, Modifier.fillMaxWidth()) { Text("Ver mi colección") }
            }
        }
    }

    if (verTrofeo && trofeo != null) {
        VentanaTrofeo(trofeo, recienGanado = !teniaTrofeo, alCerrar = { verTrofeo = false; coleccion = true })
    }

    abierta?.let { c ->
        Dialog(onDismissRequest = { abierta = null }) {
            Frente(c, Modifier.clickable { abierta = null })
        }
    }
}

@Composable
private fun Progreso(vistas: Int, total: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Descubriste $vistas de $total", style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)
        LinearProgressIndicator(
            progress = { vistas.toFloat() / total },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
            color = Colores.Terracota,
            trackColor = Colores.CremaOscura,
            drawStopIndicator = {},
        )
    }
}

@Composable
private fun CartaGiratoria(carta: Curiosidad, dadaVuelta: Boolean, repetida: Boolean, alDescubrir: () -> Unit) {
    val giro by animateFloatAsState(if (dadaVuelta) 180f else 0f, tween(700), label = "giro")

    Box(
        Modifier.fillMaxWidth()
            .graphicsLayer { rotationY = giro; cameraDistance = 14f * density }
            .clickable(enabled = !dadaVuelta, onClick = alDescubrir),
    ) {
        if (giro <= 90f) {
            Dorso(carta)
        } else {
            // Se espeja para que el texto no quede al revés.
            Frente(carta, Modifier.graphicsLayer { rotationY = 180f }, if (repetida) "REPETIDA" else "¡NUEVA!")
        }
    }
}

/** La carta boca abajo: solo muestra la pista. */
@Composable
private fun Dorso(carta: Curiosidad) {
    Column(
        Modifier.fillMaxWidth().heightIn(min = 420.dp).clip(RoundedCornerShape(28.dp)).background(Colores.Terracota).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        Logo(tamano = 88)
        Text("¿Sabías que…", style = MaterialTheme.typography.headlineMedium, color = Colores.Crema, textAlign = TextAlign.Center)
        Text(carta.pista.removePrefix("…"), style = MaterialTheme.typography.titleMedium, color = Colores.Crema, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Tocá la carta para descubrirlo", style = MaterialTheme.typography.labelMedium, color = Colores.RosaClaro)
    }
}

/** La carta descubierta. */
@Composable
private fun Frente(carta: Curiosidad, modifier: Modifier = Modifier, sello: String? = null) {
    Column(
        modifier.fillMaxWidth().heightIn(min = 420.dp).clip(RoundedCornerShape(28.dp)).background(Colores.Tarjeta).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                carta.cultura.uppercase(),
                Modifier.clip(RoundedCornerShape(50)).background(Colores.RosaClaro).padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Colores.Vino,
            )
            sello?.let {
                Text(
                    it,
                    Modifier.clip(RoundedCornerShape(50))
                        .background(if (it == "REPETIDA") Colores.CremaOscura else Colores.Terracota)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (it == "REPETIDA") Colores.TextoSuave else Color.White,
                )
            }
        }
        Text(carta.titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        Text(carta.texto, style = MaterialTheme.typography.bodyLarge)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Colores.CremaOscura).padding(14.dp)) {
            Text(carta.conexion, style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura)
        }
    }
}

@Composable
private fun FilaColeccion(numero: Int, c: Curiosidad, descubierta: Boolean, alTocar: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(if (descubierta) Colores.Tarjeta else Colores.CremaOscura)
            .clickable(enabled = descubierta, onClick = alTocar)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.clip(RoundedCornerShape(12.dp)).background(if (descubierta) Colores.Terracota else Colores.Linea)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text("$numero", style = MaterialTheme.typography.labelLarge, color = if (descubierta) Color.White else Colores.TextoSuave)
        }
        Column(Modifier.weight(1f)) {
            if (descubierta) {
                Text(c.titulo, style = MaterialTheme.typography.titleMedium)
                Text(c.cultura, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
            } else {
                Text("Carta sin descubrir", style = MaterialTheme.typography.titleMedium, color = Colores.TextoSuave)
            }
        }
        if (!descubierta) Icon(Icons.Outlined.Lock, null, tint = Colores.TextoSuave)
    }
}
