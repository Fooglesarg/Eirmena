package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.BuildConfig

@Composable
fun PantallaAcerca(alVolver: () -> Unit) {
    var abierta by remember { mutableStateOf<Pair<String, String>?>(null) }
    abierta?.let { (titulo, archivo) -> VentanaLicencia(titulo, archivo, alCerrar = { abierta = null }) }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
            Text("Acerca de Eirmena", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Logo(Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp), tamano = 96)

            Bloque(
                "Privacidad",
                "Eirmena no tiene permiso de internet: es técnicamente imposible que envíe tus datos a algún lado. " +
                    "No hay cuentas, servidores, publicidad ni estadísticas de uso. Tus datos se guardan cifrados en " +
                    "este teléfono y Android no los copia a la nube. Solo salen si exportás un respaldo, que también va cifrado.",
            )
            Bloque(
                "Aviso médico",
                "Eirmena es una herramienta de registro y de información general. Las fechas de período, ovulación y " +
                    "ventana fértil son estimaciones estadísticas: no es un método anticonceptivo, no diagnostica " +
                    "enfermedades y no reemplaza la consulta con un profesional de la salud.",
            )
            Bloque(
                "Gratis y de código abierto",
                "Eirmena no tiene fines de lucro ni versión paga: todo es gratis para todas las personas. " +
                    "Se publica bajo la licencia GPL-3.0: cualquiera puede leer el código, usarlo y modificarlo, " +
                    "siempre que las versiones modificadas también sean libres y mantengan la autoría original.\n\n" +
                    "Código fuente: github.com/Fooglesarg/Eirmena",
            )
            Bloque(
                "Fuentes",
                "La información de salud se basa en la Organización Mundial de la Salud (OMS), el Colegio Americano de " +
                    "Obstetras y Ginecólogos (ACOG), el Servicio Nacional de Salud del Reino Unido (NHS) y el Ministerio " +
                    "de Salud de la Argentina.",
            )

            Tarjeta {
                Text("Licencias", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text(
                    "Eirmena se distribuye bajo la GPL-3.0. Usa las tipografías Comfortaa y Outfit, sin modificar, " +
                        "bajo la SIL Open Font License 1.1.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                LICENCIAS.forEach { (titulo, archivo) ->
                    TextButton(onClick = { abierta = titulo to archivo }) { Text(titulo) }
                }
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Eirmena ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
                Firma()
            }
        }
    }
}

/** Textos completos de las licencias, en assets/licencias. */
private val LICENCIAS = listOf(
    "Eirmena · GNU GPL 3.0" to "eirmena-gpl3.txt",
    "Comfortaa · SIL OFL 1.1" to "comfortaa.txt",
    "Outfit · SIL OFL 1.1" to "outfit.txt",
)

@Composable
private fun VentanaLicencia(titulo: String, archivo: String, alCerrar: () -> Unit) {
    val contexto = LocalContext.current
    val texto = remember(archivo) {
        try { contexto.assets.open("licencias/$archivo").bufferedReader().use { it.readText() } }
        catch (e: Exception) { "No se pudo abrir la licencia." }
    }
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(titulo) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                Text(texto, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = alCerrar) { Text("Cerrar") } },
        containerColor = Colores.Tarjeta,
    )
}

@Composable
private fun Bloque(titulo: String, texto: String) {
    Tarjeta {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
