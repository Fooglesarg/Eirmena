package com.byfooglesframes.eirmena.ui

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object Fechas {
    private val es = Locale.forLanguageTag("es-AR")
    private val diaMes = DateTimeFormatter.ofPattern("d 'de' MMMM", es)
    private val diaMesAnio = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", es)
    private val semana = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", es)
    private val corta = DateTimeFormatter.ofPattern("d MMM", es)
    private val mes = DateTimeFormatter.ofPattern("MMMM yyyy", es)

    /** "26 de noviembre" (agrega el año si no es el actual). */
    fun larga(f: LocalDate, hoy: LocalDate = LocalDate.now()): String =
        if (f.year == hoy.year) diaMes.format(f) else diaMesAnio.format(f)

    /** "Jueves 26 de noviembre" */
    fun conSemana(f: LocalDate): String = semana.format(f).replaceFirstChar { it.uppercase() }

    /** "26 nov" */
    fun corta(f: LocalDate): String = corta.format(f).trimEnd('.')

    /** "Noviembre 2026" */
    fun mes(m: YearMonth): String = mes.format(m).replaceFirstChar { it.uppercase() }

    /** "entre el 3 y el 9 de diciembre" o "entre el 28 de noviembre y el 4 de diciembre" */
    fun rango(desde: LocalDate, hasta: LocalDate): String =
        if (desde.month == hasta.month) "entre el ${desde.dayOfMonth} y el ${larga(hasta)}"
        else "entre el ${larga(desde)} y el ${larga(hasta)}"

    fun dias(n: Int): String = if (n == 1) "1 día" else "$n días"
}
