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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.educacion.Alerta
import com.byfooglesframes.eirmena.educacion.Alertas
import com.byfooglesframes.eirmena.educacion.Articulo
import com.byfooglesframes.eirmena.educacion.Biblioteca

/** Pestaña "Aprender": hormonas, asistente de retraso, señales para consultar y biblioteca. */
@Composable
fun PantallaAprender(vm: EirmenaViewModel, abrir: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val alertas = remember(datos) { Alertas.calcular(datos) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Aprender", style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)

        Acceso("Tus hormonas", "Gráfico de estrógeno, progesterona, LH y FSH día por día, con tu ciclo.", Colores.Terracota) {
            abrir(Sub.HORMONAS.name)
        }
        Acceso("Bienestar por fase", "Comida, movimiento, descanso y ánimo según el momento del ciclo.", Colores.Salvia) {
            abrir(Sub.BIENESTAR.name)
        }
        Acceso("Asistente de retraso", "Paso a paso: qué puede estar pasando y qué hacer.", Colores.Vino) {
            abrir(Sub.RETRASO.name)
        }
        Acceso(
            "Señales para consultar",
            if (alertas.isEmpty()) "Tus datos no muestran nada para consultar. También: cuándo ir a la guardia."
            else "Tus datos muestran ${if (alertas.size == 1) "1 cosa" else "${alertas.size} cosas"} para charlar en tu próxima consulta.",
            if (alertas.isEmpty()) Colores.Salvia else Colores.TerracotaOscura,
        ) { abrir(Sub.SENALES.name) }

        Text("Biblioteca", style = MaterialTheme.typography.titleLarge, color = Colores.Vino, modifier = Modifier.padding(top = 8.dp))
        Biblioteca.categorias.forEach { categoria ->
            Text(categoria, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            Biblioteca.articulos.filter { it.categoria == categoria }.forEach { a ->
                FilaArticulo(a) { abrir("${Sub.ARTICULO.name}:${a.id}") }
            }
        }
        Text(
            "Información general revisada en ${Biblioteca.REVISION}, basada en las fuentes de cada artículo. No reemplaza la consulta con un profesional.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}

@Composable
private fun Acceso(titulo: String, texto: String, color: Color, alTocar: () -> Unit) {
    Tarjeta(Modifier.clickable(onClick = alTocar), color = color) {
        Text(titulo, style = MaterialTheme.typography.titleLarge, color = Colores.Crema)
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = Colores.Crema)
    }
}

@Composable
private fun FilaArticulo(a: Articulo, alTocar: () -> Unit) {
    Tarjeta(Modifier.clickable(onClick = alTocar)) {
        Text(a.titulo, style = MaterialTheme.typography.titleMedium)
        Text(a.resumen, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
    }
}

@Composable
fun PantallaArticulo(id: String, alVolver: () -> Unit) {
    val a = Biblioteca.porId(id) ?: return alVolver()
    Subpantalla(a.categoria, alVolver) {
        Text(a.titulo, style = MaterialTheme.typography.headlineMedium, color = Colores.Vino)
        Text(a.resumen, style = MaterialTheme.typography.bodyLarge, color = Colores.TextoSuave)
        a.secciones.forEach { s ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text(s.texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Tarjeta(color = Colores.CremaOscura) {
            Text("Fuentes", style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)
            a.fuentes.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            Text("Revisado en ${Biblioteca.REVISION}.", style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
        }
    }
}

/** Alertas personales a partir de los datos, más las señales de guardia. */
@Composable
fun PantallaSenales(vm: EirmenaViewModel, alVolver: () -> Unit, abrirArticulo: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val alertas = remember(datos) { Alertas.calcular(datos) }
    val visibles = Alertas.visibles(alertas, datos.ajustes.alertasVistas)

    Subpantalla("Señales para consultar", alVolver) {
        Text(
            "Eirmena revisa tus datos y te avisa si ve algo que conviene mencionar en una consulta. No es un diagnóstico.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (alertas.isEmpty()) {
            Tarjeta(color = Colores.SalviaClara) {
                Text("Nada para señalar", style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
                Text(
                    "Con lo que anotaste no aparece nada fuera de lo esperable. Cuantos más ciclos anotes, mejor puede revisar.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        alertas.forEach { a -> TarjetaAlerta(a, vista = a !in visibles, alEntendido = { vm.alertaVista(a.id) }) }

        Button(
            onClick = { abrirArticulo("guardia") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Colores.TerracotaOscura),
        ) { Text("Cuándo ir a la guardia") }
        OutlinedButton(onClick = { abrirArticulo("lineas") }, modifier = Modifier.fillMaxWidth()) { Text("Líneas de ayuda") }
    }
}

@Composable
fun TarjetaAlerta(a: Alerta, vista: Boolean, alEntendido: (() -> Unit)?, alTocar: (() -> Unit)? = null) {
    Tarjeta(
        if (alTocar != null) Modifier.clickable(onClick = alTocar) else Modifier,
        color = if (vista) Colores.Tarjeta else Colores.RosaClaro,
    ) {
        Text("PARA CONSULTAR", style = MaterialTheme.typography.labelSmall, color = Colores.TerracotaOscura)
        Text(a.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(a.texto, style = MaterialTheme.typography.bodyMedium)
        Text(a.queHacer, style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura)
        if (alEntendido != null && !vista) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.TextButton(onClick = alEntendido) { Text("Entendido") }
            }
        }
    }
}
