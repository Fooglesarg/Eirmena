package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.datos.Embarazo
import com.byfooglesframes.eirmena.educacion.Gestacion
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Tarjeta de Hoy durante el embarazo. */
@Composable
fun TarjetaEmbarazo(e: Embarazo, hoy: LocalDate, alVerMas: () -> Unit) {
    val semana = Gestacion.semanas(e.fum, hoy)
    val dias = Gestacion.diasExtra(e.fum, hoy)
    val info = Gestacion.semana(semana)
    val faltan = ChronoUnit.DAYS.between(hoy, e.fechaProbable)
    Tarjeta(Modifier.clickable(onClick = alVerMas), color = Colores.Tarjeta) {
        Text("EMBARAZO · ${Gestacion.trimestre(semana)}.º TRIMESTRE", style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(
            if (dias == 0) "Semana $semana" else "Semana $semana y ${Fechas.dias(dias)}",
            style = MaterialTheme.typography.headlineMedium, color = Colores.Vino,
        )
        LinearProgressIndicator(
            progress = { (Gestacion.dias(e.fum, hoy) / 280f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
            color = Colores.Terracota, trackColor = Colores.CremaOscura, drawStopIndicator = {},
        )
        Text(
            "Fecha probable de parto: ${Fechas.larga(e.fechaProbable)}" +
                if (faltan > 0) " (faltan ${Fechas.dias(faltan.toInt())})." else ".",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text("Tiene el tamaño de ${info.tamano}.", style = MaterialTheme.typography.titleMedium, color = Colores.TerracotaOscura)
        Text(info.bebe, style = MaterialTheme.typography.bodyMedium)
        Text("Semana a semana, controles y señales de alarma →", style = MaterialTheme.typography.labelLarge, color = Colores.Terracota)
    }
}

@Composable
fun PantallaEmbarazo(vm: EirmenaViewModel, alVolver: () -> Unit, abrirArticulo: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val e = datos.ajustes.embarazo ?: return alVolver()
    val hoy = LocalDate.now()
    val actual = Gestacion.semanas(e.fum, hoy)

    Subpantalla("Tu embarazo", alVolver) {
        Button(
            onClick = { abrirArticulo("alarma-embarazo") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Colores.TerracotaOscura),
        ) { Text("Señales de alarma") }

        val ahora = Gestacion.controlesDe(actual)
        Text("Controles y vacunas", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        if (ahora.isNotEmpty()) {
            Tarjeta(color = Colores.RosaClaro) {
                Text("Te toca ahora", style = MaterialTheme.typography.labelLarge, color = Colores.TerracotaOscura)
                ahora.forEach {
                    Text(it.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                    Text(it.texto, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        val proximos = Gestacion.controles.filter { it.desde > actual }.take(2)
        proximos.forEach {
            Tarjeta {
                Text("Desde la semana ${it.desde}", style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
                Text(it.titulo, style = MaterialTheme.typography.titleMedium)
                Text(it.texto, style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedButton(onClick = { abrirArticulo("controles-embarazo") }, modifier = Modifier.fillMaxWidth()) { Text("Todos los controles") }

        Text("Semana a semana", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        (4..40).forEach { n ->
            val s = Gestacion.semana(n)
            val esta = n == actual
            Tarjeta(color = if (esta) Colores.Terracota else if (n < actual) Colores.CremaOscura else Colores.Tarjeta) {
                val tinta = if (esta) Colores.Crema else Colores.Vino
                Text("Semana $n${if (esta) " · ahora" else ""}", style = MaterialTheme.typography.titleMedium, color = tinta)
                Text("Tamaño de ${s.tamano}", style = MaterialTheme.typography.labelLarge, color = if (esta) Colores.RosaClaro else Colores.TerracotaOscura)
                Text(s.bebe, style = MaterialTheme.typography.bodyMedium, color = if (esta) Colores.Crema else Colores.Texto)
                Text(s.vos, style = MaterialTheme.typography.bodySmall, color = if (esta) Colores.RosaClaro else Colores.TextoSuave)
            }
        }
        Text(
            "Los tamaños y fechas son aproximados: cada embarazo es distinto. Seguí siempre las indicaciones de tu equipo de salud.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}
