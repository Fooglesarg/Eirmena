package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.avisos.Avisos
import com.byfooglesframes.eirmena.avisos.TipoAviso
import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Fertilidad
import com.byfooglesframes.eirmena.ciclo.FuenteOvulacion
import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Embarazo
import com.byfooglesframes.eirmena.datos.EtapaVida
import com.byfooglesframes.eirmena.datos.FinEmbarazo
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Json
import com.byfooglesframes.eirmena.datos.Moco
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Test
import com.byfooglesframes.eirmena.educacion.Alertas
import com.byfooglesframes.eirmena.educacion.Gestacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test as Prueba
import java.time.LocalDate

class EtapasTest {
    private val d0 = LocalDate.parse("2026-03-01")

    private fun conPeriodo(ajustes: Ajustes = Ajustes(bienvenidaHecha = true)): Datos {
        var r = Datos(ajustes)
        for (k in 0 until 5) r = r.conDia(RegistroDia(d0.plusDays(k.toLong()), Flujo.MEDIO))
        return r
    }

    /** Temperaturas bajas hasta el día [subida] (base 0) y altas desde ahí. */
    private fun conTemperaturas(d: Datos, subida: Int): Datos {
        var r = d
        for (k in 5..24) {
            val t = if (k < subida) 36.30 + (k % 3) * 0.05 else 36.75
            r = r.conDia(r.dia(d0.plusDays(k.toLong())).copy(temperatura = t))
        }
        return r
    }

    @Prueba fun ovulacionPorTemperatura() {
        val d = conTemperaturas(conPeriodo(), subida = 16)
        val o = Fertilidad.detectar(d, d0.plusDays(6), d0.plusDays(24))!!
        assertEquals(FuenteOvulacion.TEMPERATURA, o.fuente)
        assertEquals(d0.plusDays(15), o.fecha)
        // El período estimado pasa a ser ovulación + 14.
        val p = Ciclos.predecir(d, d0.plusDays(24))
        assertEquals(d0.plusDays(29), p.proximoInicio)
        assertEquals(2, p.rango)
    }

    @Prueba fun sinSubidaNoHayOvulacion() {
        val d = conTemperaturas(conPeriodo(), subida = 99)
        assertNull(Fertilidad.detectar(d, d0.plusDays(6), d0.plusDays(24)))
    }

    @Prueba fun ovulacionPorTestLH() {
        var d = conPeriodo()
        d = d.conDia(d.dia(d0.plusDays(12)).copy(testOvulacion = Test.POSITIVO))
        val o = Fertilidad.detectar(d, d0.plusDays(6), d0.plusDays(20))!!
        assertEquals(FuenteOvulacion.TEST_LH, o.fuente)
        assertEquals(d0.plusDays(13), o.fecha)
    }

    @Prueba fun semanasDeEmbarazo() {
        val fum = LocalDate.parse("2026-01-01")
        assertEquals(LocalDate.parse("2026-10-08"), Embarazo(fum).fechaProbable)
        assertEquals(12, Gestacion.semanas(fum, fum.plusDays(87)))
        assertEquals(3, Gestacion.diasExtra(fum, fum.plusDays(87)))
        assertEquals(1, Gestacion.trimestre(12))
        assertEquals(2, Gestacion.trimestre(20))
        assertEquals(3, Gestacion.trimestre(30))
        (1..42).forEach { assertTrue(Gestacion.semana(it).bebe.isNotBlank()) }
        assertEquals(20, Gestacion.semana(20).numero)
    }

    @Prueba fun enEmbarazoNoHayAlertasNiAvisosDePeriodo() {
        val a = Ajustes(bienvenidaHecha = true, etapa = EtapaVida.EMBARAZO, embarazo = Embarazo(d0))
        val d = conPeriodo(a)
        assertTrue(Alertas.calcular(d, d0.plusDays(150)).isEmpty())
        val ev = Avisos.eventos(d, d0.plusDays(10).atStartOfDay(), d0.plusDays(40).atStartOfDay())
        assertTrue(ev.none { it.tipo == TipoAviso.PERIODO || it.tipo == TipoAviso.FERTIL })
        assertTrue(ev.any { it.tipo == TipoAviso.EMBARAZO && it.titulo == "Empezaste la semana 2" })
    }

    @Prueba fun perimenopausiaNoJuzgaCiclosPeroSiSangradoTardio() {
        var d = conPeriodo(Ajustes(bienvenidaHecha = true, etapa = EtapaVida.PERIMENOPAUSIA))
        assertFalse(Alertas.calcular(d, d0.plusDays(200)).any { it.id == "sin-periodo" })
        d = d.conDia(RegistroDia(d0.plusDays(400), Flujo.LEVE))
        assertTrue(Alertas.calcular(d, d0.plusDays(410)).any { it.id == "posmenopausia" })
    }

    @Prueba fun primerosCiclosAceptanCiclosLargos() {
        var d = Datos(Ajustes(bienvenidaHecha = true, etapa = EtapaVida.PRIMEROS_CICLOS))
        listOf(0, 42, 84, 126).forEach { s -> for (k in 0 until 5) d = d.conDia(RegistroDia(d0.plusDays((s + k).toLong()), Flujo.MEDIO)) }
        assertFalse(Alertas.calcular(d, d0.plusDays(140)).any { it.id == "ciclos-largos" })
        val adulta = d.copy(ajustes = d.ajustes.copy(etapa = EtapaVida.CICLO))
        assertTrue(Alertas.calcular(adulta, d0.plusDays(140)).any { it.id == "ciclos-largos" })
    }

    @Prueba fun respaldoConEtapasYFertilidad() {
        val d = Datos(
            Ajustes(etapa = EtapaVida.CICLO, embarazo = Embarazo(d0, d0.plusDays(200), FinEmbarazo.NACIMIENTO)),
            mapOf(d0 to RegistroDia(d0, temperatura = 36.55, moco = Moco.CLARA, testOvulacion = Test.POSITIVO, testEmbarazo = Test.NEGATIVO)),
        )
        assertEquals(d, Json.desdeJson(Json.aJson(d)))
    }
}
