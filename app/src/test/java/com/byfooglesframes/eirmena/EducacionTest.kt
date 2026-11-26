package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.Anticonceptivo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.educacion.Alertas
import com.byfooglesframes.eirmena.educacion.Biblioteca
import com.byfooglesframes.eirmena.educacion.Etapa
import com.byfooglesframes.eirmena.educacion.Hormonas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EducacionTest {
    private val d0 = LocalDate.parse("2026-01-01")

    /** Períodos que empiezan cada [ciclos] días, con [dias] de sangrado [flujo]. */
    private fun datos(ciclos: List<Int>, dias: Int = 5, flujo: Flujo = Flujo.MEDIO, ajustes: Ajustes = Ajustes(bienvenidaHecha = true)): Datos {
        var r = Datos(ajustes)
        var inicio = d0
        for (c in ciclos + 0) {
            for (k in 0 until dias) r = r.conDia(RegistroDia(inicio.plusDays(k.toLong()), flujo))
            inicio = inicio.plusDays(c.toLong())
        }
        return r
    }

    private fun ids(d: Datos, hoy: LocalDate) = Alertas.calcular(d, hoy).map { it.id }

    @Test fun ciclosNormalesNoAlertan() {
        val d = datos(listOf(28, 29, 27, 28, 30))
        assertTrue(ids(d, d0.plusDays(150)).isEmpty())
    }

    @Test fun ciclosLargosEIrregulares() {
        val d = datos(listOf(28, 45, 30, 42, 44))
        val a = ids(d, d0.plusDays(190))
        assertTrue("ciclos-largos" in a)
        assertTrue("irregulares" in a)
    }

    @Test fun conPastillaNoSeJuzganLosCiclos() {
        val d = datos(listOf(28, 45, 30, 42, 44), ajustes = Ajustes(anticonceptivo = Anticonceptivo(Metodo.PASTILLA, d0)))
        assertFalse("irregulares" in ids(d, d0.plusDays(190)))
    }

    @Test fun mesesSinPeriodo() {
        val d = datos(listOf(28))
        assertTrue("sin-periodo" in ids(d, d0.plusDays(130)))
        val conImplante = d.copy(ajustes = d.ajustes.copy(anticonceptivo = Anticonceptivo(Metodo.IMPLANTE, d0)))
        assertFalse("sin-periodo" in ids(conImplante, d0.plusDays(130)))
    }

    @Test fun sangradoAbundante() {
        val d = datos(listOf(28, 28, 28), flujo = Flujo.ABUNDANTE)
        assertTrue("abundante" in ids(d, d0.plusDays(100)))
    }

    @Test fun animoPremenstrual() {
        var d = datos(listOf(28, 28, 28, 28))
        for (c in 2..4) { // los 3 ciclos más recientes
            val inicio = d0.plusDays(28L * c)
            for (k in 1..3) d = d.conDia(d.dia(inicio.minusDays(k.toLong())).copy(animo = setOf(Animo.IRRITABILIDAD)))
        }
        assertTrue("spm" in ids(d, d0.plusDays(120)))
    }

    @Test fun alertaVistaSeOcultaUnTiempo() {
        val d = datos(listOf(28))
        val hoy = d0.plusDays(130)
        val a = Alertas.calcular(d, hoy)
        assertTrue(Alertas.visibles(a, mapOf("sin-periodo" to hoy), hoy).none { it.id == "sin-periodo" })
        assertTrue(Alertas.visibles(a, mapOf("sin-periodo" to hoy.minusDays(61)), hoy).any { it.id == "sin-periodo" })
    }

    @Test fun hormonasEnSuLugar() {
        val ov = Hormonas.diaOvulacion(28)
        // El pico de LH está alrededor de la ovulación y la progesterona alta en la fase lútea.
        val lhPico = (1..28).maxBy { Hormonas.niveles(it.toFloat(), 28).lh }
        assertTrue(lhPico in (ov - 2)..ov)
        val progPico = (1..28).maxBy { Hormonas.niveles(it.toFloat(), 28).progesterona }
        assertTrue(progPico in (ov + 5)..(ov + 9))
        assertEquals(Etapa.MENSTRUAL, Hormonas.etapa(2, 28, 5))
        assertEquals(Etapa.OVULATORIA, Hormonas.etapa(ov, 28, 5))
        assertEquals(Etapa.PREMENSTRUAL, Hormonas.etapa(27, 28, 5))
    }

    @Test fun articulosCompletos() {
        val ids = Biblioteca.articulos.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        Biblioteca.articulos.forEach { assertTrue(it.secciones.isNotEmpty() && it.fuentes.isNotEmpty()) }
        assertTrue("guardia" in ids && "lineas" in ids)
    }
}
