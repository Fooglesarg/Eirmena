package com.byfooglesframes.eirmena.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Devuelve una función que abre el selector de archivos para importar un respaldo. */
@Composable
fun rememberImportador(vm: EirmenaViewModel): () -> Unit {
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::leerRespaldo)
    }
    return { lanzador.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")) }
}

/** Devuelve una función que pide la contraseña y después dónde guardar el respaldo. */
@Composable
fun rememberExportador(vm: EirmenaViewModel): () -> Unit {
    var pedir by remember { mutableStateOf(false) }
    var contrasena by remember { mutableStateOf<CharArray?>(null) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val c = contrasena
        contrasena = null
        if (uri != null && c != null) vm.exportar(uri, c) else c?.fill(' ')
    }
    if (pedir) {
        DialogoContrasenaNueva(
            alConfirmar = { c ->
                pedir = false
                contrasena = c
                lanzador.launch("Eirmena-respaldo-${LocalDate.now()}.json")
            },
            alCancelar = { pedir = false },
        )
    }
    return { pedir = true }
}

@Composable
private fun DialogoContrasenaNueva(alConfirmar: (CharArray) -> Unit, alCancelar: () -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    val error = when {
        a.isNotEmpty() && a.length < 6 -> "Usá al menos 6 caracteres."
        b.isNotEmpty() && a != b -> "Las contraseñas no coinciden."
        else -> null
    }
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Contraseña del respaldo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "El archivo queda cifrado: sin esta contraseña nadie puede leerlo, ni vos. " +
                        "Si la olvidás, no hay forma de recuperarlo.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                CampoContrasena(a, { a = it }, "Contraseña")
                CampoContrasena(b, { b = it }, "Repetir contraseña")
                error?.let { Text(it, color = Colores.TerracotaOscura, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = { alConfirmar(a.toCharArray()) }, enabled = a.length >= 6 && a == b) { Text("Exportar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } },
        containerColor = Colores.Tarjeta,
    )
}

@Composable
private fun CampoContrasena(valor: String, alCambiar: (String) -> Unit, etiqueta: String) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Diálogos de importación: contraseña del archivo y "reemplazar o combinar". */
@Composable
fun DialogosRespaldo(vm: EirmenaViewModel) {
    val pideContrasena by vm.pideContrasena.collectAsState()
    val importacion by vm.importacion.collectAsState()
    val trabajando by vm.trabajando.collectAsState()
    val alcance = rememberCoroutineScope()

    if (pideContrasena != null) {
        var c by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { if (!trabajando) vm.cancelarContrasena() },
            title = { Text("Abrir respaldo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ingresá la contraseña que elegiste al exportarlo.", style = MaterialTheme.typography.bodyMedium)
                    CampoContrasena(c, { c = it; error = null }, "Contraseña")
                    error?.let { Text(it, color = Colores.TerracotaOscura, style = MaterialTheme.typography.bodySmall) }
                    if (trabajando) CircularProgressIndicator(color = Colores.Terracota)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = c.isNotEmpty() && !trabajando,
                    onClick = { alcance.launch { error = vm.descifrarRespaldo(c.toCharArray()) } },
                ) { Text("Abrir") }
            },
            dismissButton = { TextButton(onClick = { vm.cancelarContrasena() }, enabled = !trabajando) { Text("Cancelar") } },
            containerColor = Colores.Tarjeta,
        )
    }

    importacion?.let { imp ->
        AlertDialog(
            onDismissRequest = { vm.cancelarImportacion() },
            title = { Text("Importar ${imp.dias} días") },
            text = {
                Text(
                    "Ya tenés datos en Eirmena. Podés combinarlos con el respaldo (si un día está en los dos, " +
                        "queda el del respaldo) o reemplazar todo por el respaldo."
                )
            },
            confirmButton = { TextButton(onClick = { vm.confirmarImportacion(combinar = true) }) { Text("Combinar") } },
            dismissButton = {
                TextButton(onClick = { vm.confirmarImportacion(combinar = false) }) {
                    Text("Reemplazar todo", color = Colores.TerracotaOscura)
                }
            },
            containerColor = Colores.Tarjeta,
        )
    }

    if (trabajando && pideContrasena == null) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Cifrando…") },
            text = { CircularProgressIndicator(color = Colores.Terracota) },
            containerColor = Colores.Tarjeta,
        )
    }
}
