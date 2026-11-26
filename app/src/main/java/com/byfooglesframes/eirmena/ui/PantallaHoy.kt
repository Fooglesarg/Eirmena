package com.byfooglesframes.eirmena.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Fase
import com.byfooglesframes.eirmena.ciclo.Hoy
import com.byfooglesframes.eirmena.ciclo.Prediccion
import com.byfooglesframes.eirmena.EirmenaViewModel
import com.byfooglesframes.eirmena.datos.Curiosidad
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.FinEmbarazo
import java.time.temporal.ChronoUnit
import androidx.compose.material3.TextButton
import com.byfooglesframes.eirmena.educacion.Alertas
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.EstadoMetodo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Metodos
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Relacion
import com.byfooglesframes.eirmena.datos.Curiosidades
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PantallaHoy(vm: EirmenaViewModel, abrirDia: (LocalDate) -> Unit, abrir: (Sub) -> Unit, abrirArticulo: (String) -> Unit) {
    val datos by vm.datos.collectAsState()
    val hoy = LocalDate.now()
    val prediccion = remember(datos) { Ciclos.predecir(datos, hoy) }
    val estado = remember(datos) { Ciclos.hoy(datos, prediccion, hoy) }
    var elegirFecha by remember { mutableStateOf(false) }
    val metodo = datos.ajustes.anticonceptivo
    val sinOvulacion = metodo.metodo.suprimeOvulacion
    val registroHoy = datos.dia(hoy)
    val etapa = datos.ajustes.etapa
    val embarazo = datos.ajustes.embarazo?.takeIf { etapa == EtapaVida.EMBARAZO && it.fin == null }
    val alerta = remember(datos) { Alertas.visibles(Alertas.calcular(datos, hoy), datos.ajustes.alertasVistas, hoy).firstOrNull() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(Fechas.conSemana(hoy), style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)

        if (embarazo != null) {
            TarjetaEmbarazo(embarazo, hoy, alVerMas = { abrir(Sub.EMBARAZO) })
            OutlinedButton(onClick = { abrirDia(hoy) }, modifier = Modifier.fillMaxWidth()) { Text("Anotar mi día") }
        } else if (estado == null) {
            if (etapa == EtapaVida.PRIMEROS_CICLOS) {
                SinDatosPrimeros(alVenirHoy = { vm.empezoPeriodo(hoy) }, alElegirFecha = { elegirFecha = true }, alLeer = { abrirArticulo("primera-menstruacion") })
            } else {
                SinDatos(alVenirHoy = { vm.empezoPeriodo(hoy) }, alElegirFecha = { elegirFecha = true })
            }
        } else {
            Anillo(prediccion, estado, sinOvulacion, metodo.metodo.nombre)
            Estado(prediccion, estado, hoy, contextoRetraso(datos, prediccion))
            if (estado.retraso > 0) {
                OutlinedButton(onClick = { abrir(Sub.RETRASO) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir el asistente de retraso") }
            }
            if (estado.diaDelPeriodo != null) {
                Button(onClick = { abrirDia(hoy) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Anotar cómo va hoy")
                }
            } else {
                Button(onClick = { vm.empezoPeriodo(hoy) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Me vino hoy")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { elegirFecha = true }, modifier = Modifier.weight(1f)) { Text("Me vino otro día") }
                    OutlinedButton(onClick = { abrirDia(hoy) }, modifier = Modifier.weight(1f)) { Text("Anotar mi día") }
                }
            }
        }

        if (etapa == EtapaVida.BUSCO_EMBARAZO && embarazo == null) {
            estadoFertil(datos, prediccion, hoy)?.let { TarjetaFertil(it, alTocar = { abrir(Sub.FERTILIDAD) }) }
        }
        if (etapa == EtapaVida.PERIMENOPAUSIA) TarjetaPerimenopausia(prediccion.ultimoInicio, hoy, alLeer = { abrirArticulo("perimenopausia") })
        datos.ajustes.embarazo?.takeIf { e -> embarazo == null && e.fin != null && ChronoUnit.DAYS.between(e.fin, hoy) in 0..90 }?.let { e ->
            TarjetaDespues(e.resultado, alLeer = { abrirArticulo("posparto") })
        }

        Metodos.estado(metodo, hoy)?.let { em ->
            TarjetaMetodo(
                metodo = metodo.metodo,
                estado = em,
                tomada = registroHoy.pastillaTomada,
                alTomar = { vm.pastillaTomada(hoy, it) },
                alAplicar = { vm.cambiarAjustes { a -> a.copy(anticonceptivo = a.anticonceptivo.copy(inicio = hoy)) } },
            )
        }

        if (!registroHoy.copy(pastillaTomada = false).vacio) ResumenDia(registroHoy, alTocar = { abrirDia(hoy) })

        alerta?.let { TarjetaAlerta(it, vista = false, alEntendido = { vm.alertaVista(it.id) }, alTocar = { abrir(Sub.SENALES) }) }

        if (estado != null && embarazo == null) TarjetaFase(estado.fase, if (sinOvulacion) metodo.metodo else null, verHormonas = { abrir(Sub.HORMONAS) })

        if (embarazo == null) TarjetaBienestar(datos, alTocar = { abrir(Sub.BIENESTAR) })

        TarjetaSabias(if (datos.ajustes.trofeo != null) null else Curiosidades.porId(datos.ajustes.proximaCarta), alTocar = { abrir(Sub.SABIAS) })

        Text(
            "Las fechas son estimaciones a partir de tu historial. Eirmena no es un método anticonceptivo " +
                "ni reemplaza la consulta con un profesional de la salud.",
            style = MaterialTheme.typography.bodySmall,
            color = Colores.TextoSuave,
        )
    }

    if (elegirFecha) {
        ElegirFecha(
            titulo = "¿Qué día te vino?",
            alElegir = { vm.empezoPeriodo(it); elegirFecha = false },
            alCancelar = { elegirFecha = false },
        )
    }
}

@Composable
private fun SinDatosPrimeros(alVenirHoy: () -> Unit, alElegirFecha: () -> Unit, alLeer: () -> Unit) {
    Tarjeta {
        Text("Tus primeros ciclos", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        Text(
            "Cuando te venga, tocá \"Me vino hoy\". Es algo natural y no tiene nada de malo. Los primeros años los ciclos " +
                "suelen ser irregulares, y Eirmena lo tiene en cuenta.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(4.dp))
        Button(onClick = alVenirHoy, modifier = Modifier.fillMaxWidth()) { Text("Me vino hoy") }
        OutlinedButton(onClick = alElegirFecha, modifier = Modifier.fillMaxWidth()) { Text("Me vino otro día") }
        TextButton(onClick = alLeer) { Text("Qué esperar y cómo prepararte") }
    }
}

@Composable
private fun TarjetaPerimenopausia(ultimo: LocalDate?, hoy: LocalDate, alLeer: () -> Unit) {
    val dias = ultimo?.let { ChronoUnit.DAYS.between(it, hoy).toInt() }
    Tarjeta(Modifier.clickable(onClick = alLeer), color = Colores.CremaOscura) {
        Text("PERIMENOPAUSIA", style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(
            when {
                dias == null -> "Anotá tus períodos cuando vengan"
                dias >= 365 -> "Más de un año sin período"
                else -> "Tu último período empezó hace ${Fechas.dias(dias)}"
            },
            style = MaterialTheme.typography.titleMedium, color = Colores.Vino,
        )
        Text(
            when {
                dias == null -> "En esta etapa los ciclos cambian: anotar períodos y síntomas como los sofocos te ayuda a ver cómo vas."
                dias >= 365 -> "Doce meses seguidos sin período marcan la menopausia. Si vuelve a aparecer un sangrado, consultalo pronto."
                else -> "Es normal que los ciclos se alarguen, se acorten o se salteen. Cuando pasen 12 meses seguidos sin período, llegaste a la menopausia."
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Text("Perimenopausia y menopausia →", style = MaterialTheme.typography.labelLarge, color = Colores.Terracota)
    }
}

@Composable
private fun TarjetaDespues(resultado: FinEmbarazo?, alLeer: () -> Unit) {
    val texto = when (resultado) {
        FinEmbarazo.NACIMIENTO -> "Sin lactancia, el período suele volver entre las 6 y 8 semanas; con lactancia puede tardar meses. " +
            "Podés ovular antes del primer período: si no buscás otro embarazo, hablá de anticoncepción en tu control."
        FinEmbarazo.PERDIDA -> "Lo sentimos mucho. Date el tiempo que necesites. El período suele volver en 4 a 6 semanas. " +
            "Si te cuesta atravesarlo, pedir ayuda está bien."
        else -> "El período suele volver en 4 a 6 semanas. Podés ovular antes: si no buscás un embarazo, consultá por anticoncepción."
    }
    Tarjeta(Modifier.clickable(onClick = alLeer), color = Colores.CremaOscura) {
        Text("DESPUÉS DEL EMBARAZO", style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
        Text("Leer más →", style = MaterialTheme.typography.labelLarge, color = Colores.Terracota)
    }
}

@Composable
private fun SinDatos(alVenirHoy: () -> Unit, alElegirFecha: () -> Unit) {
    Tarjeta {
        Text("Empecemos", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
        Text(
            "Anotá el primer día de tu último período. Con eso Eirmena estima el próximo, " +
                "y con cada ciclo que anotes la estimación se ajusta a vos.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(4.dp))
        Button(onClick = alVenirHoy, modifier = Modifier.fillMaxWidth()) { Text("Me vino hoy") }
        OutlinedButton(onClick = alElegirFecha, modifier = Modifier.fillMaxWidth()) { Text("Me vino otro día") }
    }
}

/** Anillo del ciclo: período en terracota, ventana fértil en salvia y un punto en el día de hoy. */
@Composable
private fun Anillo(p: Prediccion, h: Hoy, sinOvulacion: Boolean, metodo: String) {
    val total = maxOf(p.duracionCiclo, h.diaDelCiclo)
    val ovulacion = p.duracionCiclo - Ciclos.FASE_LUTEA // día del ciclo (base 0)
    Box(Modifier.fillMaxWidth().height(250.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(230.dp)) {
            val grosor = 18.dp.toPx()
            val radio = size.minDimension / 2 - grosor
            val estilo = Stroke(grosor, cap = StrokeCap.Round)
            val porDia = 360f / total
            fun arco(desde: Int, dias: Int, color: Color) = drawArc(
                color, startAngle = -90f + desde * porDia + 1.5f, sweepAngle = (dias * porDia - 3f).coerceAtLeast(0.1f),
                useCenter = false, topLeft = Offset(center.x - radio, center.y - radio),
                size = androidx.compose.ui.geometry.Size(radio * 2, radio * 2), style = estilo,
            )
            arco(0, total, Colores.CremaOscura)
            arco(0, p.duracionPeriodo, Colores.Terracota)
            if (!sinOvulacion) {
                arco(ovulacion - 5, 7, Colores.SalviaClara)
                arco(ovulacion, 1, Colores.Salvia)
            }

            val angulo = Math.toRadians((-90.0 + (h.diaDelCiclo - 0.5) * porDia))
            val punto = Offset(center.x + radio * cos(angulo).toFloat(), center.y + radio * sin(angulo).toFloat())
            drawCircle(Color.White, grosor * 0.85f, punto)
            drawCircle(Colores.Vino, grosor * 0.5f, punto)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Día", style = MaterialTheme.typography.labelLarge, color = Colores.TextoSuave)
            Text("${h.diaDelCiclo}", style = MaterialTheme.typography.displayLarge, color = Colores.Vino)
            Text(
                when {
                    h.retraso > 0 -> "Retraso"
                    sinOvulacion && h.fase != Fase.MENSTRUAL -> metodo
                    else -> h.fase.nombre
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (h.retraso > 0) Colores.TerracotaOscura else Colores.Texto,
            )
        }
    }
}

@Composable
private fun Estado(p: Prediccion, h: Hoy, hoy: LocalDate, contexto: String?) {
    val proximo = p.proximoInicio!!
    Tarjeta(color = if (h.retraso > 0) Colores.RosaClaro else Colores.Tarjeta) {
        when {
            h.diaDelPeriodo != null -> {
                Text("Día ${h.diaDelPeriodo} de tu período", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
                Text(
                    "Suele durarte ${Fechas.dias(p.duracionPeriodo)}. Anotá los días que sigas sangrando; " +
                        "cuando no anotes más, Eirmena entiende que terminó.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            h.retraso > 0 -> {
                Text("Tu período tiene ${Fechas.dias(h.retraso)} de retraso", style = MaterialTheme.typography.titleLarge, color = Colores.Vino)
                Text(
                    if (!h.retrasoFueraDeRango)
                        "Todavía está dentro de lo que suelen variar tus ciclos (± ${Fechas.dias(p.rango)}). Es común que se corra unos días."
                    else
                        "Un retraso puede deberse a estrés, cambios de peso o de rutina, viajes, enfermedad, " +
                            "anticonceptivos, lactancia o un embarazo. Si tuviste relaciones sin protección, " +
                            "un test de embarazo de farmacia ya es confiable desde el primer día de retraso.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                contexto?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = Colores.TerracotaOscura)
                }
            }
            else -> {
                val n = h.diasParaProximo
                Text(
                    if (n == 0) "Tu período podría llegar hoy" else "Tu período llegaría en ${Fechas.dias(n)}",
                    style = MaterialTheme.typography.titleLarge, color = Colores.Vino,
                )
                val desde = proximo.minusDays(p.rango.toLong()).let { if (it < hoy) hoy else it }
                Text(
                    "Lo más probable es el ${Fechas.larga(proximo)}, ${Fechas.rango(desde, proximo.plusDays(p.rango.toLong()))}.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TarjetaFase(fase: Fase, hormonal: Metodo?, verHormonas: () -> Unit) {
    val (titulo, texto) = if (hormonal != null) {
        val conPausa = hormonal == Metodo.PASTILLA || hormonal == Metodo.PARCHE || hormonal == Metodo.ANILLO
        "Tu ciclo con ${hormonal.nombre.lowercase()}" to (
            "Las hormonas del método frenan la ovulación y mantienen estables el estrógeno y la progesterona, " +
                "así que las fases naturales del ciclo no aplican. " +
                if (conPausa) "El sangrado de la semana de descanso es un \"sangrado por deprivación\" por la baja de hormonas, no una menstruación como la natural."
                else "Con este método es común que el sangrado sea irregular, más escaso o que desaparezca, y no es peligroso."
            )
    } else when (fase) {
        Fase.MENSTRUAL -> "Fase menstrual" to
            "El estrógeno y la progesterona están en su punto más bajo, y por eso se desprende la capa interna del útero. " +
            "Es normal sentir más cansancio o cólicos."
        Fase.FOLICULAR -> "Fase folicular" to
            "La hormona FSH hace madurar los folículos de los ovarios y el estrógeno sube de a poco. " +
            "Muchas personas notan más energía y mejor ánimo."
        Fase.FERTIL -> "Ventana fértil (estimada)" to
            "El estrógeno llega a su pico y dispara la hormona LH, que provoca la ovulación. " +
            "Son los días con más probabilidad de embarazo. Es una estimación: no la uses como método anticonceptivo."
        Fase.LUTEA -> "Fase lútea" to
            "Después de ovular, el ovario produce progesterona para preparar el útero. Si no hay embarazo, " +
            "las hormonas bajan al final del ciclo y pueden aparecer síntomas premenstruales."
    }
    Tarjeta(Modifier.clickable(onClick = verHormonas), color = Colores.CremaOscura) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        Text(texto, style = MaterialTheme.typography.bodyMedium)
        Text("Ver tus hormonas día por día →", style = MaterialTheme.typography.labelLarge, color = Colores.Terracota)
    }
}

/** Qué anotó la persona en este ciclo que ayuda a entender un retraso. */
private fun contextoRetraso(d: Datos, p: Prediccion): String? {
    val inicio = p.ultimoInicio ?: return null
    val ciclo = d.dias.values.filter { it.fecha >= inicio }
    return when {
        ciclo.any { it.pastillaEmergencia } ->
            "Este ciclo anotaste anticoncepción de emergencia: puede correr el período unos días. Si pasa una semana de la fecha esperada, hacete un test."
        ciclo.any { Relacion.SIN_PRESERVATIVO in it.relaciones } && !d.ajustes.anticonceptivo.metodo.suprimeOvulacion ->
            "Este ciclo anotaste relaciones sin preservativo: un test de embarazo te saca la duda."
        else -> null
    }
}

@Composable
private fun TarjetaMetodo(metodo: Metodo, estado: EstadoMetodo, tomada: Boolean, alTomar: (Boolean) -> Unit, alAplicar: () -> Unit) {
    Tarjeta(color = if (estado.atrasado) Colores.RosaClaro else Colores.Tarjeta) {
        Text(metodo.nombre.uppercase(), style = MaterialTheme.typography.labelSmall, color = Colores.TextoSuave)
        Text(estado.titulo, style = MaterialTheme.typography.titleMedium, color = Colores.Vino)
        estado.tareaHoy?.takeIf { metodo != Metodo.PASTILLA }?.let {
            Text(it, style = MaterialTheme.typography.bodyLarge, color = Colores.TerracotaOscura)
        }
        estado.detalle?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        if (estado.tomaPastilla) {
            if (tomada) {
                OutlinedButton(onClick = { alTomar(false) }, modifier = Modifier.fillMaxWidth()) { Text("✓ Tomada · deshacer") }
            } else {
                Button(onClick = { alTomar(true) }, modifier = Modifier.fillMaxWidth()) { Text("Ya la tomé") }
            }
        }
        if (metodo == Metodo.INYECCION) {
            OutlinedButton(onClick = alAplicar, modifier = Modifier.fillMaxWidth()) { Text("Me la di hoy") }
        }
    }
}

/** Lo que se anotó hoy, en una línea. */
@Composable
private fun ResumenDia(r: RegistroDia, alTocar: () -> Unit) {
    val partes = buildList {
        r.flujo?.let { add("Sangrado ${it.nombre.lowercase()}") }
        addAll(r.sintomas.map { it.nombre })
        addAll(r.animo.map { it.nombre })
        r.energia?.let { add("Energía " + listOf("poca", "normal", "mucha")[it.ordinal]) }
        r.sueno?.let { add("Sueño " + listOf("malo", "regular", "bueno")[it.ordinal]) }
        r.deseo?.let { add("Deseo " + listOf("bajo", "normal", "alto")[it.ordinal]) }
        if (r.relaciones.isNotEmpty() || r.pastillaEmergencia) add("Sexo")
        if (r.nota.isNotBlank()) add("Nota")
    }
    Tarjeta(Modifier.clickable(onClick = alTocar), color = Colores.CremaOscura) {
        Text("Hoy anotaste", style = MaterialTheme.typography.labelMedium, color = Colores.TextoSuave)
        Text(partes.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
    }
}

/** Invitación al minijuego: muestra la pista de la próxima carta sin descubrir. */
@Composable
private fun TarjetaSabias(proxima: Curiosidad?, alTocar: () -> Unit) {
    Tarjeta(Modifier.clickable(onClick = alTocar), color = Colores.Terracota) {
        Text("¿Sabías que…", style = MaterialTheme.typography.titleLarge, color = Colores.Crema)
        Text(
            proxima?.pista?.removePrefix("…") ?: "Completaste la colección y ganaste el Trofeo de Lyfjaberg.",
            style = MaterialTheme.typography.bodyLarge, color = Colores.Crema,
        )
        Text(
            if (proxima != null) "Tocá para dar vuelta la carta" else "Ver mi trofeo",
            style = MaterialTheme.typography.labelMedium, color = Colores.RosaClaro,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirFecha(titulo: String, alElegir: (LocalDate) -> Unit, alCancelar: () -> Unit, futuro: Boolean = false, inicial: LocalDate? = null) {
    val hoyMs = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = inicial?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli() ?: hoyMs,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = futuro || utcTimeMillis <= hoyMs
        },
    )
    DatePickerDialog(
        onDismissRequest = alCancelar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { alElegir(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
            }) { Text("Listo") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } },
        colors = androidx.compose.material3.DatePickerDefaults.colors(containerColor = Colores.Tarjeta),
    ) {
        DatePicker(
            state = estado,
            title = { Text(titulo, Modifier.padding(start = 24.dp, top = 16.dp), style = MaterialTheme.typography.titleMedium) },
            showModeToggle = false,
        )
    }
}
