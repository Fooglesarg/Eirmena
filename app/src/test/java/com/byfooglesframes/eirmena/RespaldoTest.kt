package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.ContrasenaIncorrecta
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.FormatoInvalido
import com.byfooglesframes.eirmena.datos.Json
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Respaldo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RespaldoTest {
    private val datos = Datos(
        Ajustes(cicloHabitual = 31, periodoHabitual = 4, bienvenidaHecha = true, ocultarEnRecientes = false, curiosidades = listOf("eir", "mena")),
        listOf(
            RegistroDia(LocalDate.parse("2026-03-01"), Flujo.ABUNDANTE, "cólicos, \"comillas\" y ñ"),
            RegistroDia(LocalDate.parse("2026-03-02"), Flujo.MANCHADO),
            RegistroDia(LocalDate.parse("2026-03-10"), null, "solo nota"),
            RegistroDia(LocalDate.parse("2026-03-11"), sintomas = setOf(com.byfooglesframes.eirmena.datos.Sintoma.ACNE), animo = setOf(com.byfooglesframes.eirmena.datos.Animo.CALMA), energia = com.byfooglesframes.eirmena.datos.Nivel.ALTO, relaciones = setOf(com.byfooglesframes.eirmena.datos.Relacion.SIN_PRESERVATIVO), pastillaEmergencia = true, pastillaTomada = true),
        ).associateBy { it.fecha },
    )

    @Test fun jsonIdaYVuelta() {
        assertEquals(datos, Json.desdeJson(Json.aJson(datos)))
    }

    @Test fun cifradoIdaYVuelta() {
        val archivo = Respaldo.cifrar(Json.aJson(datos), "clave segura".toCharArray(), iteraciones = 1000)
        assertTrue(Respaldo.estaCifrado(archivo))
        assertFalse(archivo.contains("cólicos"))
        assertEquals(datos, Json.desdeJson(Respaldo.descifrar(archivo, "clave segura".toCharArray())))
    }

    @Test(expected = ContrasenaIncorrecta::class)
    fun contrasenaIncorrecta() {
        val archivo = Respaldo.cifrar(Json.aJson(datos), "clave segura".toCharArray(), iteraciones = 1000)
        Respaldo.descifrar(archivo, "otra".toCharArray())
    }

    @Test(expected = FormatoInvalido::class)
    fun archivoAjeno() {
        Json.desdeJson("{\"hola\": 1}")
    }

    @Test fun combinarGanaLoImportado() {
        val otro = Datos(dias = mapOf(LocalDate.parse("2026-03-01") to RegistroDia(LocalDate.parse("2026-03-01"), Flujo.LEVE)))
        val r = datos.combinarCon(otro)
        assertEquals(Flujo.LEVE, r.dias[LocalDate.parse("2026-03-01")]!!.flujo)
        assertEquals(4, r.dias.size)
        assertEquals(31, r.ajustes.cicloHabitual)
    }
}
