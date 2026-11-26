package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.FinEmbarazo
import java.time.LocalDate

/** Elegir la etapa de vida. Pasar a embarazo pide la fecha; terminarlo pide cómo terminó. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaEtapa(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val actual = datos.ajustes.etapa
    var pedirFum by remember { mutableStateOf(false) }
    var pedirFpp by remember { mutableStateOf(false) }
    var elegirComo by remember { mutableStateOf(false) }
    var terminar by remember { mutableStateOf(false) }

    Subpantalla("Etapa de vida", alVolver) {
        Text(
            "Eirmena se adapta a lo que estás viviendo. Podés cambiarla cuando quieras: tus datos no se borran.",
            style = MaterialTheme.typography.bodyMedium,
        )
        EtapaVida.entries.forEach { e ->
            val elegida = e == actual
            Tarjeta(
                Modifier.clickable(enabled = !elegida) {
                    if (e == EtapaVida.EMBARAZO) elegirComo = true else vm.cambiarEtapa(e)
                },
                color = if (elegida) Colores.Terracota else Colores.Tarjeta,
            ) {
                Text(e.nombre, style = MaterialTheme.typography.titleMedium, color = if (elegida) Colores.Crema else Colores.Vino)
                Text(e.descripcion, style = MaterialTheme.typography.bodyMedium, color = if (elegida) Colores.Crema else Colores.Texto)
            }
        }
        if (actual == EtapaVida.EMBARAZO) {
            OutlinedButton(onClick = { terminar = true }, modifier = Modifier.fillMaxWidth()) { Text("Terminó el embarazo") }
        }
    }

    if (elegirComo) {
        AlertDialog(
            onDismissRequest = { elegirComo = false },
            title = { Text("¿Cómo calculamos las semanas?") },
            text = { Text("Con el primer día de tu última menstruación (FUM) o con la fecha probable de parto que te dieron en un control.") },
            confirmButton = { TextButton(onClick = { elegirComo = false; pedirFum = true }) { Text("Última menstruación") } },
            dismissButton = { TextButton(onClick = { elegirComo = false; pedirFpp = true }) { Text("Fecha probable de parto") } },
            containerColor = Colores.Tarjeta,
        )
    }
    if (pedirFum) {
        val sugerida = Ciclos.predecir(datos).ultimoInicio
        ElegirFecha(
            titulo = "Primer día de tu última menstruación",
            inicial = sugerida,
            alElegir = { vm.empezarEmbarazo(it); pedirFum = false },
            alCancelar = { pedirFum = false },
        )
    }
    if (pedirFpp) {
        ElegirFecha(
            titulo = "Fecha probable de parto",
            futuro = true,
            inicial = LocalDate.now().plusMonths(6),
            alElegir = { vm.empezarEmbarazo(it.minusDays(280)); pedirFpp = false },
            alCancelar = { pedirFpp = false },
        )
    }
    if (terminar) DialogoFinEmbarazo(alConfirmar = { fin, r -> vm.terminarEmbarazo(fin, r); terminar = false }, alCancelar = { terminar = false })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogoFinEmbarazo(alConfirmar: (LocalDate, FinEmbarazo) -> Unit, alCancelar: () -> Unit) {
    var resultado by remember { mutableStateOf<FinEmbarazo?>(null) }
    var fecha by remember { mutableStateOf(LocalDate.now()) }
    var elegirFecha by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Terminó el embarazo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Esto queda solo en tu teléfono y sirve para acompañarte en lo que sigue.", style = MaterialTheme.typography.bodyMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FinEmbarazo.entries.forEach { r -> Chip(r.nombre, resultado == r) { resultado = r } }
                }
                OutlinedButton(onClick = { elegirFecha = true }, modifier = Modifier.fillMaxWidth()) { Text("Fecha: ${Fechas.larga(fecha)}") }
            }
        },
        confirmButton = {
            TextButton(onClick = { resultado?.let { alConfirmar(fecha, it) } }, enabled = resultado != null) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } },
        containerColor = Colores.Tarjeta,
    )
    if (elegirFecha) ElegirFecha("¿Qué día?", alElegir = { fecha = it; elegirFecha = false }, alCancelar = { elegirFecha = false })
}
