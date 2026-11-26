package com.byfooglesframes.eirmena.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.avisos.Planificador
import com.byfooglesframes.eirmena.datos.Avisos
import com.byfooglesframes.eirmena.datos.Metodo

@Composable
fun PantallaAvisos(vm: EirmenaViewModel, alVolver: () -> Unit, abrirAnticonceptivo: () -> Unit) {
    val contexto = LocalContext.current
    val datos by vm.datos.collectAsState()
    val av = datos.ajustes.avisos
    val metodo = datos.ajustes.anticonceptivo
    var elegirHora by remember { mutableStateOf(false) }
    fun cambiar(t: (Avisos) -> Avisos) = vm.cambiarAjustes { it.copy(avisos = t(it.avisos)) }

    // Se vuelve a mirar el estado de los permisos al volver de los ajustes de Android.
    var refresco by remember { mutableIntStateOf(0) }
    val ciclo = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(ciclo) {
        val o = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_RESUME) refresco++ }
        ciclo.addObserver(o)
        onDispose { ciclo.removeObserver(o) }
    }
    val puedeNotificar = remember(refresco) { Planificador.puedeNotificar(contexto) }
    val exactas = remember(refresco) { Planificador.puedeAlarmasExactas(contexto) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresco++ }

    Subpantalla("Avisos", alVolver) {
        if (!puedeNotificar) {
            Tarjeta(color = Colores.RosaClaro) {
                Text("Las notificaciones están desactivadas", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text("Sin este permiso Eirmena no puede avisarte nada.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= 33) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else abrirAjustesNotificaciones(contexto)
                }, modifier = Modifier.fillMaxWidth()) { Text("Permitir notificaciones") }
                androidx.compose.material3.TextButton(onClick = { abrirAjustesNotificaciones(contexto) }) {
                    Text("Abrir ajustes de Android")
                }
            }
        }

        Tarjeta {
            Interruptor("Período", "Dos días antes de la fecha estimada.", av.periodo) { v -> cambiar { it.copy(periodo = v) } }
            Interruptor(
                "Ventana fértil",
                if (metodo.metodo.suprimeOvulacion) "No aplica con tu método anticonceptivo." else "El día que empieza la ventana fértil estimada.",
                av.fertil && !metodo.metodo.suprimeOvulacion,
                habilitado = !metodo.metodo.suprimeOvulacion,
            ) { v -> cambiar { it.copy(fertil = v) } }
            Interruptor(
                "Anticonceptivo",
                when (metodo.metodo) {
                    Metodo.PASTILLA -> "Todos los días a las ${textoHora(metodo.hora)}."
                    Metodo.PARCHE, Metodo.ANILLO -> "Los días de cambio, a las ${textoHora(metodo.hora)}."
                    Metodo.INYECCION -> "Dos días antes y el día de la inyección."
                    Metodo.IMPLANTE, Metodo.DIU_HORMONAL, Metodo.DIU_COBRE -> "30 días antes del vencimiento."
                    else -> "Elegí tu método para activarlo."
                },
                av.anticonceptivo,
            ) { v -> cambiar { it.copy(anticonceptivo = v) } }
            androidx.compose.material3.TextButton(onClick = abrirAnticonceptivo) { Text("Configurar método") }
            Interruptor(
                "Registro diario",
                "Un recordatorio para anotar cómo te sentiste, a las ${textoHora(av.horaRegistro)}.",
                av.registroDiario,
            ) { v -> cambiar { it.copy(registroDiario = v) } }
            if (av.registroDiario) {
                OutlinedButton(onClick = { elegirHora = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cambiar hora: ${textoHora(av.horaRegistro)}")
                }
            }
        }

        Tarjeta {
            Interruptor(
                "Avisos discretos",
                "La notificación solo dice \"Eirmena: tenés un recordatorio\", sin hablar del ciclo ni del método. " +
                    "Con el teléfono bloqueado siempre se ve así.",
                av.discretos,
            ) { v -> cambiar { it.copy(discretos = v) } }
        }

        if (metodo.metodo == Metodo.PASTILLA && av.anticonceptivo && !exactas) {
            Tarjeta(color = Colores.CremaOscura) {
                Text("Hora exacta para la pastilla", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Android puede demorar el aviso unos minutos para ahorrar batería. Si querés que llegue en el minuto exacto, " +
                        "permití las alarmas exactas para Eirmena.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= 31) {
                        contexto.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${contexto.packageName}"))
                        )
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Permitir alarmas exactas") }
            }
        }

        OutlinedButton(
            onClick = { Planificador.probar(contexto, datos) },
            enabled = puedeNotificar,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Probar un aviso") }

        Text(
            "Los avisos se generan en el teléfono, sin internet. Si apagás el teléfono, al prenderlo se reprograman solos.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }

    if (elegirHora) {
        ElegirHora(av.horaRegistro, alElegir = { h -> cambiar { it.copy(horaRegistro = h) }; elegirHora = false }, alCancelar = { elegirHora = false })
    }
}

private fun abrirAjustesNotificaciones(contexto: android.content.Context) {
    contexto.startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, contexto.packageName)
    )
}
