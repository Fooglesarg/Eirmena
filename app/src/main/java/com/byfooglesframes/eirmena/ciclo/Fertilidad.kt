package com.byfooglesframes.eirmena.ciclo

import com.byfooglesframes.eirmena.datos.Datos
import com.byfooglesframes.eirmena.datos.Test
import java.time.LocalDate

enum class FuenteOvulacion(val nombre: String) {
    TEMPERATURA("por tu temperatura basal"),
    TEST_LH("por tu test de ovulación"),
}

data class OvulacionDetectada(val fecha: LocalDate, val fuente: FuenteOvulacion, val lineaBase: Double? = null)

/**
 * Detecta la ovulación de un ciclo con lo que anotó la persona.
 *
 * Temperatura basal (regla "3 sobre 6"): la ovulación se confirma cuando hay 3 temperaturas seguidas por
 * encima de la más alta de las 6 anteriores (la "línea base"), y la tercera la supera por 0,2 °C o más.
 * La ovulación se ubica el día anterior a la primera temperatura alta.
 * Test de LH: un positivo indica que la ovulación ocurre en las 24 a 36 horas siguientes.
 * La temperatura confirma que ya pasó; el test la anticipa. Si hay ambos, manda la temperatura.
 */
object Fertilidad {
    fun detectar(datos: Datos, desde: LocalDate, hasta: LocalDate): OvulacionDetectada? {
        porTemperatura(datos, desde, hasta)?.let { return it }
        val positivo = datos.dias.values
            .filter { it.fecha in desde..hasta && it.testOvulacion == Test.POSITIVO }
            .minByOrNull { it.fecha } ?: return null
        return OvulacionDetectada(positivo.fecha.plusDays(1), FuenteOvulacion.TEST_LH)
    }

    fun temperaturas(datos: Datos, desde: LocalDate, hasta: LocalDate): List<Pair<LocalDate, Double>> =
        datos.dias.values.filter { it.fecha in desde..hasta && it.temperatura != null }
            .sortedBy { it.fecha }.map { it.fecha to it.temperatura!! }

    private fun porTemperatura(datos: Datos, desde: LocalDate, hasta: LocalDate): OvulacionDetectada? {
        val t = temperaturas(datos, desde, hasta)
        for (i in 6..t.size - 3) {
            val base = t.subList(i - 6, i).maxOf { it.second }
            val altas = t.subList(i, i + 3)
            if (altas.all { it.second > base } && altas.last().second >= base + 0.2 - 1e-9) {
                return OvulacionDetectada(altas.first().first.minusDays(1), FuenteOvulacion.TEMPERATURA, base)
            }
        }
        return null
    }
}
