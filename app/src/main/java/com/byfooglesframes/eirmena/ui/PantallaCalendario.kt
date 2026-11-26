package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Marca
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.RegistroDia
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun PantallaCalendario(vm: EirmenaViewModel, abrirDia: (LocalDate) -> Unit) {
    val datos by vm.datos.collectAsState()
    val hoy = LocalDate.now()
    var mesTexto by rememberSaveable { mutableStateOf(YearMonth.from(hoy).toString()) }
    val mes = YearMonth.parse(mesTexto)
    val prediccion = remember(datos) { Ciclos.predecir(datos, hoy) }
    val sinOvulacion = datos.ajustes.anticonceptivo.metodo.suprimeOvulacion
    val marcas = remember(datos, mes) {
        // Durante el embarazo no hay estimaciones de período ni de fertilidad.
        if (datos.ajustes.etapa == com.byfooglesframes.eirmena.datos.EtapaVida.EMBARAZO) emptyMap()
        else Ciclos.marcas(prediccion, mes.atDay(1), mes.atEndOfMonth(), hoy).filterValues { !sinOvulacion || it == Marca.PERIODO }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { mesTexto = mes.minusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Mes anterior")
            }
            Text(
                Fechas.mes(mes),
                Modifier.weight(1f).clickable { mesTexto = YearMonth.from(hoy).toString() },
                style = MaterialTheme.typography.titleLarge,
                color = Colores.Vino,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = { mesTexto = mes.plusMonths(1).toString() }) {
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Mes siguiente")
            }
        }

        Tarjeta {
            Row(Modifier.fillMaxWidth()) {
                listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                    Text(
                        it, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium,
                        color = Colores.TextoSuave, textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
            val vacios = mes.atDay(1).dayOfWeek.value - 1
            val celdas = vacios + mes.lengthOfMonth()
            for (semana in 0 until (celdas + 6) / 7) {
                Row(Modifier.fillMaxWidth()) {
                    for (d in 0 until 7) {
                        val n = semana * 7 + d - vacios + 1
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (n in 1..mes.lengthOfMonth()) {
                                val fecha = mes.atDay(n)
                                Dia(
                                    fecha = fecha,
                                    registro = datos.dias[fecha],
                                    marca = marcas[fecha],
                                    esHoy = fecha == hoy,
                                    alTocar = if (fecha <= hoy) ({ abrirDia(fecha) }) else null,
                                )
                            }
                        }
                    }
                }
            }
        }

        Referencias(sinOvulacion)
        Text(
            "Tocá un día para anotar o corregir el sangrado. Los días futuros muestran estimaciones.",
            style = MaterialTheme.typography.bodySmall,
            color = Colores.TextoSuave,
        )
    }
}

@Composable
private fun Dia(fecha: LocalDate, registro: RegistroDia?, marca: Marca?, esHoy: Boolean, alTocar: (() -> Unit)?) {
    val flujo = registro?.flujo
    val fondo = when {
        flujo != null && flujo.cuentaComoPeriodo -> colorFlujo(flujo)
        marca == Marca.FERTIL -> Colores.SalviaClara
        marca == Marca.OVULACION -> Colores.Salvia
        else -> Color.Transparent
    }
    val texto = when {
        flujo != null && flujo.cuentaComoPeriodo && flujo != Flujo.LEVE -> Color.White
        marca == Marca.OVULACION -> Color.White
        alTocar == null -> Colores.TextoSuave
        else -> Colores.Texto
    }
    var mod = Modifier.padding(3.dp).fillMaxSize().clip(CircleShape).background(fondo)
    if (marca == Marca.PERIODO && flujo == null) mod = mod.border(BorderStroke(2.dp, Colores.Rosa), CircleShape)
    if (esHoy) mod = mod.border(BorderStroke(2.dp, Colores.Vino), CircleShape)
    if (alTocar != null) mod = mod.clickable(onClick = alTocar)

    Box(mod, contentAlignment = Alignment.Center) {
        Text(
            "${fecha.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
            color = texto,
        )
        val conPunto = flujo == Flujo.MANCHADO || registro?.tieneDetalle == true
        if (conPunto) {
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).size(5.dp).clip(CircleShape)
                    .background(if (flujo == Flujo.MANCHADO) Colores.Terracota else Colores.Vino)
            )
        }
    }
}

fun colorFlujo(f: Flujo): Color = when (f) {
    Flujo.MANCHADO -> Colores.RosaClaro
    Flujo.LEVE -> Colores.Rosa
    Flujo.MEDIO -> Colores.Terracota
    Flujo.ABUNDANTE -> Colores.TerracotaOscura
}

@Composable
private fun Referencias(sinOvulacion: Boolean) {
    Tarjeta(color = Colores.CremaOscura) {
        Referencia(Colores.Terracota, null, "Período anotado")
        Referencia(Color.Transparent, Colores.Rosa, "Período estimado")
        if (!sinOvulacion) {
            Referencia(Colores.SalviaClara, null, "Ventana fértil estimada")
            Referencia(Colores.Salvia, null, "Ovulación estimada")
        }
        Punto(Colores.Terracota, "Manchado")
        Punto(Colores.Vino, "Síntomas, ánimo u otras notas")
    }
}

@Composable
private fun Punto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        }
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Referencia(relleno: Color, borde: Color?, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        var m = Modifier.size(20.dp).clip(CircleShape).background(relleno)
        if (borde != null) m = m.border(BorderStroke(2.dp, borde), CircleShape)
        Box(m)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
