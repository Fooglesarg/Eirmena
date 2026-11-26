package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
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
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.educacion.Bienestar
import com.byfooglesframes.eirmena.educacion.Consejo
import com.byfooglesframes.eirmena.educacion.Etapa
import com.byfooglesframes.eirmena.educacion.Hormonas
import java.time.LocalDate

/** Fase de hoy según el ciclo, o null si no aplica (embarazo, sin datos o método que frena la ovulación). */
fun faseDeHoy(datos: Datos, hoy: LocalDate = LocalDate.now()): Etapa? {
    if (datos.ajustes.etapa == EtapaVida.EMBARAZO || datos.ajustes.etapa == EtapaVida.PERIMENOPAUSIA) return null
    if (datos.ajustes.anticonceptivo.metodo.suprimeOvulacion) return null
    val p = Ciclos.predecir(datos, hoy)
    val h = Ciclos.hoy(datos, p, hoy) ?: return null
    return Hormonas.etapa(h.diaDelCiclo.coerceAtMost(p.duracionCiclo), p.duracionCiclo, p.duracionPeriodo)
}

/** Consejos que corresponden a la persona hoy. */
fun consejosDeHoy(datos: Datos): Pair<String, List<Consejo>>? = when {
    datos.ajustes.etapa == EtapaVida.EMBARAZO -> null
    datos.ajustes.etapa == EtapaVida.PERIMENOPAUSIA -> "Perimenopausia" to Bienestar.perimenopausia
    datos.ajustes.anticonceptivo.metodo.suprimeOvulacion -> "Con tu método" to Bienestar.conMetodoHormonal
    else -> faseDeHoy(datos)?.let { it.nombre to Bienestar.de(it) }
}

/** Tarjeta chica de Hoy: un consejo por día, que va rotando. */
@Composable
fun TarjetaBienestar(datos: Datos, alTocar: () -> Unit) {
    val (titulo, consejos) = consejosDeHoy(datos) ?: return
    val c = consejos[(LocalDate.now().toEpochDay() % consejos.size).toInt()]
    Tarjeta(Modifier.clickable(onClick = alTocar), color = Colores.SalviaClara) {
        Text("BIENESTAR · ${titulo.uppercase()}", style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(c.tema, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(c.texto, style = MaterialTheme.typography.bodyMedium)
        Text("Más ideas para cada fase →", style = MaterialTheme.typography.labelLarge, color = Colores.Vino)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaBienestar(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val hoy = remember(datos) { faseDeHoy(datos) }
    var fase by remember { mutableStateOf(hoy ?: Etapa.MENSTRUAL) }
    val especial = when {
        datos.ajustes.etapa == EtapaVida.PERIMENOPAUSIA -> "Para la perimenopausia" to Bienestar.perimenopausia
        datos.ajustes.anticonceptivo.metodo.suprimeOvulacion -> "Con tu método anticonceptivo" to Bienestar.conMetodoHormonal
        else -> null
    }

    Subpantalla("Bienestar por fase", alVolver) {
        Text(
            "Ideas para acompañar lo que muchas personas sienten en cada fase. No son reglas: probá y quedate con lo que te sirva.",
            style = MaterialTheme.typography.bodyMedium,
        )
        especial?.let { (titulo, consejos) ->
            Text(titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
            consejos.forEach { TarjetaConsejo(it) }
            Text("Fases del ciclo natural", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Etapa.entries.forEach { e -> Chip(if (e == hoy) "${e.nombre} · hoy" else e.nombre, fase == e) { fase = e } }
        }
        Bienestar.de(fase).forEach { TarjetaConsejo(it) }
        Text(
            "Basado en recomendaciones generales de salud (ACOG, NHS, OMS). Si algo te duele o te preocupa, consultá.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}

@Composable
private fun TarjetaConsejo(c: Consejo) {
    Tarjeta {
        Text(c.tema, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(c.texto, style = MaterialTheme.typography.bodyMedium)
    }
}
