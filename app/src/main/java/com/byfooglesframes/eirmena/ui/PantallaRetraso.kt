package com.byfooglesframes.eirmena.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Relacion

/** Causas posibles de un retraso, con su explicación para el resultado. */
private enum class Factor(val nombre: String, val explicacion: String) {
    ESTRES("Estrés fuerte", "El estrés intenso puede frenar la señal del cerebro que dispara la ovulación, y sin ovulación el período se corre."),
    PESO("Cambios de peso o de alimentación", "Bajar o subir mucho de peso, o comer muy poco, cambia las hormonas que regulan el ciclo."),
    EJERCICIO("Ejercicio intenso", "Entrenar mucho más de lo habitual puede retrasar o frenar la ovulación."),
    VIAJE("Viaje o cambio de horarios", "Los viajes, los turnos de noche y dormir mal pueden correr el ciclo unos días."),
    ENFERMEDAD("Enfermedad o fiebre", "Una enfermedad en la primera mitad del ciclo puede retrasar la ovulación y, con ella, el período."),
    METODO("Empecé, dejé o cambié un anticonceptivo", "Al dejar un método hormonal el ciclo puede tardar unos meses en acomodarse; al empezar uno, el sangrado cambia."),
    EMERGENCIA("Tomé anticoncepción de emergencia", "La pastilla de emergencia puede adelantar o atrasar unos días el período."),
    LACTANCIA("Estoy amamantando", "La lactancia frena la ovulación: es normal no menstruar o tener ciclos irregulares."),
    EDAD("Tengo más de 40 años", "En la perimenopausia los ciclos se vuelven irregulares y pueden saltearse."),
}

private enum class Paso { RELACIONES, PROTECCION, FACTORES, RESULTADO }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaRetraso(vm: EirmenaViewModel, alVolver: () -> Unit, abrirArticulo: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val p = remember(datos) { Ciclos.predecir(datos) }
    val estado = remember(datos) { Ciclos.hoy(datos, p) }
    val retraso = estado?.retraso ?: 0

    // Lo anotado en este ciclo responde solo algunas preguntas.
    val ciclo = p.ultimoInicio?.let { inicio -> datos.dias.values.filter { it.fecha >= inicio } }.orEmpty()
    val anotoSinProteccion = ciclo.any { Relacion.SIN_PRESERVATIVO in it.relaciones }
    val anotoEmergencia = ciclo.any { it.pastillaEmergencia }

    var paso by rememberSaveable { mutableStateOf(Paso.RELACIONES) }
    var relaciones by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var riesgo by rememberSaveable { mutableStateOf(false) }
    var factores by rememberSaveable { mutableStateOf(if (anotoEmergencia) setOf(Factor.EMERGENCIA.name) else setOf()) }

    fun atras() {
        paso = when (paso) {
            Paso.RELACIONES -> { alVolver(); return }
            Paso.PROTECCION -> Paso.RELACIONES
            Paso.FACTORES -> if (relaciones == true) Paso.PROTECCION else Paso.RELACIONES
            Paso.RESULTADO -> Paso.FACTORES
        }
    }
    BackHandler { atras() }

    Subpantalla("Asistente de retraso", ::atras) {
        Tarjeta(color = Colores.RosaClaro) {
            Text(
                when {
                    retraso > 0 -> "Tu período tiene ${Fechas.dias(retraso)} de retraso según tus datos."
                    estado != null -> "Según tus datos, tu período todavía no está atrasado. Igual podés usar el asistente."
                    else -> "Todavía no anotaste períodos. Igual podés usar el asistente."
                },
                style = MaterialTheme.typography.bodyLarge, color = Colores.Vino,
            )
        }

        AnimatedContent(paso, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "paso") { actual ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (actual) {
                    Paso.RELACIONES -> {
                        Pregunta("¿Tuviste relaciones con penetración vaginal en este ciclo?")
                        if (anotoSinProteccion) Nota("En Eirmena anotaste relaciones sin preservativo este ciclo.")
                        Opcion("Sí") { relaciones = true; paso = Paso.PROTECCION }
                        Opcion("No") { relaciones = false; riesgo = false; paso = Paso.FACTORES }
                    }
                    Paso.PROTECCION -> {
                        Pregunta("¿Usaron un método anticonceptivo todas las veces, sin fallas?")
                        Nota("Una falla es, por ejemplo, que se rompa o se salga el preservativo, olvidarte pastillas o vomitar después de tomarla.")
                        Opcion("Sí, siempre y sin fallas") { riesgo = false; paso = Paso.FACTORES }
                        Opcion("No, o hubo alguna falla") { riesgo = true; paso = Paso.FACTORES }
                        Opcion("No lo sé") { riesgo = true; paso = Paso.FACTORES }
                    }
                    Paso.FACTORES -> {
                        Pregunta("¿Te pasó algo de esto en las últimas semanas?")
                        Nota("Elegí todas las que correspondan, o ninguna.")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Factor.entries.forEach { f ->
                                Chip(f.nombre, f.name in factores) {
                                    factores = if (f.name in factores) factores - f.name else factores + f.name
                                }
                            }
                        }
                        Button(onClick = { paso = Paso.RESULTADO }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Ver qué hacer") }
                    }
                    Paso.RESULTADO -> Resultado(
                        riesgo = riesgo,
                        factores = Factor.entries.filter { it.name in factores },
                        alReiniciar = { paso = Paso.RELACIONES; relaciones = null; riesgo = false },
                        abrirArticulo = abrirArticulo,
                    )
                }
            }
        }

        Text(
            "Este asistente da información general y no reemplaza una consulta.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}

@Composable
private fun Resultado(riesgo: Boolean, factores: List<Factor>, alReiniciar: () -> Unit, abrirArticulo: (String) -> Unit) {
    if (riesgo) {
        Tarjeta {
            Text("1. Hacete un test de embarazo", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
            Text(
                "Los tests de farmacia son confiables desde el primer día de retraso. Usá la primera orina de la mañana, " +
                    "que está más concentrada, y seguí las instrucciones del envase. También podés pedir uno gratis en un " +
                    "centro de salud.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Si da negativo", style = MaterialTheme.typography.titleMedium)
            Text(
                "Si el período sigue sin llegar, repetilo en una semana: a veces el test se hace demasiado pronto.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Si da positivo", style = MaterialTheme.typography.titleMedium)
            Text(
                "Pedí un turno para confirmarlo y hablar de tus opciones. Si querés continuar el embarazo, empezá los " +
                    "controles cuanto antes. Si no querés continuarlo, en Argentina la interrupción es legal y gratuita hasta " +
                    "la semana 14 inclusive. La línea 0800 222 3444 te informa, gratis y de forma confidencial, sobre todas las opciones.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Andá a la guardia si tenés un dolor fuerte de un lado del bajo vientre, sangrado con dolor o mareos: puede " +
                    "ser un embarazo ectópico.",
                style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura,
            )
        }
    }

    Tarjeta {
        Text(
            if (riesgo) "2. Otras causas posibles" else "Causas posibles",
            style = MaterialTheme.typography.titleLarge, color = Colores.Vino,
        )
        if (factores.isEmpty()) {
            Text(
                "No marcaste ninguna causa en especial. A veces el período se corre unos días sin un motivo claro, sobre " +
                    "todo si tus ciclos suelen variar.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            factores.forEach { f ->
                Text(f.nombre, style = MaterialTheme.typography.titleMedium)
                Text(f.explicacion, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (!riesgo) {
            Text(
                "Sin relaciones con penetración, o con un método usado sin fallas, un embarazo es muy poco probable. " +
                    "Si igual te queda la duda, un test te la saca.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    Tarjeta(color = Colores.CremaOscura) {
        Text("Consultá si", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(
            "• Pasan 3 meses sin período (y no estás embarazada).\n" +
                "• Los retrasos se repiten varios meses seguidos.\n" +
                "• Además tenés acné, más vello, cambios de peso que no entendés o salida de líquido por los pezones.\n" +
                "• Te preocupa: no hace falta esperar para consultar.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    TextButton(onClick = { abrirArticulo("guardia") }) { Text("Cuándo ir a la guardia") }
    TextButton(onClick = { abrirArticulo("lineas") }) { Text("Líneas de ayuda") }
    OutlinedButton(onClick = alReiniciar, modifier = Modifier.fillMaxWidth()) { Text("Empezar de nuevo") }
}

@Composable
private fun Pregunta(texto: String) = Text(texto, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)

@Composable
private fun Nota(texto: String) = Text(texto, style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)

@Composable
private fun Opcion(texto: String, alTocar: () -> Unit) {
    OutlinedButton(onClick = alTocar, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(texto) }
}
