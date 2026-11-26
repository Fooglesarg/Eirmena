package com.byfooglesframes.eirmena.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.avisos.Planificador
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

enum class Seccion(val titulo: String, val icono: ImageVector) {
    HOY("Hoy", Icons.Outlined.WbTwilight),
    CALENDARIO("Calendario", Icons.Outlined.CalendarMonth),
    CICLOS("Ciclos", Icons.Outlined.Timeline),
    APRENDER("Aprender", Icons.Outlined.AutoStories),
    AJUSTES("Ajustes", Icons.Outlined.Settings),
}

enum class Sub { SABIAS, ACERCA, ANTICONCEPTIVO, AVISOS, HORMONAS, RETRASO, SENALES, ARTICULO, ETAPA, EMBARAZO, FERTILIDAD, ESTADISTICAS, INFORME, BIENESTAR }

/** En Android 13 o más, la primera vez que se entra se pide permiso para notificar. */
@Composable
private fun PedirNotificacionesUnaVez() {
    val contexto = LocalContext.current
    val pedir = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        val prefs = contexto.getSharedPreferences("avisos", Context.MODE_PRIVATE)
        if (Build.VERSION.SDK_INT >= 33 && !prefs.getBoolean("permisoPedido", false) && !Planificador.puedeNotificar(contexto)) {
            prefs.edit().putBoolean("permisoPedido", true).apply()
            pedir.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun App(vm: EirmenaViewModel, huellaDisponible: Boolean, pedirHuella: (() -> Unit) -> Unit) {
    val cargado by vm.cargado.collectAsState()
    val datos by vm.datos.collectAsState()
    val bloqueada by vm.bloqueada.collectAsState()
    if (!cargado) {
        Box(Modifier.fillMaxSize())
        return
    }

    val snackbar = remember { SnackbarHostState() }
    val mensaje by vm.mensaje.collectAsState()
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbar.showSnackbar(it)
            vm.mensajeVisto()
        }
    }

    val principal = !bloqueada && datos.ajustes.bienvenidaHecha
    Box(Modifier.fillMaxSize()) {
        when {
            bloqueada -> PantallaBloqueo(vm, huellaDisponible && vm.bloqueo.usarHuella, pedirHuella)
            !datos.ajustes.bienvenidaHecha -> PantallaBienvenida(vm)
            else -> Principal(vm, huellaDisponible, pedirHuella)
        }
        // Los avisos se muestran en cualquier pantalla (también al importar desde la bienvenida).
        SnackbarHost(
            snackbar,
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = if (principal) 80.dp else 16.dp),
        )
    }
    DialogosRespaldo(vm)
}

@Composable
private fun Principal(vm: EirmenaViewModel, huellaDisponible: Boolean, pedirHuella: (() -> Unit) -> Unit) {
    var seccion by rememberSaveable { mutableStateOf(Seccion.HOY) }
    var diaAbierto by remember { mutableStateOf<LocalDate?>(null) }
    // Pantallas secundarias apiladas (p. ej. Avisos → Anticonceptivo); volver saca la de arriba.
    var pila by rememberSaveable { mutableStateOf(listOf<String>()) }
    /** Abre una pantalla secundaria; los artículos van como "ARTICULO:id". */
    fun abrirTexto(destino: String) { pila = pila + destino }
    fun abrir(sub: Sub) = abrirTexto(sub.name)
    fun abrirArticulo(id: String) = abrirTexto("${Sub.ARTICULO.name}:$id")
    fun volver() { pila = pila.dropLast(1) }

    PedirNotificacionesUnaVez()

    pila.lastOrNull()?.let { arriba ->
        BackHandler { volver() }
        when (Sub.valueOf(arriba.substringBefore(':'))) {
            Sub.SABIAS -> PantallaSabias(vm, alVolver = ::volver)
            Sub.ACERCA -> PantallaAcerca(alVolver = ::volver)
            Sub.ANTICONCEPTIVO -> PantallaAnticonceptivo(vm, alVolver = ::volver)
            Sub.AVISOS -> PantallaAvisos(vm, alVolver = ::volver, abrirAnticonceptivo = { abrir(Sub.ANTICONCEPTIVO) })
            Sub.HORMONAS -> PantallaHormonas(vm, alVolver = ::volver)
            Sub.RETRASO -> PantallaRetraso(vm, alVolver = ::volver, abrirArticulo = ::abrirArticulo)
            Sub.SENALES -> PantallaSenales(vm, alVolver = ::volver, abrirArticulo = ::abrirArticulo)
            Sub.ARTICULO -> PantallaArticulo(arriba.substringAfter(':'), alVolver = ::volver)
            Sub.ETAPA -> PantallaEtapa(vm, alVolver = ::volver)
            Sub.EMBARAZO -> PantallaEmbarazo(vm, alVolver = ::volver, abrirArticulo = ::abrirArticulo)
            Sub.ESTADISTICAS -> PantallaEstadisticas(vm, alVolver = ::volver, abrirInforme = { abrir(Sub.INFORME) })
            Sub.INFORME -> PantallaInforme(vm, alVolver = ::volver)
            Sub.BIENESTAR -> PantallaBienestar(vm, alVolver = ::volver)
            Sub.FERTILIDAD -> PantallaFertilidad(vm, alVolver = ::volver, abrirDia = { diaAbierto = it }, abrirArticulo = ::abrirArticulo)
        }
        diaAbierto?.let { fecha -> HojaDia(vm, fecha, alCerrar = { diaAbierto = null }) }
        return
    }
    BackHandler(enabled = seccion != Seccion.HOY) { seccion = Seccion.HOY }

    Scaffold(
        containerColor = Colores.Crema,
        bottomBar = {
            NavigationBar(containerColor = Colores.Tarjeta) {
                Seccion.entries.forEach { s ->
                    NavigationBarItem(
                        selected = s == seccion,
                        onClick = { seccion = s },
                        icon = { Icon(s.icono, contentDescription = null) },
                        label = { Text(s.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Colores.Vino,
                            selectedTextColor = Colores.Vino,
                            indicatorColor = Colores.RosaClaro,
                            unselectedIconColor = Colores.TextoSuave,
                            unselectedTextColor = Colores.TextoSuave,
                        ),
                    )
                }
            }
        },
    ) { relleno ->
        AnimatedContent(
            targetState = seccion,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.padding(relleno),
            label = "seccion",
        ) { s ->
            when (s) {
                Seccion.HOY -> PantallaHoy(vm, abrirDia = { diaAbierto = it }, abrir = ::abrir, abrirArticulo = ::abrirArticulo)
                Seccion.CALENDARIO -> PantallaCalendario(vm, abrirDia = { diaAbierto = it })
                Seccion.CICLOS -> PantallaCiclos(vm, abrir = ::abrir)
                Seccion.APRENDER -> PantallaAprender(vm, abrir = ::abrirTexto)
                Seccion.AJUSTES -> PantallaAjustes(vm, huellaDisponible, pedirHuella, abrir = ::abrir)
            }
        }
    }

    diaAbierto?.let { fecha -> HojaDia(vm, fecha, alCerrar = { diaAbierto = null }) }
}
