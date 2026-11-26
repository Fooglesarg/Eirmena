package com.byfooglesframes.eirmena

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.byfooglesframes.eirmena.datos.Ajustes
import com.byfooglesframes.eirmena.datos.Animo
import com.byfooglesframes.eirmena.datos.Anticonceptivo
import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Flujo
import com.byfooglesframes.eirmena.datos.Metodo
import com.byfooglesframes.eirmena.datos.RegistroDia
import com.byfooglesframes.eirmena.datos.Sintoma
import com.byfooglesframes.eirmena.educacion.InformePdf
import com.byfooglesframes.eirmena.educacion.Informes
import com.byfooglesframes.eirmena.educacion.OpcionesInforme
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

/** Genera un informe de ejemplo para revisarlo a ojo (queda en files/informe-prueba.pdf de la app de depuración). */
@RunWith(AndroidJUnit4::class)
class InformePdfTest {
    @Test fun generarPdfDeEjemplo() {
        val hoy = LocalDate.now()
        var d = Datos(Ajustes(bienvenidaHecha = true, anticonceptivo = Anticonceptivo(Metodo.PRESERVATIVO)))
        val largos = listOf(28, 31, 27, 29, 33, 28, 30)
        var inicio = hoy.minusDays(largos.sum().toLong() + 3)
        largos.forEachIndexed { i, l ->
            for (k in 0 until 5) d = d.conDia(RegistroDia(inicio.plusDays(k.toLong()), if (k < 2 && i % 2 == 0) Flujo.ABUNDANTE else Flujo.MEDIO,
                sintomas = if (k < 3) setOf(Sintoma.COLICOS, Sintoma.CANSANCIO) else setOf(Sintoma.DOLOR_ESPALDA)))
            for (k in 1..3) d = d.conDia(d.dia(inicio.plusDays(l - k.toLong())).copy(animo = setOf(Animo.IRRITABILIDAD), sintomas = setOf(Sintoma.HINCHAZON)))
            inicio = inicio.plusDays(l.toLong())
        }
        for (k in 0 until 3) d = d.conDia(RegistroDia(inicio.plusDays(k.toLong()), Flujo.MEDIO))
        val informe = Informes.armar(d, OpcionesInforme(meses = 12, nombre = "Ejemplo", incluirNotas = true), hoy)
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val archivo = File(contexto.filesDir, "informe-prueba.pdf")
        archivo.outputStream().use { InformePdf.escribir(informe, it) }
        assertTrue(archivo.length() > 1000)
    }
}
