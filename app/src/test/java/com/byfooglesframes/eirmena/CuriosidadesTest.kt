package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.datos.Curiosidades
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuriosidadesTest {
    @Test fun nuncaSaleLaMismaDosVecesSeguidas() {
        var anterior: String? = null
        repeat(500) {
            val c = Curiosidades.sortear(anterior)
            assertTrue(c.id != anterior)
            anterior = c.id
        }
    }

    @Test fun puedenSalirRepetidasYTardaMasQue18() {
        // Simula a una persona sacando cartas hasta completar la colección.
        val azar = kotlin.random.Random(3)
        val vistas = mutableSetOf<String>()
        var anterior: String? = null
        var visitas = 0
        while (vistas.size < Curiosidades.todas.size) {
            anterior = Curiosidades.sortear(anterior, azar).id
            vistas += anterior
            visitas++
        }
        assertTrue("hicieron falta $visitas visitas", visitas > Curiosidades.todas.size)
        assertNull(Curiosidades.porId("no-existe"))
    }

    @Test fun completaSoloConTodas() {
        val ids = Curiosidades.todas.map { it.id }
        assertTrue(Curiosidades.completa(ids))
        assertTrue(!Curiosidades.completa(ids.drop(1)))
    }

    @Test fun elTrofeoViajaEnElRespaldo() {
        val d = com.byfooglesframes.eirmena.datos.Datos(
            com.byfooglesframes.eirmena.datos.Ajustes(trofeo = java.time.LocalDate.parse("2026-12-01"))
        )
        val vuelta = com.byfooglesframes.eirmena.datos.Json.desdeJson(com.byfooglesframes.eirmena.datos.Json.aJson(d))
        assertEquals(java.time.LocalDate.parse("2026-12-01"), vuelta.ajustes.trofeo)
    }
}
