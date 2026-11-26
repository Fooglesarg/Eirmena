package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import kotlinx.coroutines.launch

@Composable
fun PantallaBloqueo(vm: EirmenaViewModel, conHuella: Boolean, pedirHuella: (() -> Unit) -> Unit) {
    val alcance = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf<String?>(null) }
    var probando by remember { mutableStateOf(false) }
    var olvide by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { if (conHuella) pedirHuella { vm.desbloquearConHuella() } }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Eirmena", style = MaterialTheme.typography.displayLarge, color = Colores.Vino)
        Spacer(Modifier.height(32.dp))
        TecladoPin(
            titulo = "Ingresá tu PIN",
            mensaje = mensaje,
            habilitado = !probando,
            huella = if (conHuella) ({ pedirHuella { vm.desbloquearConHuella() } }) else null,
            alCompletar = { pin ->
                probando = true
                alcance.launch {
                    val espera = vm.bloqueo.esperarHasta - System.currentTimeMillis()
                    mensaje = when {
                        espera > 0 -> "Demasiados intentos. Esperá ${espera / 1000 + 1} segundos."
                        vm.probarPin(pin) -> null
                        else -> "PIN incorrecto."
                    }
                    probando = false
                }
            },
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { olvide = true }) { Text("Olvidé mi PIN") }
    }

    if (olvide) {
        AlertDialog(
            onDismissRequest = { olvide = false },
            title = { Text("¿Olvidaste tu PIN?") },
            text = {
                Text(
                    "Eirmena no tiene servidores, así que nadie puede recuperar tu PIN. La única salida es borrar " +
                        "todos los datos de la app y empezar de nuevo. Si tenés un respaldo exportado, después lo podés importar."
                )
            },
            confirmButton = {
                TextButton(onClick = { olvide = false; vm.borrarTodo() }) { Text("Borrar todo", color = Colores.TerracotaOscura) }
            },
            dismissButton = { TextButton(onClick = { olvide = false }) { Text("Cancelar") } },
            containerColor = Colores.Tarjeta,
        )
    }
}
