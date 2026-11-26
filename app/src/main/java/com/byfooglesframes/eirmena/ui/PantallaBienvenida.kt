package com.byfooglesframes.eirmena.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.R
import com.byfooglesframes.eirmena.datos.EtapaVida
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun PantallaBienvenida(vm: EirmenaViewModel) {
    var paso by rememberSaveable { mutableStateOf(0) }
    var ciclo by rememberSaveable { mutableStateOf(28) }
    var periodo by rememberSaveable { mutableStateOf(5) }
    var ultimo by rememberSaveable { mutableStateOf<String?>(null) }
    var etapa by rememberSaveable { mutableStateOf(EtapaVida.CICLO) }
    var elegirFecha by rememberSaveable { mutableStateOf(false) }
    var crearPin by rememberSaveable { mutableStateOf(false) }
    val alcance = rememberCoroutineScope()
    val importar = rememberImportador(vm)

    fun terminar() = vm.terminarBienvenida(ciclo, periodo, ultimo?.let(LocalDate::parse), etapa)

    BackHandler(enabled = paso > 0) { paso-- }

    AnimatedContent(
        targetState = paso,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        label = "paso",
    ) { p ->
        when (p) {
            0 -> Paso(
                contenido = {
                    Spacer(Modifier.height(24.dp))
                    Logo(Modifier.align(Alignment.CenterHorizontally))
                    Text(
                        "Eirmena", Modifier.fillMaxWidth(), style = MaterialTheme.typography.displayLarge,
                        color = Colores.Vino, textAlign = TextAlign.Center,
                    )
                    Text(
                        "Tu ciclo, tus datos, tu teléfono.", Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Tarjeta {
                        Text(
                            "Una app para acompañarte a entender tu cuerpo, gratis y sin pedirte nada a cambio.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            "¿De dónde viene el nombre? Lo vas a descubrir en \"¿Sabías que…?\".",
                            style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave,
                        )
                    }
                },
                botones = {
                    Button(onClick = { paso = 1 }, Modifier.fillMaxWidth().height(52.dp)) { Text("Empezar") }
                    TextButton(onClick = importar, Modifier.fillMaxWidth()) { Text("Ya usaba Eirmena: importar respaldo") }
                    Firma(Modifier.align(Alignment.CenterHorizontally))
                },
            )
            1 -> Paso(
                contenido = {
                    Text("Antes de empezar", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)
                    Punto("Sin internet", "Eirmena no tiene permiso para conectarse a internet. No hay cuentas, servidores, publicidad ni estadísticas de uso.")
                    Punto("Tus datos son tuyos", "Todo queda cifrado en este teléfono. Solo salen si vos exportás un respaldo.")
                    Punto("Hacé respaldos", "Si cambiás o formateás el teléfono, exportá un respaldo desde Ajustes para no perder tu historial.")
                    Punto("No es un consejo médico", "Las fechas son estimaciones. Eirmena no es un método anticonceptivo ni reemplaza la consulta con un profesional de la salud.")
                },
                botones = { Button(onClick = { paso = 2 }, Modifier.fillMaxWidth().height(52.dp)) { Text("Entendido") } },
            )
            2 -> Paso(
                contenido = {
                    Text("¿En qué etapa estás?", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)
                    Text("Eirmena se adapta. La podés cambiar cuando quieras en Ajustes.", style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)
                    EtapaVida.entries.forEach { e ->
                        val elegida = e == etapa
                        Tarjeta(Modifier.clickable { etapa = e }, color = if (elegida) Colores.Terracota else Colores.Tarjeta) {
                            Text(e.nombre, style = MaterialTheme.typography.titleMedium, color = if (elegida) Colores.Crema else Colores.Vino)
                            Text(e.descripcion, style = MaterialTheme.typography.bodyMedium, color = if (elegida) Colores.Crema else Colores.Texto)
                        }
                    }
                },
                botones = { Button(onClick = { paso = 3 }, Modifier.fillMaxWidth().height(52.dp)) { Text("Seguir") } },
            )
            3 -> Paso(
                contenido = {
                    Text(
                        when (etapa) {
                            EtapaVida.EMBARAZO -> "Contame de tu embarazo"
                            EtapaVida.PRIMEROS_CICLOS -> "¿Ya te vino alguna vez?"
                            else -> "Contame de tu ciclo"
                        },
                        style = MaterialTheme.typography.headlineMedium, color = Colores.Vino,
                    )
                    Text(
                        when (etapa) {
                            EtapaVida.EMBARAZO -> "Las semanas se cuentan desde el primer día de tu última menstruación (FUM). Es el dato que te piden en los controles."
                            EtapaVida.PRIMEROS_CICLOS -> "Si todavía no te vino, seguí sin elegir fecha. Cuando llegue, la anotás desde Hoy."
                            else -> "Si no lo sabés con exactitud, dejá los valores como están: se ajustan solos a medida que anotes."
                        },
                        style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave,
                    )
                    Tarjeta {
                        Text(
                            if (etapa == EtapaVida.EMBARAZO) "¿Cuándo empezó tu última menstruación?" else "¿Cuándo empezó tu último período?",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        OutlinedButton(onClick = { elegirFecha = true }, Modifier.fillMaxWidth()) {
                            Text(ultimo?.let { Fechas.larga(LocalDate.parse(it)) } ?: "Elegir fecha")
                        }
                        if (ultimo != null) {
                            TextButton(onClick = { ultimo = null }) { Text("No me acuerdo") }
                        }
                    }
                    if (etapa != EtapaVida.EMBARAZO) Tarjeta {
                        Contador("Ciclo", ciclo, 21..45) { ciclo = it }
                        Contador("Período", periodo, 2..10) { periodo = it }
                        Text(
                            "El ciclo se cuenta desde el primer día de un período hasta el día anterior al siguiente.",
                            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                        )
                    }
                    if (etapa == EtapaVida.EMBARAZO && ultimo == null) {
                        Text(
                            "Sin esta fecha no puedo calcular las semanas. Si no la sabés, seguí igual y elegí la etapa después en Ajustes con tu fecha probable de parto.",
                            style = MaterialTheme.typography.bodySmall, color = Colores.TerracotaOscura,
                        )
                    }
                },
                botones = { Button(onClick = { paso = 4 }, Modifier.fillMaxWidth().height(52.dp)) { Text("Seguir") } },
            )
            else -> Paso(
                contenido = {
                    Text("Protegé tu información", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)
                    Text(
                        "Podés pedir un PIN cada vez que se abre Eirmena. Si lo olvidás no se puede recuperar: " +
                            "habría que borrar los datos e importar un respaldo.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                botones = {
                    Button(onClick = { crearPin = true }, Modifier.fillMaxWidth().height(52.dp)) { Text("Poner un PIN") }
                    TextButton(onClick = ::terminar, Modifier.fillMaxWidth()) { Text("Ahora no") }
                },
            )
        }
    }

    if (elegirFecha) {
        ElegirFecha(
            titulo = "Primer día de tu último período",
            alElegir = { ultimo = it.toString(); elegirFecha = false },
            alCancelar = { elegirFecha = false },
        )
    }
    if (crearPin) {
        CrearPin(
            alTerminar = { pin ->
                crearPin = false
                alcance.launch { vm.ponerPin(pin); terminar() }
            },
            alCancelar = { crearPin = false },
        )
    }
}

@Composable
private fun Paso(contenido: @Composable ColumnScope.() -> Unit, botones: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = contenido,
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = botones)
    }
}

@Composable
private fun Punto(titulo: String, texto: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.padding(top = 7.dp).size(10.dp).clip(CircleShape).background(Colores.Terracota))
        Column {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(texto, style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)
        }
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier, tamano: Int = 120) {
    Box(modifier.size(tamano.dp).clip(CircleShape).background(Colores.Terracota), contentAlignment = Alignment.Center) {
        Image(painterResource(R.drawable.ic_launcher_frente), contentDescription = null, Modifier.size((tamano * 1.4).dp))
    }
}
