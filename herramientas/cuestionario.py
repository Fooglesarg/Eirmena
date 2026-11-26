"""Genera el cuestionario para testers de Eirmena como PDF rellenable.

Uso:    python cuestionario.py [salida.pdf]
Necesita: pip install reportlab pypdf fonttools
Tipografías del sistema: Comfortaa (OTF) y Open Sans (TTF). Comfortaa se convierte a TTF en un archivo
temporal en cada ejecución, porque reportlab no lee OTF. No se guarda la versión convertida en el repo:
la licencia OFL de Comfortaa reserva el nombre y no permite redistribuir versiones modificadas con él.
"""
import sys
from pathlib import Path

from reportlab.lib.colors import Color, HexColor, white
from reportlab.lib.pagesizes import A4
from reportlab.lib.utils import simpleSplit
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.pdfgen import canvas

AQUI = Path(__file__).parent
SALIDA = sys.argv[1] if len(sys.argv) > 1 else str(AQUI.parent / "docs" / "Eirmena-cuestionario-tester.pdf")
VERSION = "1.0.0-beta.1"

COMFORTAA_OTF = "/usr/share/fonts/aajohan-comfortaa-fonts/Comfortaa-Bold.otf"


def otf_a_ttf(origen: str) -> str:
    """Convierte una tipografía OTF (contornos CFF) a TTF temporal, solo para usarla en este script."""
    import tempfile
    from fontTools.pens.cu2quPen import Cu2QuPen
    from fontTools.pens.ttGlyphPen import TTGlyphPen
    from fontTools.ttLib import TTFont as Fuente, newTable

    f = Fuente(origen)
    orden = f.getGlyphOrder()
    glifos = f.getGlyphSet()
    nuevos = {}
    for n in orden:
        lapiz = TTGlyphPen(glifos)
        glifos[n].draw(Cu2QuPen(lapiz, 1.0, reverse_direction=True))
        nuevos[n] = lapiz.glyph()
    f["loca"] = newTable("loca")
    f["glyf"] = glyf = newTable("glyf")
    glyf.glyphOrder, glyf.glyphs = orden, nuevos
    del f["CFF "]
    f["head"].glyphDataFormat = 0
    maxp = f["maxp"]
    maxp.tableVersion = 0x00010000
    for k in ("maxPoints", "maxContours", "maxCompositePoints", "maxCompositeContours", "maxTwilightPoints",
              "maxStorage", "maxFunctionDefs", "maxInstructionDefs", "maxStackElements", "maxSizeOfInstructions",
              "maxComponentElements", "maxComponentDepth"):
        setattr(maxp, k, 0)
    maxp.maxZones = 1
    f.sfntVersion = "\x00\x01\x00\x00"
    salida = tempfile.NamedTemporaryFile(suffix=".ttf", delete=False).name
    f.save(salida)
    return salida


pdfmetrics.registerFont(TTFont("Titulo", otf_a_ttf(COMFORTAA_OTF)))
pdfmetrics.registerFont(TTFont("Texto", "/usr/share/fonts/open-sans/OpenSans-Regular.ttf"))
pdfmetrics.registerFont(TTFont("TextoSemi", "/usr/share/fonts/open-sans/OpenSans-Semibold.ttf"))

CREMA = HexColor("#FBF4EC")
CREMA_OSCURA = HexColor("#F3E6D8")
TARJETA = HexColor("#FFFBF7")
TERRACOTA = HexColor("#C8694F")
TERRACOTA_OSCURA = HexColor("#9E4A35")
ROSA_CLARO = HexColor("#F6DED7")
VINO = HexColor("#6E3036")
TEXTO = HexColor("#3A2925")
SUAVE = HexColor("#7D6760")
LINEA = HexColor("#E6D5C7")

ANCHO, ALTO = A4
M = 50  # margen
UTIL = ANCHO - 2 * M


TRANSPARENTE = Color(1, 1, 1, alpha=0)


class Cuestionario:
    def __init__(self, ruta):
        self.c = canvas.Canvas(ruta, pagesize=A4)
        self.c.setTitle("Eirmena · Cuestionario para testers")
        self.c.setAuthor("ByFooglesFrames")
        self.c.setSubject(f"Cuestionario de la versión {VERSION}")
        self.pagina = 0
        self.n = 0
        self.nueva_pagina()

    # ------------------------------------------------------------------ páginas
    def nueva_pagina(self):
        if self.pagina:
            self.pie()
            self.c.showPage()
        self.pagina += 1
        self.c.setFillColor(CREMA)
        self.c.rect(0, 0, ANCHO, ALTO, stroke=0, fill=1)
        self.c.setFillColor(TERRACOTA)
        self.c.rect(0, ALTO - 6, ANCHO, 6, stroke=0, fill=1)
        self.y = ALTO - M

    def pie(self):
        c = self.c
        c.setFont("Texto", 8)
        c.setFillColor(SUAVE)
        c.drawString(M, 28, f"Eirmena · Cuestionario para testers · versión {VERSION}")
        c.drawRightString(ANCHO - M, 28, f"Desarrollado por ByFooglesFrames · página {self.pagina}")

    def lugar(self, alto):
        """Pasa de página si lo que sigue no entra entero."""
        if self.y - alto < 50:
            self.nueva_pagina()

    def parrafo(self, texto, x=M, ancho=UTIL, fuente="Texto", tam=10.5, color=TEXTO, interlineado=1.45):
        lineas = simpleSplit(texto, fuente, tam, ancho)
        self.c.setFont(fuente, tam)
        self.c.setFillColor(color)
        for l in lineas:
            self.y -= tam * interlineado
            self.c.drawString(x, self.y, l)
        return len(lineas)

    def alto_parrafo(self, texto, ancho, fuente="Texto", tam=10.5, interlineado=1.45):
        return len(simpleSplit(texto, fuente, tam, ancho)) * tam * interlineado

    # ------------------------------------------------------------------ portada
    def portada(self):
        c = self.c
        # Ícono: luna creciente que abraza una gota, sobre fondo terracota.
        x0, y0, lado = M, self.y - 70, 70
        c.setFillColor(TERRACOTA)
        c.roundRect(x0, y0, lado, lado, 16, stroke=0, fill=1)
        s = lado / 108
        def P(x, y):
            return x0 + x * s, y0 + (108 - y) * s
        c.setFillColor(CREMA)
        cx, cy = P(58, 54)
        c.circle(cx, cy, 28 * s, stroke=0, fill=1)
        c.setFillColor(TERRACOTA)
        c.circle(*P(77.3, 54), 34 * s, stroke=0, fill=1)
        c.setFillColor(ROSA_CLARO)
        p = c.beginPath()
        p.moveTo(*P(62, 38))
        p.curveTo(*P(62, 38), *P(74, 52), *P(74, 60))
        p.curveTo(*P(74, 67), *P(68.6, 72), *P(62, 72))
        p.curveTo(*P(55.4, 72), *P(50, 67), *P(50, 60))
        p.curveTo(*P(50, 52), *P(62, 38), *P(62, 38))
        c.drawPath(p, stroke=0, fill=1)

        c.setFillColor(VINO)
        c.setFont("Titulo", 28)
        c.drawString(M + 88, self.y - 30, "Cuestionario para testers")
        c.setFont("TextoSemi", 11)
        c.setFillColor(TERRACOTA_OSCURA)
        c.drawString(M + 88, self.y - 52, f"Eirmena · versión {VERSION} · beta privada")
        self.y -= 96

        self.parrafo("¡Gracias por probar Eirmena! Tus respuestas van a definir qué cambia antes de la versión 1.0. "
                     "No hay respuestas correctas ni incorrectas: lo que más sirve es tu opinión sincera, "
                     "incluso (sobre todo) lo que no te gustó.", tam=11.5)
        self.y -= 14

        # Caja "cómo responder"
        items = [
            "Completalo acá mismo: tocá las casillas y escribí en los recuadros. Funciona en la mayoría de los lectores de "
            "PDF (por ejemplo, el de Google Drive o Adobe Acrobat). Después guardalo.",
            "Si tu lector no te deja escribir, imprimilo o respondé por mensaje usando los números de las preguntas.",
            "Respondé solo lo que probaste. Si algo no lo usaste, dejalo en blanco o marcá \"No lo probé\".",
            "En las escalas, 1 es lo peor y 5 lo mejor.",
            "Tiempo estimado: 15 a 20 minutos.",
        ]
        alto = 34 + sum(self.alto_parrafo(t, UTIL - 50, tam=10) + 4 for t in items)
        c.setFillColor(TARJETA)
        c.roundRect(M, self.y - alto, UTIL, alto, 14, stroke=0, fill=1)
        c.setFont("TextoSemi", 11)
        c.setFillColor(VINO)
        c.drawString(M + 18, self.y - 22, "Cómo responder")
        self.y -= 26
        for t in items:
            c.setFillColor(TERRACOTA)
            c.circle(M + 24, self.y - 10, 2.2, stroke=0, fill=1)
            self.parrafo(t, x=M + 34, ancho=UTIL - 50, tam=10)
            self.y -= 4
        self.y -= 18

    # ------------------------------------------------------------------ bloques
    def seccion(self, letra, titulo, nota=None):
        self.lugar(90)
        self.y -= 14
        c = self.c
        c.setFillColor(TERRACOTA)
        c.roundRect(M, self.y - 24, 28, 24, 7, stroke=0, fill=1)
        c.setFillColor(white)
        c.setFont("Titulo", 13)
        c.drawCentredString(M + 14, self.y - 17, letra)
        c.setFillColor(VINO)
        c.setFont("Titulo", 16)
        c.drawString(M + 38, self.y - 18, titulo)
        self.y -= 30
        if nota:
            self.parrafo(nota, tam=9.5, color=SUAVE)
        self.y -= 4

    def enunciado(self, texto, alto_respuesta):
        self.n += 1
        alto = self.alto_parrafo(f"{self.n}. {texto}", UTIL - 18, "TextoSemi", 10.5)
        self.lugar(alto + alto_respuesta + 14)
        self.y -= 8
        c = self.c
        c.setFont("TextoSemi", 10.5)
        c.setFillColor(TERRACOTA_OSCURA)
        c.drawString(M, self.y - 10.5 * 1.45 + 0.0, f"{self.n}.")
        self.parrafo(texto, x=M + 18, ancho=UTIL - 18, fuente="TextoSemi")
        self.y -= 6
        return f"p{self.n:02d}"

    def circulo(self, nombre, valor, x, y, tam, tooltip, borde=TERRACOTA):
        """Círculo dibujado a mano + campo de opción invisible encima (los visores lo dibujaban deformado)."""
        c = self.c
        c.setStrokeColor(borde)
        c.setFillColor(TARJETA)
        c.setLineWidth(1.2)
        c.circle(x + tam / 2, y + tam / 2, tam / 2 - 0.6, stroke=1, fill=1)
        c.acroForm.radio(name=nombre, value=valor, x=x, y=y, size=tam, buttonStyle="circle", shape="circle",
                         borderColor=TRANSPARENTE, fillColor=TRANSPARENTE, textColor=VINO, borderWidth=0,
                         forceBorder=False, tooltip=tooltip)

    def campo(self, nombre, alto, x=M + 18, ancho=UTIL - 18, multilinea=True):
        self.c.acroForm.textfield(
            name=nombre, x=x, y=self.y - alto, width=ancho, height=alto,
            fontName="Helvetica", fontSize=10, textColor=TEXTO, fillColor=TARJETA,
            borderColor=LINEA, borderWidth=1, forceBorder=True,
            fieldFlags="multiline" if multilinea else "", tooltip="Escribí tu respuesta",
        )
        self.y -= alto + 4

    def abierta(self, texto, lineas=3):
        alto = 16 + lineas * 14
        nombre = self.enunciado(texto, alto)
        self.campo(nombre, alto, multilinea=lineas > 1)

    def opciones(self, texto, ops, multiple=False, otra=None):
        """Opciones en filas que se acomodan solas. [otra]: texto para un campo libre al final."""
        c = self.c
        anchos = [16 + pdfmetrics.stringWidth(o, "Texto", 10) + 18 for o in ops]
        filas, actual, usado = [], [], 0
        for o, a in zip(ops, anchos):
            if actual and usado + a > UTIL - 18:
                filas.append(actual)
                actual, usado = [], 0
            actual.append((o, a))
            usado += a
        filas.append(actual)
        alto = len(filas) * 22 + (26 if otra else 0)
        nombre = self.enunciado(texto + (" (podés marcar varias)" if multiple else ""), alto)
        for fila in filas:
            x = M + 18
            for i, (o, a) in enumerate(fila):
                yy = self.y - 15
                if multiple:
                    c.acroForm.checkbox(name=f"{nombre}_op{ops.index(o)}", x=x, y=yy, size=12, buttonStyle="check",
                                        borderColor=TERRACOTA, fillColor=TARJETA, textColor=VINO, forceBorder=True,
                                        tooltip=o)
                else:
                    self.circulo(nombre, f"op{ops.index(o)}", x, yy, 12, o)
                c.setFont("Texto", 10)
                c.setFillColor(TEXTO)
                c.drawString(x + 16, yy + 2.5, o)
                x += a
            self.y -= 22
        if otra:
            c.setFont("Texto", 9.5)
            c.setFillColor(SUAVE)
            c.drawString(M + 18, self.y - 14, otra)
            w = pdfmetrics.stringWidth(otra, "Texto", 9.5) + 8
            self.y -= 2
            self.campo(nombre + "_otra", 20, x=M + 18 + w, ancho=UTIL - 18 - w, multilinea=False)

    def escala(self, texto, izq="Nada", der="Mucho", probe=True):
        """Escala del 1 al 5, con los extremos explicados y "No lo probé"."""
        nombre = self.enunciado(texto, 40)
        c = self.c
        x = M + 18
        c.setFont("Texto", 9)
        c.setFillColor(SUAVE)
        c.drawString(x, self.y - 16, izq)
        x += pdfmetrics.stringWidth(izq, "Texto", 9) + 10
        for v in range(1, 6):
            self.circulo(nombre, str(v), x, self.y - 20, 14, str(v))
            c.setFont("TextoSemi", 8.5)
            c.setFillColor(TERRACOTA_OSCURA)
            c.drawCentredString(x + 7, self.y - 31, str(v))
            x += 30
        c.setFont("Texto", 9)
        c.setFillColor(SUAVE)
        c.drawString(x + 2, self.y - 16, der)
        if probe:
            x = M + 18 + UTIL - 18 - 92
            self.circulo(nombre, "no", x, self.y - 19, 12, "No lo probé", borde=SUAVE)
            c.setFont("Texto", 9)
            c.setFillColor(SUAVE)
            c.drawString(x + 16, self.y - 16, "No lo probé")
        self.y -= 38

    def nps(self, texto):
        nombre = self.enunciado(texto, 44)
        c = self.c
        paso = (UTIL - 18) / 11
        for v in range(11):
            x = M + 18 + v * paso
            self.circulo(nombre, str(v), x, self.y - 20, 14, str(v))
            c.setFont("TextoSemi", 8.5)
            c.setFillColor(TERRACOTA_OSCURA)
            c.drawCentredString(x + 7, self.y - 31, str(v))
        c.setFont("Texto", 8.5)
        c.setFillColor(SUAVE)
        c.drawString(M + 18, self.y - 43, "Para nada")
        c.drawRightString(M + 18 + 10 * paso + 14, self.y - 43, "Seguro que sí")
        self.y -= 50

    def cierre(self):
        self.lugar(120)
        self.y -= 20
        c = self.c
        c.setFillColor(VINO)
        c.roundRect(M, self.y - 96, UTIL, 96, 18, stroke=0, fill=1)
        c.setFillColor(CREMA)
        c.setFont("Titulo", 18)
        c.drawString(M + 22, self.y - 34, "¡Gracias por tu tiempo!")
        self.y -= 44
        self.parrafo("Guardá este PDF con tus respuestas y mandáselo a quien te invitó. Cada comentario se lee "
                     "y ayuda a que Eirmena sea mejor para todas las personas que la usen.",
                     x=M + 22, ancho=UTIL - 44, tam=10.5, color=CREMA)

    def guardar(self):
        self.pie()
        self.c.save()


def armar(q: Cuestionario):
    q.portada()

    q.seccion("A", "Sobre vos")
    q.abierta("Tu nombre o apodo (opcional).", 1)
    q.abierta("¿Qué celular tenés? (marca y modelo)", 1)
    q.opciones("¿Cuánto tiempo usaste Eirmena?", ["1 o 2 días", "3 a 6 días", "1 a 2 semanas", "Más de 2 semanas"])
    q.opciones("¿Usás o usaste otra app para el ciclo?", ["Flo", "Clue", "Period Tracker", "Otra", "Ninguna"],
               multiple=True, otra="¿Cuál?")
    q.opciones("¿Qué etapa elegiste en Eirmena?", ["Registrar mi ciclo", "Busco un embarazo", "Embarazo",
                                                  "Perimenopausia", "Mis primeros ciclos"])

    q.seccion("B", "Instalación y primera vez")
    q.escala("¿Qué tan fácil fue instalarla?", "Muy difícil", "Muy fácil", probe=False)
    q.abierta("¿Tuviste algún problema para instalarla? ¿Cuál?", 2)
    q.escala("La bienvenida (los primeros pasos al abrirla), ¿fue clara?", "Nada clara", "Muy clara", probe=False)
    q.opciones("¿Te quedó claro que Eirmena no usa internet y que tus datos quedan solo en tu celular?",
               ["Sí", "Más o menos", "No"])
    q.opciones("¿Pusiste PIN o huella?", ["Sí", "No"], otra="Si no, ¿por qué?")

    q.seccion("C", "Anotar tu día")
    q.escala("¿Qué tan fácil es anotar el período (\"Me vino hoy\" o desde el calendario)?", "Muy difícil", "Muy fácil")
    q.escala("¿Qué tan fácil es anotar tu día (síntomas, ánimo, energía, etc.)?", "Muy difícil", "Muy fácil")
    q.opciones("Las opciones de síntomas y ánimo…", ["Alcanzan", "Faltan algunas", "Sobran algunas"],
               otra="¿Cuáles agregarías o sacarías?")
    q.opciones("¿Cuánto tardás en anotar un día?", ["Menos de 1 minuto", "1 a 3 minutos", "Más de 3 minutos"])
    q.abierta("¿Hubo algo que quisiste anotar y no pudiste?", 2)

    q.seccion("D", "Predicción, calendario y pantalla Hoy")
    q.opciones("¿La predicción de tu próximo período se parece a lo que esperabas?",
               ["Sí", "Más o menos", "No", "Todavía no sé"])
    q.escala("¿Se entienden los colores y las marcas del calendario?", "Nada", "Totalmente")
    q.escala("La pantalla Hoy (el anillo, la fase y las tarjetas), ¿qué tan útil te resultó?", "Nada útil", "Muy útil")
    q.abierta("¿Algo de la pantalla Hoy te sobró, te confundió o te faltó?", 3)

    q.seccion("E", "Anticonceptivo y avisos", "Si no usás ningún método ni probaste los avisos, podés saltear esta parte.")
    q.opciones("¿Configuraste un método anticonceptivo?",
               ["Pastilla", "Parche o anillo", "Inyección", "Implante o DIU", "Preservativo u otro", "No"])
    q.opciones("¿Los recordatorios llegaron a horario?", ["Sí", "A veces tarde", "No llegaron", "No los usé"])
    q.opciones("Los avisos vienen \"discretos\" (solo dicen \"Eirmena: tenés un recordatorio\"). ¿Qué preferís?",
               ["Así está bien", "Que digan de qué se trata", "No los vi"])
    q.abierta("Comentarios sobre el anticonceptivo o los avisos.", 2)

    q.seccion("F", "Aprender")
    q.escala("El gráfico de hormonas (Aprender › Tus hormonas), ¿se entiende?", "Nada", "Totalmente")
    q.escala("El asistente de retraso, ¿te pareció útil?", "Nada útil", "Muy útil")
    q.opciones("¿Leíste artículos de la biblioteca?", ["Ninguno", "1 a 3", "Más de 3"])
    q.escala("Los textos de Eirmena, ¿se entienden?", "Nada", "Totalmente")
    q.abierta("¿Encontraste algún dato que te pareció incorrecto, desactualizado o que te incomodó? ¿Dónde?", 3)
    q.opciones("¿Cómo sentiste el tono de la app (la forma en que te habla)?",
               ["Cálido y cercano", "Neutro", "Frío o muy técnico", "Demasiado informal"])
    q.opciones("Eirmena usa palabras neutras (por ejemplo, \"Ánimo: tristeza\" en lugar de \"triste\"). ¿Qué te parece?",
               ["Me gusta así", "Me da igual", "Prefiero que me hable en femenino"], otra="Otro comentario:")

    q.seccion("G", "Otras etapas", "Solo si cambiaste de etapa en Ajustes › Etapa de vida.")
    q.abierta("¿Probaste Busco un embarazo, Embarazo, Perimenopausia o Mis primeros ciclos? ¿Cuál y qué te pareció?", 3)

    q.seccion("H", "Estadísticas e informe")
    q.escala("Las estadísticas (Ciclos › Estadísticas), ¿te sirvieron?", "Nada", "Mucho")
    q.opciones("¿Generaste el informe en PDF?", ["Sí", "No"])
    q.opciones("¿Lo llevarías a una consulta médica?", ["Sí", "Tal vez", "No"], otra="¿Por qué?")

    q.seccion("I", "¿Sabías que…?")
    q.escala("¿Te gustó el minijuego de cartas?", "Nada", "Mucho")
    q.opciones("Que las cartas se repitan…", ["Me motiva a volver", "Me da igual", "Me frustra"])
    q.abierta("¿Te gustaría más cartas o algún tema en especial?", 2)

    q.seccion("J", "Diseño")
    q.escala("¿Te gustan los colores y la estética?", "Nada", "Mucho", probe=False)
    q.opciones("¿Se lee bien todo (tamaño de letra, contraste)?", ["Sí", "Algunas partes no", "No"], otra="¿Dónde?")
    q.abierta("¿Algo se veía mal, cortado o fuera de lugar en tu pantalla?", 2)

    q.seccion("K", "Privacidad y confianza")
    q.escala("¿Cuánto confiás en Eirmena para guardar tus datos?", "Nada", "Totalmente", probe=False)
    q.opciones("¿Exportaste un respaldo?", ["Sí, fue fácil", "Sí, pero me costó", "No"])

    q.seccion("L", "Errores")
    q.opciones("¿La app se cerró sola o se trabó?", ["Nunca", "Una vez", "Varias veces"],
               otra="¿Qué estabas haciendo?")
    q.abierta("¿Anduvo lenta en algún momento? ¿Dónde?", 2)

    q.seccion("M", "En general")
    q.nps("Del 0 al 10, ¿qué tan probable es que le recomiendes Eirmena a una amiga?")
    q.abierta("¿Qué fue lo que más te gustó?", 3)
    q.abierta("¿Qué fue lo que menos te gustó?", 3)
    q.abierta("Si pudieras cambiar una sola cosa, ¿cuál sería?", 2)
    q.abierta("Comparada con otras apps que usaste, ¿qué le falta?", 3)
    q.opciones("¿La seguirías usando?", ["Sí", "Tal vez", "No"])
    q.abierta("Comentarios libres: lo que quieras contar.", 6)
    q.cierre()


def puntos_centrados(ruta):
    """Reemplaza el dibujo de "opción marcada" de reportlab (un símbolo chico y corrido)
    por un círculo vino relleno y centrado, igual en todos los visores."""
    from pypdf import PdfWriter
    from pypdf.generic import DecodedStreamObject, DictionaryObject, NameObject, ArrayObject, FloatObject

    w = PdfWriter(clone_from=ruta)
    k = 0.5523
    for pag in w.pages:
        for ref in pag.get("/Annots", []):
            a = ref.get_object()
            padre = a.get("/Parent")
            ff = padre.get_object().get("/Ff", 0) if padre else a.get("/Ff", 0)
            if not (int(ff) & (1 << 15)):  # solo botones de opción (radio)
                continue
            x1, y1, x2, y2 = [float(v) for v in a["/Rect"]]
            ancho, alto = x2 - x1, y2 - y1
            cx, cy, r = ancho / 2, alto / 2, min(ancho, alto) * 0.27
            c = r * k
            trazo = (f"q 0.431 0.188 0.212 rg {cx + r:.2f} {cy:.2f} m "
                     f"{cx + r:.2f} {cy + c:.2f} {cx + c:.2f} {cy + r:.2f} {cx:.2f} {cy + r:.2f} c "
                     f"{cx - c:.2f} {cy + r:.2f} {cx - r:.2f} {cy + c:.2f} {cx - r:.2f} {cy:.2f} c "
                     f"{cx - r:.2f} {cy - c:.2f} {cx - c:.2f} {cy - r:.2f} {cx:.2f} {cy - r:.2f} c "
                     f"{cx + c:.2f} {cy - r:.2f} {cx + r:.2f} {cy - c:.2f} {cx + r:.2f} {cy:.2f} c f Q")

            def flujo(contenido):
                st = DecodedStreamObject()
                st.set_data(contenido.encode())
                st.update({NameObject("/Type"): NameObject("/XObject"), NameObject("/Subtype"): NameObject("/Form"),
                           NameObject("/BBox"): ArrayObject([FloatObject(0), FloatObject(0), FloatObject(ancho), FloatObject(alto)])})
                return w._add_object(st)

            ap = a["/AP"]
            for modo in ("/N", "/D"):
                if modo not in ap:
                    continue
                estados = ap[modo].get_object()
                nuevos = DictionaryObject()
                for estado in estados.keys():
                    nuevos[NameObject(estado)] = flujo("" if estado == "/Off" else trazo)
                ap[NameObject(modo)] = nuevos
    w.write(ruta)


if __name__ == "__main__":
    q = Cuestionario(SALIDA)
    armar(q)
    q.guardar()
    puntos_centrados(SALIDA)
    print(SALIDA, q.n, "preguntas,", q.pagina, "páginas")
