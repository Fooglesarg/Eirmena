package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalTime

/** Pantalla secundaria con flecha de volver y contenido desplazable. */
@Composable
fun Subpantalla(titulo: String, alVolver: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Volver") }
            Text(titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = contenido,
        )
    }
}

@Composable
fun Interruptor(titulo: String, detalle: String?, valor: Boolean, habilitado: Boolean = true, alCambiar: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, color = if (habilitado) Colores.Texto else Colores.TextoSuave)
            detalle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave) }
        }
        Switch(
            checked = valor,
            onCheckedChange = alCambiar,
            enabled = habilitado,
            colors = SwitchDefaults.colors(checkedTrackColor = Colores.Terracota),
        )
    }
}

@Composable
fun Chip(texto: String, elegido: Boolean, alTocar: () -> Unit) {
    FilterChip(
        selected = elegido,
        onClick = alTocar,
        label = { Text(texto) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Colores.Terracota,
            selectedLabelColor = Color.White,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirHora(inicial: LocalTime, alElegir: (LocalTime) -> Unit, alCancelar: () -> Unit) {
    val estado = rememberTimePickerState(inicial.hour, inicial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = alCancelar,
        confirmButton = { TextButton(onClick = { alElegir(LocalTime.of(estado.hour, estado.minute)) }) { Text("Listo") } },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } },
        text = {
            TimePicker(
                estado,
                colors = TimePickerDefaults.colors(
                    selectorColor = Colores.Terracota,
                    timeSelectorSelectedContainerColor = Colores.RosaClaro,
                    clockDialColor = Colores.CremaOscura,
                ),
            )
        },
        containerColor = Colores.Tarjeta,
    )
}

fun textoHora(h: LocalTime): String = "%02d:%02d".format(h.hour, h.minute)
