package com.byfooglesframes.eirmena.avisos

import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.educacion.Gestacion
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Metodos
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class TipoAviso { PERIODO, FERTIL, REGISTRO, PASTILLA, METODO, EMBARAZO }

data class Evento(val momento: LocalDateTime, val tipo: TipoAviso, val titulo: String, val texto: String)

/**
 * Calcula los avisos que corresponden entre dos momentos, solo a partir de los datos.
 * Las notificaciones se programan con esto y se vuelven a calcular cada vez que algo cambia.
 */
object Avisos {
    private val MANANA = LocalTime.of(9, 0)

    /** Eventos con momento en (desde, hasta]. */
    fun eventos(datos: Datos, desde: LocalDateTime, hasta: LocalDateTime): List<Evento> {
        val av = datos.ajustes.avisos
        val ac = datos.ajustes.anticonceptivo
        val hoy = desde.toLocalDate()
        val p = Ciclos.predecir(datos, hoy)
        val lista = mutableListOf<Evento>()
        val embarazo = datos.ajustes.embarazo?.takeIf { datos.ajustes.etapa == EtapaVida.EMBARAZO && it.fin == null }

        var fecha: LocalDate = hoy
        while (fecha <= hasta.toLocalDate()) {
            val f = fecha
            fun en(hora: LocalTime, tipo: TipoAviso, titulo: String, texto: String) {
                lista += Evento(f.atTime(hora), tipo, titulo, texto)
            }

            val proximo = p.proximoInicio.takeIf { embarazo == null }
            if (embarazo != null && av.periodo) {
                // Cada vez que empieza una semana nueva de embarazo.
                val d = Gestacion.dias(embarazo.fum, f)
                if (d > 0 && d % 7 == 0 && d / 7 <= 42) {
                    en(MANANA, TipoAviso.EMBARAZO, "Empezaste la semana ${d / 7}", "Mirá qué pasa esta semana.")
                }
            }
            if (av.periodo && proximo != null && proximo > hoy) {
                // Se avisa con 2 días y se repite cada ciclo estimado (por si no se abre la app).
                var inicio: LocalDate = proximo
                while (inicio.minusDays(2) < f) inicio = inicio.plusDays(p.duracionCiclo.toLong())
                if (inicio.minusDays(2) == f) {
                    en(MANANA, TipoAviso.PERIODO, "Tu período llegaría en 2 días",
                        "Estimado para el ${inicio.dayOfMonth}/${inicio.monthValue}. Tené todo a mano.")
                }
            }

            if (av.fertil && !ac.metodo.suprimeOvulacion && proximo != null) {
                var inicio: LocalDate = proximo
                while (inicio.minusDays(Ciclos.FASE_LUTEA + 5L) < f) inicio = inicio.plusDays(p.duracionCiclo.toLong())
                if (inicio.minusDays(Ciclos.FASE_LUTEA + 5L) == f) {
                    en(MANANA, TipoAviso.FERTIL, "Empieza tu ventana fértil estimada",
                        "Es una estimación: no la uses como método anticonceptivo.")
                }
            }

            if (av.registroDiario) {
                en(av.horaRegistro, TipoAviso.REGISTRO, "¿Cómo estuvo tu día?", "Anotá en un minuto cómo te sentiste hoy.")
            }

            if (av.anticonceptivo) {
                val estado = Metodos.estado(ac, f)
                if (estado != null) {
                    if (ac.metodo == Metodo.PASTILLA) {
                        if (estado.tomaPastilla && !datos.dia(f).pastillaTomada) {
                            en(ac.hora, TipoAviso.PASTILLA, "Hora de la pastilla", estado.titulo)
                            en(ac.hora.plusHours(1), TipoAviso.PASTILLA, "¿Ya tomaste la pastilla?",
                                "Si ya la tomaste, marcala para que no te lo vuelva a recordar.")
                        } else if (!estado.tomaPastilla && estado.detalle?.startsWith("Mañana") == true) {
                            en(ac.hora, TipoAviso.METODO, "Mañana empezás un blíster nuevo", "Dejalo a mano.")
                        }
                    } else if (estado.tareaHoy != null) {
                        val hora = if (ac.metodo.tieneVencimiento) LocalTime.of(10, 0) else ac.hora
                        en(hora, TipoAviso.METODO, estado.tareaHoy, estado.titulo)
                    }
                }
            }
            fecha = fecha.plusDays(1)
        }
        return lista.filter { it.momento > desde && it.momento <= hasta }.sortedBy { it.momento }
    }
}
