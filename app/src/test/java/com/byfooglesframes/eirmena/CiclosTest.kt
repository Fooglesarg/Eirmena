package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.ciclo.Ciclos
import com.byfooglesframes.eirmena.ciclo.Fase
import com.byfooglesframes.eirmena.ciclo.Marca
import com.byfooglesframes.eirmena.ciclo.Regularidad
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.RegistroDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CiclosTest {
    private fun d(s: String) = LocalDate.parse(s)

    /** Períodos de [dias] días que empiezan en cada fecha. */
    private fun datos(vararg inicios: String, dias: Int = 5, ciclo: Int = 28): Datos {
        var r = Datos(ajustes = Datos().ajustes.copy(cicloHabitual = ciclo, bienvenidaHecha = true))
        for (i in inicios) for (k in 0 until dias) r = r.conDia(RegistroDia(d(i).plusDays(k.toLong()), Flujo.MEDIO))
        return r
    }

    @Test fun agrupaDiasConHuecoCortoEnUnSoloPeriodo() {
        var r = datos("2026-01-01", dias = 2)
        r = r.conDia(RegistroDia(d("2026-01-05"), Flujo.LEVE)) // hueco de 2 días
        val p = Ciclos.periodos(r.dias)
        assertEquals(1, p.size)
        assertEquals(5, p[0].dias)
    }

    @Test fun manchadoNoEmpiezaPeriodo() {
        val r = Datos().conDia(RegistroDia(d("2026-01-01"), Flujo.MANCHADO))
        assertTrue(Ciclos.periodos(r.dias).isEmpty())
    }

    @Test fun sinDatosUsaElCicloHabitual() {
        val r = datos("2026-01-01", ciclo = 30)
        val p = Ciclos.predecir(r, d("2026-01-10"))
        assertEquals(30, p.duracionCiclo)
        assertEquals(d("2026-01-31"), p.proximoInicio)
        assertEquals(Regularidad.POCOS_DATOS, p.regularidad)
        assertEquals(3, p.rango)
    }

    @Test fun ciclosRegularesDan28() {
        val r = datos("2026-01-01", "2026-01-29", "2026-02-26", "2026-03-26")
        val p = Ciclos.predecir(r, d("2026-04-01"))
        assertEquals(28, p.duracionCiclo)
        assertEquals(Regularidad.REGULAR, p.regularidad)
        assertEquals(1, p.rango)
        assertEquals(d("2026-04-23"), p.proximoInicio)
        assertEquals(5, p.duracionPeriodo)
    }

    @Test fun losCiclosRecientesPesanMas() {
        // 26, 26, 26 y después 32, 32
        val r = datos("2026-01-01", "2026-01-27", "2026-02-22", "2026-03-20", "2026-04-21", "2026-05-23")
        val p = Ciclos.predecir(r, d("2026-05-30"))
        assertTrue("debería acercarse a 32: ${p.duracionCiclo}", p.duracionCiclo in 29..31)
    }

    @Test fun ignoraCiclosAbsurdos() {
        // Un hueco de 120 días (no se anotó) no arruina el promedio.
        val r = datos("2026-01-01", "2026-05-01", "2026-05-29", "2026-06-26")
        val p = Ciclos.predecir(r, d("2026-07-01"))
        assertEquals(28, p.duracionCiclo)
    }

    @Test fun detectaRetraso() {
        val r = datos("2026-01-01", "2026-01-29", "2026-02-26", "2026-03-26")
        val p = Ciclos.predecir(r, d("2026-04-30"))
        val h = Ciclos.hoy(r, p, d("2026-04-30"))!!
        assertEquals(7, h.retraso)
        assertTrue(h.retrasoFueraDeRango)
    }

    @Test fun faseSegunElDia() {
        val r = datos("2026-01-01", "2026-01-29", "2026-02-26", "2026-03-26")
        val p = Ciclos.predecir(r, d("2026-03-27"))
        assertEquals(Fase.MENSTRUAL, Ciclos.hoy(r, p, d("2026-03-27"))!!.fase)
        assertEquals(Fase.FOLICULAR, Ciclos.hoy(r, p, d("2026-04-02"))!!.fase)
        assertEquals(Fase.FERTIL, Ciclos.hoy(r, p, d("2026-04-08"))!!.fase) // ovulación estimada 9/4
        assertEquals(Fase.LUTEA, Ciclos.hoy(r, p, d("2026-04-15"))!!.fase)
    }

    @Test fun marcasFuturas() {
        val r = datos("2026-01-01", "2026-01-29", "2026-02-26", "2026-03-26")
        val p = Ciclos.predecir(r, d("2026-04-01"))
        val m = Ciclos.marcas(p, d("2026-04-01"), d("2026-04-30"), d("2026-04-01"))
        assertEquals(Marca.OVULACION, m[d("2026-04-09")])
        assertEquals(Marca.FERTIL, m[d("2026-04-04")])
        assertEquals(Marca.PERIODO, m[d("2026-04-23")])
        assertNull(m[d("2026-04-15")])
    }

    @Test fun sinPeriodosNoHayEstado() {
        val r = Datos()
        assertNull(Ciclos.hoy(r, Ciclos.predecir(r)))
    }
}
