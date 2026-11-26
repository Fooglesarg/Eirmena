package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.datos.Anticonceptivo
import com.byfooglesframes.eirmena.datos.Esquema
import com.byfooglesframes.eirmena.datos.Frecuencia
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.Metodos
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MetodosTest {
    private val inicio = LocalDate.parse("2026-11-01")
    private fun pastilla(e: Esquema) = Anticonceptivo(Metodo.PASTILLA, inicio, esquema = e)

    @Test fun pastilla21mas7() {
        val a = pastilla(Esquema.E21_7)
        assertEquals("Pastilla 1 de 21", Metodos.estado(a, inicio)!!.titulo)
        assertTrue(Metodos.estado(a, inicio.plusDays(20))!!.tomaPastilla)
        val descanso = Metodos.estado(a, inicio.plusDays(21))!!
        assertFalse(descanso.tomaPastilla)
        assertEquals("Descanso: día 1 de 7", descanso.titulo)
        assertEquals("Mañana empezás un blíster nuevo.", Metodos.estado(a, inicio.plusDays(27))!!.detalle)
        // Segundo blíster
        assertEquals("Pastilla 1 de 21", Metodos.estado(a, inicio.plusDays(28))!!.titulo)
    }

    @Test fun placeboSeToma() {
        val e = Metodos.estado(pastilla(Esquema.E24_4), inicio.plusDays(25))!!
        assertTrue(e.tomaPastilla)
        assertEquals("Placebo 2 de 4", e.titulo)
    }

    @Test fun antesDelInicioNoHayEstado() {
        assertNull(Metodos.estado(pastilla(Esquema.E21_7), inicio.minusDays(1)))
    }

    @Test fun parcheCambiaCadaSemana() {
        val a = Anticonceptivo(Metodo.PARCHE, inicio)
        assertEquals("Hoy toca poner un parche nuevo", Metodos.estado(a, inicio)!!.tareaHoy)
        assertEquals("Hoy toca cambiar el parche", Metodos.estado(a, inicio.plusDays(7))!!.tareaHoy)
        assertEquals("Hoy toca sacar el parche", Metodos.estado(a, inicio.plusDays(21))!!.tareaHoy)
        assertNull(Metodos.estado(a, inicio.plusDays(3))!!.tareaHoy)
    }

    @Test fun inyeccionTrimestral() {
        val a = Anticonceptivo(Metodo.INYECCION, inicio, frecuencia = Frecuencia.TRIMESTRAL)
        assertEquals("Hoy toca la inyección", Metodos.estado(a, inicio.plusDays(90))!!.titulo)
        assertTrue(Metodos.estado(a, inicio.plusDays(95))!!.atrasado)
    }

    @Test fun diuVence() {
        val a = Anticonceptivo(Metodo.DIU_COBRE, inicio, vence = inicio.plusYears(10))
        assertEquals("Tu método vence en 30 días", Metodos.estado(a, inicio.plusYears(10).minusDays(30))!!.tareaHoy)
        assertTrue(Metodos.estado(a, inicio.plusYears(10).plusDays(1))!!.atrasado)
    }
}
