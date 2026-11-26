package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.Moco
import com.byfooglesframes.eirmena.datos.Test
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Metodos
import com.byfooglesframes.eirmena.datos.Nivel
import com.byfooglesframes.eirmena.datos.Relacion
import com.byfooglesframes.eirmena.datos.Sintoma
import java.time.LocalDate

/** Hoja para anotar todo lo de un día: sangrado, síntomas, ánimo, energía, sueño, deseo y relaciones. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HojaDia(vm: EirmenaViewModel, fecha: LocalDate, alCerrar: () -> Unit) {
    val datos by vm.datos.collectAsState()
    var r by remember(fecha) { mutableStateOf(datos.dia(fecha)) }
    val metodo = datos.ajustes.anticonceptivo

    // Si se marca sangrado en un día sin período cerca, se ofrece completar los días siguientes.
    val esInicio = r.flujo?.cuentaComoPeriodo == true &&
        (1L..3L).none { datos.dias[fecha.minusDays(it)]?.flujo?.cuentaComoPeriodo == true } &&
        datos.dias[fecha.plusDays(1)]?.flujo == null
    val diasHastaHoy = (LocalDate.now().toEpochDay() - fecha.toEpochDay()).toInt() + 1
    val completables = minOf(datos.ajustes.periodoHabitual, diasHastaHoy)
    var completar by remember(fecha) { mutableStateOf(true) }
    val etapa = datos.ajustes.etapa
    var verFertilidad by remember(fecha) {
        mutableStateOf(etapa == EtapaVida.BUSCO_EMBARAZO || r.temperatura != null || r.moco != null || r.testOvulacion != null || r.testEmbarazo != null)
    }
    var textoTemp by remember(fecha) { mutableStateOf(r.temperatura?.let { "%.2f".format(it).replace('.', ',') } ?: "") }
    val tempValida = textoTemp.isBlank() || textoTemp.replace(',', '.').toDoubleOrNull()?.let { it in 35.0..38.5 } == true
    var ofrecerEmbarazo by remember(fecha) { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = alCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Colores.Tarjeta,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(Fechas.conSemana(fecha), style = MaterialTheme.typography.titleLarge, color = Colores.Vino)

            Titulo("Sangrado")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Opcion("Nada", r.flujo == null) { r = r.copy(flujo = null) }
                Flujo.entries.forEach { f -> Opcion(f.nombre, r.flujo == f) { r = r.copy(flujo = f) } }
            }
            if (esInicio && completables > 1) {
                Casilla("Es el primer día: marcar también los ${completables - 1} días siguientes", completar) { completar = it }
            }

            if (metodo.metodo == Metodo.PASTILLA && Metodos.estado(metodo, fecha)?.tomaPastilla == true) {
                Separador()
                Casilla("Tomé la pastilla anticonceptiva", r.pastillaTomada) { r = r.copy(pastillaTomada = it) }
            }

            Separador()
            Titulo("Síntomas")
            val sintomas = Sintoma.entries.filter { !it.soloPerimenopausia || etapa == EtapaVida.PERIMENOPAUSIA || it in r.sintomas }
            Multiple(sintomas, r.sintomas, { it.nombre }) { r = r.copy(sintomas = it) }

            Separador()
            Titulo("Ánimo")
            Multiple(Animo.entries, r.animo, { it.nombre }) { r = r.copy(animo = it) }

            Separador()
            Escala("Energía", r.energia, listOf("Poca", "Normal", "Mucha")) { r = r.copy(energia = it) }
            Escala("Sueño", r.sueno, listOf("Mal", "Regular", "Bien")) { r = r.copy(sueno = it) }
            Escala("Deseo sexual", r.deseo, listOf("Bajo", "Normal", "Alto")) { r = r.copy(deseo = it) }

            Separador()
            Titulo("Sexo")
            Multiple(Relacion.entries, r.relaciones, { it.nombre }) { r = r.copy(relaciones = it) }
            Casilla("Tomé anticoncepción de emergencia", r.pastillaEmergencia) { r = r.copy(pastillaEmergencia = it) }
            if (r.pastillaEmergencia) {
                Text(
                    "La pastilla de emergencia puede adelantar o atrasar unos días el próximo período.",
                    style = MaterialTheme.typography.bodySmall, color = Colores.TextoSuave,
                )
            }

            Separador()
            Row(Modifier.fillMaxWidth().clickable { verFertilidad = !verFertilidad }, verticalAlignment = Alignment.CenterVertically) {
                Text("Fertilidad y tests", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                Text(if (verFertilidad) "▴" else "▾", style = MaterialTheme.typography.titleMedium, color = Colores.TextoSuave)
            }
            if (verFertilidad) {
                OutlinedTextField(
                    value = textoTemp,
                    onValueChange = { textoTemp = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5) },
                    label = { Text("Temperatura basal (°C)") },
                    placeholder = { Text("36,45") },
                    isError = !tempValida,
                    supportingText = { Text(if (tempValida) "Al despertar, antes de levantarte." else "Tiene que estar entre 35 y 38,5 °C.") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
                Text("Flujo", style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Moco.entries.forEach { m -> Opcion(m.nombre, r.moco == m) { r = r.copy(moco = if (r.moco == m) null else m) } }
                }
                Text("Test de ovulación", style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Test.entries.forEach { t -> Opcion(t.nombre, r.testOvulacion == t) { r = r.copy(testOvulacion = if (r.testOvulacion == t) null else t) } }
                }
                Text("Test de embarazo", style = MaterialTheme.typography.bodyMedium, color = Colores.TextoSuave)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Test.entries.forEach { t -> Opcion(t.nombre, r.testEmbarazo == t) { r = r.copy(testEmbarazo = if (r.testEmbarazo == t) null else t) } }
                }
            }

            Separador()
            OutlinedTextField(
                value = r.nota,
                onValueChange = { r = r.copy(nota = it.take(500)) },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(16.dp),
            )

            Button(
                enabled = tempValida,
                onClick = {
                    if (esInicio && completar && completables > 1) vm.empezoPeriodo(fecha)
                    val temp = textoTemp.replace(',', '.').toDoubleOrNull()
                    vm.guardarDia(r.copy(nota = r.nota.trim(), temperatura = temp))
                    // Un test positivo nuevo: se ofrece (sin insistir) pasar al modo embarazo.
                    val nuevoPositivo = r.testEmbarazo == Test.POSITIVO && datos.dia(fecha).testEmbarazo != Test.POSITIVO
                    if (nuevoPositivo && etapa != EtapaVida.EMBARAZO) ofrecerEmbarazo = true else alCerrar()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Guardar") }
        }
    }

    if (ofrecerEmbarazo) {
        DialogoTestPositivo(
            alActivar = {
                // La última menstruación anotada es la mejor estimación de la FUM.
                val fum = Ciclos.predecir(datos).ultimoInicio ?: fecha.minusDays(28)
                vm.empezarEmbarazo(fum)
                alCerrar()
            },
            alCerrar = alCerrar,
        )
    }
}

@Composable
private fun DialogoTestPositivo(alActivar: () -> Unit, alCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Test positivo") },
        text = {
            Text(
                "Si querés, Eirmena puede acompañarte semana a semana con el modo embarazo. Sea cual sea tu decisión, en " +
                    "Aprender → Asistente de retraso tenés información sobre los próximos pasos y la línea 0800 222 3444."
            )
        },
        confirmButton = { TextButton(onClick = alActivar) { Text("Activar modo embarazo") } },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Ahora no") } },
        containerColor = Colores.Tarjeta,
    )
}

@Composable
private fun Titulo(texto: String) = Text(texto, style = MaterialTheme.typography.titleMedium)

@Composable
private fun Separador() = HorizontalDivider(color = Colores.Linea)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Multiple(opciones: List<T>, elegidas: Set<T>, nombre: (T) -> String, alCambiar: (Set<T>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { o ->
            Opcion(nombre(o), o in elegidas) { alCambiar(if (o in elegidas) elegidas - o else elegidas + o) }
        }
    }
}

@Composable
private fun Escala(titulo: String, valor: Nivel?, nombres: List<String>, alCambiar: (Nivel?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Titulo(titulo)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Nivel.entries.forEachIndexed { i, n ->
                // Tocar el elegido lo desmarca.
                Opcion(nombres[i], valor == n) { alCambiar(if (valor == n) null else n) }
            }
        }
    }
}

@Composable
private fun Casilla(texto: String, valor: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = valor, onCheckedChange = alCambiar,
            colors = CheckboxDefaults.colors(checkedColor = Colores.Terracota),
        )
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Opcion(texto: String, elegida: Boolean, alTocar: () -> Unit) {
    FilterChip(
        selected = elegida,
        onClick = alTocar,
        label = { Text(texto) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Colores.Terracota,
            selectedLabelColor = Color.White,
        ),
    )
}
