package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.BuildConfig
import com.byfooglesframes.eirmena.EirmenaViewModel
import kotlinx.coroutines.launch

@Composable
fun PantallaAjustes(
    vm: EirmenaViewModel,
    huellaDisponible: Boolean,
    pedirHuella: (() -> Unit) -> Unit,
    abrir: (Sub) -> Unit,
) {
    val datos by vm.datos.collectAsState()
    val ajustes = datos.ajustes
    val alcance = rememberCoroutineScope()
    var tienePin by remember { mutableStateOf(vm.bloqueo.tienePin) }
    var huella by remember { mutableStateOf(vm.bloqueo.usarHuella) }
    var crearPin by remember { mutableStateOf(false) }
    var confirmarBorrar by remember { mutableStateOf(false) }
    val exportar = rememberExportador(vm)
    val importar = rememberImportador(vm)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)

        Seccion("Privacidad") {
            if (!tienePin) {
                Fila("Bloquear con PIN", "Pide un PIN de $LARGO_PIN números al abrir la app.", alTocar = { crearPin = true })
            } else {
                Fila("Cambiar PIN", null, alTocar = { crearPin = true })
                Fila("Quitar PIN", null, alTocar = {
                    vm.quitarPin(); tienePin = false; huella = false
                    vm.avisar("Se quitó el PIN.")
                })
                if (huellaDisponible) {
                    Interruptor("Desbloquear con huella o rostro", null, huella) { activar ->
                        if (activar) pedirHuella { vm.bloqueo.usarHuella = true; huella = true }
                        else { vm.bloqueo.usarHuella = false; huella = false }
                    }
                }
            }
            Interruptor(
                "Ocultar en apps recientes",
                "Tapa el contenido en la vista de apps abiertas y bloquea las capturas de pantalla.",
                ajustes.ocultarEnRecientes,
            ) { v -> vm.cambiarAjustes { it.copy(ocultarEnRecientes = v) } }
        }

        Seccion("Tu ciclo") {
            Fila("Etapa de vida", ajustes.etapa.nombre, alTocar = { abrir(Sub.ETAPA) })
            Contador("Duración habitual del ciclo", ajustes.cicloHabitual, 21..45) { v -> vm.cambiarAjustes { it.copy(cicloHabitual = v) } }
            Contador("Duración habitual del período", ajustes.periodoHabitual, 2..10) { v -> vm.cambiarAjustes { it.copy(periodoHabitual = v) } }
            Text(
                "Se usan mientras tengas menos de 3 ciclos anotados. Después manda tu historial.",
                style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
            )
        }

        Seccion("Anticonceptivo y avisos") {
            Fila("Método anticonceptivo", ajustes.anticonceptivo.metodo.nombre, alTocar = { abrir(Sub.ANTICONCEPTIVO) })
            Fila("Avisos", "Período, anticonceptivo y registro diario.", alTocar = { abrir(Sub.AVISOS) })
        }

        Seccion("Respaldo") {
            Text(
                "Tus datos solo existen en este teléfono. Si lo cambiás, lo formateás o desinstalás la app, " +
                    "se pierden, salvo que exportes un respaldo y lo guardes en otro lado.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Fila("Exportar respaldo", "Archivo .json cifrado con una contraseña.", alTocar = exportar)
            Fila("Importar respaldo", "Recuperar tu historial desde un archivo.", alTocar = importar)
        }

        Seccion("Datos") {
            Fila("Borrar todos los datos", null, color = Colores.TerracotaOscura, alTocar = { confirmarBorrar = true })
        }

        Seccion("Eirmena") {
            Fila("¿Sabías que…?", "Cartas de mitología sobre el ciclo, la sanación y el cuerpo.", alTocar = { abrir(Sub.SABIAS) })
            Fila("Acerca de Eirmena", "Privacidad, licencia y fuentes.", alTocar = { abrir(Sub.ACERCA) })
        }

        Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Eirmena ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
            Firma()
        }
    }

    if (crearPin) {
        CrearPin(
            alTerminar = { pin ->
                crearPin = false
                alcance.launch {
                    vm.ponerPin(pin)
                    tienePin = true
                    vm.avisar("PIN guardado. Si lo olvidás, no se puede recuperar.")
                }
            },
            alCancelar = { crearPin = false },
        )
    }

    if (confirmarBorrar) {
        AlertDialog(
            onDismissRequest = { confirmarBorrar = false },
            title = { Text("¿Borrar todo?") },
            text = {
                Text(
                    "Se borran todos tus días anotados, los ajustes y el PIN. No se puede deshacer. " +
                        if (datos.dias.isNotEmpty()) "Si querés conservar tu historial, exportá un respaldo antes." else ""
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmarBorrar = false; vm.borrarTodo() }) {
                    Text("Borrar todo", color = Colores.TerracotaOscura)
                }
            },
            dismissButton = { TextButton(onClick = { confirmarBorrar = false }) { Text("Cancelar") } },
            containerColor = Colores.Tarjeta,
        )
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Tarjeta { contenido() }
    }
}

@Composable
private fun Fila(titulo: String, detalle: String?, color: Color = Colores.Texto, alTocar: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = alTocar).padding(vertical = 6.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyLarge, color = color)
        detalle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave) }
    }
}

@Composable
fun Contador(titulo: String, valor: Int, limites: IntRange, alCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = { alCambiar((valor - 1).coerceIn(limites)) }, enabled = valor > limites.first) {
            Icon(Icons.Outlined.Remove, "Menos")
        }
        Text("$valor días", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        IconButton(onClick = { alCambiar((valor + 1).coerceIn(limites)) }, enabled = valor < limites.last) {
            Icon(Icons.Outlined.Add, "Más")
        }
    }
}
