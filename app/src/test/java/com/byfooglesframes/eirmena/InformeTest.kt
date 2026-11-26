package com.byfooglesframes.eirmena

import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Nivel
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Relacion
import com.byfooglesframes.eirmena.datos.Sintoma
import com.byfooglesframes.eirmena.educacion.Bienestar
import com.byfooglesframes.eirmena.educacion.Estadisticas
import com.byfooglesframes.eirmena.educacion.Etapa
import com.byfooglesframes.eirmena.educacion.Informes
import com.byfooglesframes.eirmena.educacion.OpcionesInforme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InformeTest {
    private val d0 = LocalDate.parse("2026-01-05")
    private val hoy = d0.plusDays(150)

    /** 5 ciclos de 28 días con cólicos los días de período e irritabilidad antes de cada uno. */
    private fun datos(): Datos {
        var r = Datos(Ajustes(bienvenidaHecha = true))
        for (c in 0..5) {
            val inicio = d0.plusDays(28L * c)
            for (k in 0 until 5) r = r.conDia(RegistroDia(inicio.plusDays(k.toLong()), Flujo.MEDIO, sintomas = setOf(Sintoma.COLICOS), energia = Nivel.BAJO))
            if (c > 0) for (k in 1..3) r = r.conDia(r.dia(inicio.minusDays(k.toLong())).copy(animo = setOf(Animo.IRRITABILIDAD)))
        }
        r = r.conDia(r.dia(d0.plusDays(40)).copy(relaciones = setOf(Relacion.SIN_PRESERVATIVO), nota = "nota secreta"))
        return r
    }

    @Test fun estadisticasBasicas() {
        val e = Estadisticas.calcular(datos(), d0, hoy)
        assertEquals(28, e.promedio)
        assertEquals(5, e.periodoPromedio)
        assertEquals(5, e.ciclos.size)
        assertEquals(Sintoma.COLICOS, e.sintomas.first().first)
        // Los cólicos se concentran en la menstruación y la irritabilidad antes del período.
        val colicos = e.sintomasPorFase.first { it.nombre == "Cólicos" }.valores
        assertEquals(1f, colicos[Etapa.MENSTRUAL.ordinal]!!, 0.01f)
        val irrit = e.animoPorFase.first { it.nombre == "Irritabilidad" }.valores
        assertTrue(irrit[Etapa.PREMENSTRUAL.ordinal]!! > 0.5f)
        assertEquals(0f, e.nivelesPorFase.first { it.nombre == "Energía" }.valores[Etapa.MENSTRUAL.ordinal]!!, 0.01f)
    }

    @Test fun informeRespetaLoQueSeEligeCompartir() {
        val privado = Informes.markdown(Informes.armar(datos(), OpcionesInforme(meses = 6), hoy))
        assertTrue("## Resumen" in privado)
        assertTrue("| Inicio | Ciclo (días) |" in privado)
        assertTrue("Cólicos" in privado)
        assertFalse("Salud sexual" in privado)
        assertFalse("nota secreta" in privado)

        val completo = Informes.markdown(Informes.armar(datos(), OpcionesInforme(meses = 6, nombre = "Ana", incluirNotas = true, incluirSexo = true), hoy))
        assertTrue("Informe del ciclo — Ana" in completo)
        assertTrue("## Salud sexual" in completo)
        assertTrue("nota secreta" in completo)
    }

    @Test fun bienestarTieneConsejosParaCadaFase() {
        Etapa.entries.forEach { assertTrue(Bienestar.de(it).size >= 3) }
    }
}
