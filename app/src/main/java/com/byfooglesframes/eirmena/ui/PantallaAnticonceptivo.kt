package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.datos.Anticonceptivo
import com.byfooglesframes.eirmena.datos.Esquema
import com.byfooglesframes.eirmena.datos.Frecuencia
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Metodos
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaAnticonceptivo(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val a = datos.ajustes.anticonceptivo
    var elegirFecha by remember { mutableStateOf(false) }
    var elegirHora by remember { mutableStateOf(false) }
    fun cambiar(t: (Anticonceptivo) -> Anticonceptivo) = vm.cambiarAjustes { it.copy(anticonceptivo = t(it.anticonceptivo)) }

    Subpantalla("Anticonceptivo", alVolver) {
        Tarjeta {
            Text("¿Qué método usás?", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Metodo.entries.forEach { m ->
                    Chip(m.nombre, a.metodo == m) {
                        // Al cambiar de método se borra la fecha, porque significa otra cosa.
                        if (m != a.metodo) cambiar { it.copy(metodo = m, inicio = null, vence = null) }
                    }
                }
            }
        }

        val textoFecha = when (a.metodo) {
            Metodo.PASTILLA -> "Primer día del blíster actual"
            Metodo.PARCHE -> "Día en que pusiste el primer parche de este ciclo"
            Metodo.ANILLO -> "Día en que colocaste el anillo actual"
            Metodo.INYECCION -> "Fecha de la última inyección"
            Metodo.IMPLANTE, Metodo.DIU_HORMONAL, Metodo.DIU_COBRE -> "Fecha de colocación"
            else -> null
        }

        if (a.metodo == Metodo.PASTILLA) {
            Tarjeta {
                Text("¿Cómo es tu blíster?", style = MaterialTheme.typography.titleMedium)
                Esquema.entries.forEach { e ->
                    Chip(e.nombre, a.esquema == e) { cambiar { it.copy(esquema = e) } }
                }
                Text("Si tenés dudas, fijate en el prospecto.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
            }
        }
        if (a.metodo == Metodo.INYECCION) {
            Tarjeta {
                Text("¿Cada cuánto te la aplicás?", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Frecuencia.entries.forEach { f -> Chip(f.nombre, a.frecuencia == f) { cambiar { it.copy(frecuencia = f) } } }
                }
            }
        }

        if (textoFecha != null) {
            Tarjeta {
                Text(textoFecha, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { elegirFecha = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(a.inicio?.let { Fechas.larga(it) } ?: "Elegir fecha")
                }
                if (a.metodo == Metodo.PASTILLA || a.metodo == Metodo.PARCHE || a.metodo == Metodo.ANILLO) {
                    Text(
                        "Con esta fecha Eirmena sabe en qué día del ciclo estás. Si se corre, por ejemplo por olvidarte " +
                            "o por empezar antes, actualizala.",
                        style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                    )
                }
            }
        }

        if (a.metodo.tieneVencimiento && a.inicio != null) {
            Tarjeta {
                Text("¿Cuánto dura?", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 8, 10).forEach { anios ->
                        val vence = a.inicio.plusYears(anios.toLong())
                        Chip("$anios años", a.vence == vence) { cambiar { it.copy(vence = vence) } }
                    }
                }
                Text(
                    a.vence?.let { "Vence el ${Fechas.larga(it)}. Te aviso 30 días antes." }
                        ?: "Depende del modelo: preguntá a quien te lo colocó o mirá tu carnet.",
                    style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                )
            }
        }

        if (a.metodo in listOf(Metodo.PASTILLA, Metodo.PARCHE, Metodo.ANILLO, Metodo.INYECCION)) {
            Tarjeta {
                Text("Hora del recordatorio", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { elegirHora = true }, modifier = Modifier.fillMaxWidth()) { Text(textoHora(a.hora)) }
                if (a.metodo == Metodo.PASTILLA) {
                    Text(
                        "Si no marcás que la tomaste, te lo vuelvo a recordar una hora después.",
                        style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                    )
                }
            }
        }

        Metodos.estado(a, LocalDate.now())?.let { e ->
            Tarjeta(color = Colores.CremaOscura) {
                Text("Hoy", style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
                Text(e.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                e.detalle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        val nota = when {
            a.metodo.suprimeOvulacion ->
                "Con este método el calendario no muestra ventana fértil ni ovulación, porque el método las frena."
            a.metodo == Metodo.DIU_HORMONAL ->
                "Con el DIU hormonal muchas personas siguen ovulando, por eso el calendario mantiene las estimaciones."
            else -> null
        }
        nota?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Text(
            "Eirmena solo te recuerda lo que vos cargás. Seguí siempre las indicaciones de tu profesional de la salud y del prospecto.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }

    if (elegirFecha) {
        ElegirFecha(
            titulo = textoFecha(a.metodo),
            alElegir = { f -> cambiar { it.copy(inicio = f, vence = null) }; elegirFecha = false },
            alCancelar = { elegirFecha = false },
        )
    }
    if (elegirHora) {
        ElegirHora(a.hora, alElegir = { h -> cambiar { it.copy(hora = h) }; elegirHora = false }, alCancelar = { elegirHora = false })
    }
}

private fun textoFecha(m: Metodo) = when (m) {
    Metodo.INYECCION -> "Última inyección"
    Metodo.IMPLANTE, Metodo.DIU_HORMONAL, Metodo.DIU_COBRE -> "Fecha de colocación"
    else -> "Primer día del ciclo actual"
}
