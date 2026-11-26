package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.avisos.Avisos
import com.byfooglesframes.eirmena.avisos.TipoAviso
import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Anticonceptivo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.RegistroDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import com.byfooglesframes.eirmena.datos.Avisos as AjustesAvisos

class AvisosTest {
    private val d1 = LocalDate.parse("2026-11-01")

    private fun base(ajustes: Ajustes = Ajustes(bienvenidaHecha = true)): Datos {
        var r = Datos(ajustes)
        for (k in 0 until 5) r = r.conDia(RegistroDia(d1.plusDays(k.toLong()), Flujo.MEDIO))
        return r
    }

    @Test fun avisoDelPeriodoDosDiasAntes() {
        val ev = Avisos.eventos(base(), d1.plusDays(5).atStartOfDay(), d1.plusDays(40).atStartOfDay())
        val periodo = ev.filter { it.tipo == TipoAviso.PERIODO }
        // Ciclo habitual 28: próximo el 29/11, aviso el 27/11 a las 9.
        assertEquals(LocalDateTime.parse("2026-11-27T09:00"), periodo.first().momento)
    }

    @Test fun pastillaConRepasoSiNoSeTomo() {
        val a = Ajustes(bienvenidaHecha = true, anticonceptivo = Anticonceptivo(Metodo.PASTILLA, d1, LocalTime.of(21, 0)))
        var datos = base(a)
        val desde = d1.plusDays(10).atTime(8, 0)
        val hasta = d1.plusDays(10).atTime(23, 0)
        val ev = Avisos.eventos(datos, desde, hasta).filter { it.tipo == TipoAviso.PASTILLA }
        assertEquals(listOf(LocalTime.of(21, 0), LocalTime.of(22, 0)), ev.map { it.momento.toLocalTime() })

        datos = datos.conDia(datos.dia(d1.plusDays(10)).copy(pastillaTomada = true))
        assertTrue(Avisos.eventos(datos, desde, hasta).none { it.tipo == TipoAviso.PASTILLA })
    }

    @Test fun sinVentanaFertilConMetodoHormonal() {
        val a = Ajustes(
            bienvenidaHecha = true,
            anticonceptivo = Anticonceptivo(Metodo.IMPLANTE, d1),
            avisos = AjustesAvisos(fertil = true),
        )
        val ev = Avisos.eventos(base(a), d1.atStartOfDay(), d1.plusDays(60).atStartOfDay())
        assertTrue(ev.none { it.tipo == TipoAviso.FERTIL })
    }

    @Test fun registroDiario() {
        val a = Ajustes(bienvenidaHecha = true, avisos = AjustesAvisos(periodo = false, registroDiario = true))
        val ev = Avisos.eventos(base(a), d1.atTime(22, 0), d1.plusDays(3).atTime(22, 0))
        assertEquals(3, ev.count { it.tipo == TipoAviso.REGISTRO })
    }
}
