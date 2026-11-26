package com.byfooglesframes.eirmena.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.educacion.Bloque
import com.byfooglesframes.eirmena.educacion.InformePdf
import com.byfooglesframes.eirmena.educacion.Informes
import com.byfooglesframes.eirmena.educacion.OpcionesInforme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Elegir qué incluir y guardar el informe como PDF o como texto (.md). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PantallaInforme(vm: EirmenaViewModel, alVolver: () -> Unit) {
    val datos by vm.datos.collectAsState()
    val contexto = LocalContext.current
    val alcance = rememberCoroutineScope()
    var o by remember { mutableStateOf(OpcionesInforme()) }
    val informe = remember(datos, o) { Informes.armar(datos, o) }

    fun guardar(uri: android.net.Uri?, pdf: Boolean) {
        if (uri == null) return
        alcance.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    contexto.contentResolver.openOutputStream(uri, "wt")!!.use { s ->
                        if (pdf) InformePdf.escribir(informe, s) else s.write(Informes.markdown(informe).toByteArray())
                    }
                    true
                } catch (e: Exception) { false }
            }
            vm.avisar(if (ok) "Informe guardado. Podés compartirlo desde tus archivos." else "No se pudo guardar el informe.")
        }
    }
    val guardarPdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { guardar(it, true) }
    val guardarMd = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { guardar(it, false) }
    val nombreArchivo = "Eirmena-informe-${LocalDate.now()}"

    Subpantalla("Informe para la consulta", alVolver) {
        Text(
            "Un resumen de tus ciclos, síntomas y patrones para mostrar en una consulta. Se arma en el teléfono y lo guardás donde quieras: Eirmena no lo envía a ningún lado.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Tarjeta {
            Text("Período", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 12).forEach { m -> Chip(if (m == 12) "1 año" else "$m meses", o.meses == m) { o = o.copy(meses = m) } }
            }
            OutlinedTextField(
                value = o.nombre,
                onValueChange = { o = o.copy(nombre = it.take(60)) },
                label = { Text("Tu nombre (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            )
            Interruptor("Incluir notas", "Las notas que escribiste en cada día.", o.incluirNotas) { o = o.copy(incluirNotas = it) }
            Interruptor(
                "Incluir salud sexual",
                "Relaciones sin preservativo, anticoncepción de emergencia y tests de embarazo. Elegí si querés compartirlo.",
                o.incluirSexo,
            ) { o = o.copy(incluirSexo = it) }
        }

        Tarjeta(color = Colores.CremaOscura) {
            Text("Vista previa", style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)
            Text(informe.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
            Text(informe.subtitulo, style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave)
            informe.bloques.filterIsInstance<Bloque.Titulo>().forEach {
                Text("• ${it.texto}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Button(onClick = { guardarPdf.launch("$nombreArchivo.pdf") }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Guardar PDF") }
        OutlinedButton(onClick = { guardarMd.launch("$nombreArchivo.md") }, modifier = Modifier.fillMaxWidth()) { Text("Guardar como texto (.md)") }
        Text(
            "El informe lleva datos de salud: compartilo solo con quien vos elijas.",
            style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
        )
    }
}
