package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

const val LARGO_PIN = 4

/**
 * Teclado numérico con puntos. Llama a [alCompletar] cuando se ingresan [LARGO_PIN] dígitos
 * y se vacía solo (el que lo usa decide si estuvo bien).
 */
@Composable
fun TecladoPin(
    titulo: String,
    mensaje: String?,
    alCompletar: (String) -> Unit,
    habilitado: Boolean = true,
    huella: (() -> Unit)? = null,
) {
    var pin by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Vino, textAlign = TextAlign.Center)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(LARGO_PIN) { i ->
                Box(
                    Modifier.size(16.dp).clip(CircleShape)
                        .background(if (i < pin.length) Colores.Terracota else Color.Transparent)
                        .border(2.dp, Colores.Terracota, CircleShape)
                )
            }
        }
        Text(
            mensaje ?: " ",
            style = MaterialTheme.typography.bodyMedium,
            color = Colores.TerracotaOscura,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        val filas = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("huella", "0", "borrar"))
        filas.forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                fila.forEach { tecla ->
                    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        when (tecla) {
                            "huella" -> if (huella != null) Tecla(alTocar = huella) {
                                Icon(Icons.Outlined.Fingerprint, "Usar huella", tint = Colores.Vino)
                            }
                            "borrar" -> Tecla(alTocar = { pin = pin.dropLast(1) }) {
                                Icon(Icons.AutoMirrored.Outlined.Backspace, "Borrar", tint = Colores.Vino)
                            }
                            else -> Tecla(alTocar = {
                                if (habilitado && pin.length < LARGO_PIN) {
                                    pin += tecla
                                    if (pin.length == LARGO_PIN) {
                                        val completo = pin
                                        pin = ""
                                        alCompletar(completo)
                                    }
                                }
                            }) {
                                Text(tecla, style = MaterialTheme.typography.headlineMedium, color = Colores.Texto)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tecla(alTocar: () -> Unit, contenido: @Composable () -> Unit) {
    Box(
        Modifier.size(72.dp).clip(CircleShape).background(Colores.Tarjeta).clickable(onClick = alTocar),
        contentAlignment = Alignment.Center,
    ) { contenido() }
}

/** Pantalla completa para elegir un PIN nuevo (lo pide dos veces). */
@Composable
fun CrearPin(alTerminar: (String) -> Unit, alCancelar: () -> Unit) {
    var primero by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = alCancelar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Colores.Crema) {
            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TecladoPin(
                    titulo = if (primero == null) "Elegí un PIN de $LARGO_PIN números" else "Repetilo para confirmar",
                    mensaje = mensaje,
                    alCompletar = { pin ->
                        when {
                            primero == null -> { primero = pin; mensaje = null }
                            pin == primero -> alTerminar(pin)
                            else -> { primero = null; mensaje = "No coinciden. Probá de nuevo." }
                        }
                    },
                )
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = alCancelar) { Text("Cancelar") }
            }
        }
    }
}
