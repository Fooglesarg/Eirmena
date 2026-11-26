package com.byfooglesframes.eirmena.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclo
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Prediccion
import com.byfooglesframes.eirmena.ciclo.Regularidad
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun PantallaCiclos(vm: EirmenaViewModel, abrir: (Sub) -> Unit) {
    val datos by vm.datos.collectAsState()
    val hoy = LocalDate.now()
    val p = remember(datos) { Ciclos.predecir(datos, hoy) }
    val lista = p.ciclos.reversed()
    val escala = (lista.mapNotNull { it.duracion } + p.duracionCiclo).max().coerceAtLeast(35)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Tus ciclos", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino) }
        item { Resumen(p) }
        item {
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.Button(onClick = { abrir(Sub.ESTADISTICAS) }, modifier = Modifier.weight(1f)) { Text("Estadísticas") }
                androidx.compose.material3.OutlinedButton(onClick = { abrir(Sub.INFORME) }, modifier = Modifier.weight(1f)) { Text("Informe") }
            }
        }
        item { ComoCalculo(p) }
        if (lista.isEmpty()) {
            item {
                Text(
                    "Todavía no hay ciclos anotados. Cuando anotes tu período, acá vas a ver el historial.",
                    style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave,
                )
            }
        } else {
            item { Text("Historial", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
            items(lista, key = { it.inicio.toString() }) { FilaCiclo(it, escala, hoy) }
        }
    }
}

@Composable
private fun Resumen(p: Prediccion) {
    Tarjeta {
        Row(Modifier.fillMaxWidth()) {
            Dato("Ciclo", "${p.duracionCiclo} días", Modifier.weight(1f))
            Dato("Período", "${p.duracionPeriodo} días", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth()) {
            Dato("Regularidad", p.regularidad.nombre, Modifier.weight(1f))
            Dato("Ciclos anotados", "${p.ciclos.count { it.duracion != null }}", Modifier.weight(1f))
        }
        val nota = when (p.regularidad) {
            Regularidad.POCOS_DATOS -> "Con 3 ciclos completos la estimación ya se basa solo en tus datos."
            Regularidad.REGULAR -> "Tus ciclos varían ${p.variacion} días entre sí. Es lo esperable."
            Regularidad.VARIABLE -> "Tus ciclos varían ${p.variacion} días entre sí. Puede pasar por estrés, cambios de rutina o etapas de la vida; si te preocupa, consultalo."
            Regularidad.IRREGULAR -> "Tus ciclos varían ${p.variacion} días entre sí. Cuando la diferencia es tan grande conviene consultarlo con un profesional de la salud."
        }
        Text(nota, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
    }
}

@Composable
private fun Dato(titulo: String, valor: String, modifier: Modifier) {
    Column(modifier.padding(vertical = 4.dp)) {
        Text(titulo, style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
        Text(valor, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
    }
}

@Composable
private fun ComoCalculo(p: Prediccion) {
    var abierto by remember { mutableStateOf(false) }
    Tarjeta(Modifier.animateContentSize().clickable { abierto = !abierto }, color = Colores.CremaOscura) {
        Text(if (abierto) "¿Cómo lo calculo? ▴" else "¿Cómo lo calculo? ▾", style = MaterialTheme.typography.titleMedium)
        if (abierto) {
            Text(
                "• Uso tus últimos ${p.ciclosUsados.coerceAtLeast(1)} ciclos (hasta 6) y los más recientes pesan más.\n" +
                    "• Si tenés menos de 3 ciclos, completo con la duración habitual que indicaste en Ajustes.\n" +
                    "• El rango (± ${Fechas.dias(p.rango)}) sale de cuánto varían tus ciclos.\n" +
                    "• La ovulación se estima 14 días antes del próximo período, y la ventana fértil son los 5 días previos, " +
                    "el día de la ovulación y el siguiente.\n" +
                    "• Ciclos de menos de 15 o más de 90 días no cuentan para el promedio.\n\n" +
                    "Es un cálculo estadístico: no detecta la ovulación ni sirve como anticonceptivo.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun FilaCiclo(c: Ciclo, escala: Int, hoy: LocalDate) {
    val enCurso = c.duracion == null
    val dias = c.duracion ?: (ChronoUnit.DAYS.between(c.inicio, hoy).toInt() + 1)
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(Fechas.larga(c.inicio, hoy), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(
                if (enCurso) "En curso · día $dias" else "$dias días",
                style = MaterialTheme.typography.labelLarge,
                color = if (enCurso) Colores.Terracota else Colores.Vino,
            )
        }
        // Barra: el tramo terracota es el período y el resto, el ciclo.
        Box(
            Modifier.fillMaxWidth(dias.coerceAtMost(escala).toFloat() / escala).height(10.dp)
                .clip(RoundedCornerShape(50)).background(Colores.RosaClaro)
        ) {
            Box(
                Modifier.fillMaxHeight().fillMaxWidth((c.periodo.dias.toFloat() / dias).coerceAtMost(1f))
                    .clip(RoundedCornerShape(50)).background(Colores.Terracota)
            )
        }
        Text("Período de ${Fechas.dias(c.periodo.dias)}", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
    }
}
