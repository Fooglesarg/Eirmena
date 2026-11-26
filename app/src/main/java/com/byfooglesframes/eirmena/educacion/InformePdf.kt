package com.byfooglesframes.eirmena.educacion

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream

/** Dibuja el informe en un PDF A4, con saltos de página automáticos. */
object InformePdf {
    private const val ANCHO = 595
    private const val ALTO = 842
    private const val MARGEN = 48f
    private const val TERRACOTA = 0xFFC8694F.toInt()
    private const val VINO = 0xFF6E3036.toInt()
    private const val TEXTO = 0xFF3A2925.toInt()
    private const val SUAVE = 0xFF7D6760.toInt()
    private const val LINEA = 0xFFE6D5C7.toInt()
    private const val FONDO_TABLA = 0xFFF6DED7.toInt()

    fun escribir(informe: Informe, salida: OutputStream) {
        val doc = PdfDocument()
        Escritor(doc, informe).dibujar()
        doc.writeTo(salida)
        doc.close()
    }

    private class Escritor(val doc: PdfDocument, val informe: Informe) {
        private var numero = 0
        private lateinit var pagina: PdfDocument.Page
        private lateinit var lienzo: Canvas
        private var y = 0f
        private val ancho = ANCHO - 2 * MARGEN

        private fun pincel(tam: Float, color: Int = TEXTO, negrita: Boolean = false) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = tam
            this.color = color
            typeface = Typeface.create(Typeface.SANS_SERIF, if (negrita) Typeface.BOLD else Typeface.NORMAL)
        }

        private val cuerpo = pincel(10.5f)
        private val titulo = pincel(14f, VINO, true)
        private val chico = pincel(8.5f, SUAVE)

        fun dibujar() {
            nuevaPagina()
            // Encabezado
            lienzo.drawRect(0f, 0f, ANCHO.toFloat(), 6f, Paint().apply { color = TERRACOTA })
            lienzo.drawText(informe.titulo, MARGEN, y + 18f, pincel(20f, VINO, true))
            y += 30f
            lienzo.drawText(informe.subtitulo, MARGEN, y + 10f, chico)
            y += 24f

            informe.bloques.forEach { b ->
                when (b) {
                    is Bloque.Titulo -> { espacio(80f); y += 10f; // un título nunca queda solo al pie de la página
                         lienzo.drawText(b.texto, MARGEN, y + 12f, titulo); y += 22f }
                    is Bloque.Parrafo -> parrafo(b.texto, MARGEN, ancho)
                    is Bloque.Lista -> b.items.forEach { item ->
                        espacio(16f)
                        lienzo.drawCircle(MARGEN + 3f, y + 7f, 1.8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TERRACOTA })
                        parrafo(item, MARGEN + 12f, ancho - 12f)
                    }
                    is Bloque.Tabla -> tabla(b)
                    is Bloque.BarrasCiclos -> barras(b.ciclos)
                }
            }
            espacio(40f)
            y += 12f
            lienzo.drawLine(MARGEN, y, ANCHO - MARGEN, y, Paint().apply { color = LINEA })
            y += 6f
            parrafo(informe.pie, MARGEN, ancho, chico)
            terminarPagina()
        }

        private fun nuevaPagina() {
            numero++
            pagina = doc.startPage(PdfDocument.PageInfo.Builder(ANCHO, ALTO, numero).create())
            lienzo = pagina.canvas
            y = MARGEN
        }

        private fun terminarPagina() {
            lienzo.drawText("Eirmena · página $numero", MARGEN, ALTO - 24f, chico)
            doc.finishPage(pagina)
        }

        /** Si no entra [alto] en lo que queda de la página, pasa a la siguiente. */
        private fun espacio(alto: Float) {
            if (y + alto > ALTO - MARGEN - 10f) { terminarPagina(); nuevaPagina() }
        }

        private fun parrafo(texto: String, x: Float, ancho: Float, p: Paint = cuerpo) {
            val alto = p.textSize * 1.4f
            renglones(texto, ancho, p).forEach { r ->
                espacio(alto)
                lienzo.drawText(r, x, y + p.textSize, p)
                y += alto
            }
            y += 3f
        }

        private fun renglones(texto: String, ancho: Float, p: Paint): List<String> {
            val salida = mutableListOf<String>()
            texto.split("\n").forEach { linea ->
                var actual = ""
                linea.split(" ").forEach { palabra ->
                    val prueba = if (actual.isEmpty()) palabra else "$actual $palabra"
                    if (p.measureText(prueba) <= ancho) actual = prueba
                    else { if (actual.isNotEmpty()) salida += actual; actual = palabra }
                }
                salida += actual
            }
            return salida
        }

        private fun tabla(t: Bloque.Tabla) {
            val col = ancho / t.encabezados.size
            val alto = 18f
            fun fila(celdas: List<String>, negrita: Boolean, fondo: Int?) {
                espacio(alto)
                fondo?.let { lienzo.drawRect(MARGEN, y, MARGEN + ancho, y + alto, Paint().apply { color = it }) }
                val p = pincel(9.5f, if (negrita) VINO else TEXTO, negrita)
                celdas.forEachIndexed { i, c -> lienzo.drawText(c, MARGEN + 6f + i * col, y + 12.5f, p) }
                lienzo.drawLine(MARGEN, y + alto, MARGEN + ancho, y + alto, Paint().apply { color = LINEA })
                y += alto
            }
            fila(t.encabezados, true, FONDO_TABLA)
            t.filas.forEach { fila(it, false, null) }
            y += 6f
        }

        /** Barras de duración de cada ciclo con la franja normal (24 a 38 días) sombreada. */
        private fun barras(ciclos: List<CicloResumen>) {
            val alto = 120f
            espacio(alto + 30f)
            val maximo = maxOf(ciclos.maxOf { it.duracion }, 40).toFloat()
            val base = y + alto
            fun yDe(v: Float) = base - v / maximo * alto
            lienzo.drawRect(MARGEN, yDe(38f), MARGEN + ancho, yDe(24f), Paint().apply { color = 0x22C8694F })
            lienzo.drawText("24–38 días: rango normal", MARGEN + ancho - 110f, yDe(38f) - 3f, chico)
            val paso = ancho / ciclos.size
            val anchoBarra = minOf(paso * 0.6f, 28f)
            ciclos.forEachIndexed { i, c ->
                val x = MARGEN + i * paso + (paso - anchoBarra) / 2
                lienzo.drawRoundRect(RectF(x, yDe(c.duracion.toFloat()), x + anchoBarra, base), 3f, 3f,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = TERRACOTA })
                val v = "${c.duracion}"
                lienzo.drawText(v, x + anchoBarra / 2 - chico.measureText(v) / 2, yDe(c.duracion.toFloat()) - 3f, chico)
            }
            lienzo.drawLine(MARGEN, base, MARGEN + ancho, base, Paint().apply { color = SUAVE })
            y = base + 6f
            lienzo.drawText("Duración de cada ciclo, del más viejo al más reciente", MARGEN, y + 9f, chico)
            y += 20f
        }
    }
}
