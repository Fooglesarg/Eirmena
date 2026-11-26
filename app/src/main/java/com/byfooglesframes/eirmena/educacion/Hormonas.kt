package com.byfooglesframes.eirmena.educacion

import kotlin.math.exp

/** Niveles relativos (0 a 1) de cada hormona en un día. Es un esquema ilustrativo, no un análisis. */
data class Niveles(val estrogeno: Float, val progesterona: Float, val lh: Float, val fsh: Float)

enum class Etapa(val nombre: String) {
    MENSTRUAL("Menstruación"),
    FOLICULAR("Fase folicular"),
    OVULATORIA("Ovulación"),
    LUTEA("Fase lútea"),
    PREMENSTRUAL("Días premenstruales"),
}

/** Lo que pasa un día del ciclo, para mostrar debajo del gráfico. */
data class Explicacion(val etapa: Etapa, val hormonas: String, val sensaciones: String)

/**
 * Curvas típicas de un ciclo ovulatorio, armadas con campanas suaves. La fase lútea dura 14 días
 * y la folicular se estira o se acorta según la duración del ciclo, como pasa en la realidad.
 * Fuentes: descripción del ciclo de ACOG, NHS y textos de fisiología (Guyton & Hall).
 */
object Hormonas {
    /** Día del ciclo (1 = primer día del período) en que se estima la ovulación. */
    fun diaOvulacion(duracion: Int): Int = duracion - 13

    fun niveles(dia: Float, duracion: Int): Niveles {
        val ov = diaOvulacion(duracion).toFloat()
        val x = dia - ov
        val folicular = ov - 1
        val estrogeno = 0.12f +
            (if (x <= -1.5f) 0.78f * campana(x, -1.5f, maxOf(2.5f, folicular * 0.28f)) else 0.78f * campana(x, -1.5f, 1.3f)) +
            0.33f * campana(x, 7f, 3f)
        val progesterona = 0.05f + 0.85f * campana(x, 7f, 3.2f)
        val lh = 0.1f + 0.9f * campana(x, -1f, 0.8f)
        val fsh = 0.22f + 0.18f * campana(dia, 3f, 3f) + 0.35f * campana(x, -1f, 0.9f) + 0.15f * campana(dia, duracion + 1f, 2.5f)
        return Niveles(estrogeno.coerceIn(0f, 1f), progesterona.coerceIn(0f, 1f), lh.coerceIn(0f, 1f), fsh.coerceIn(0f, 1f))
    }

    fun etapa(dia: Int, duracion: Int, periodo: Int): Etapa {
        val ov = diaOvulacion(duracion)
        return when {
            dia <= periodo -> Etapa.MENSTRUAL
            dia in (ov - 2)..(ov + 1) -> Etapa.OVULATORIA
            dia < ov -> Etapa.FOLICULAR
            dia > duracion - 5 -> Etapa.PREMENSTRUAL
            else -> Etapa.LUTEA
        }
    }

    fun explicacion(dia: Int, duracion: Int, periodo: Int): Explicacion = when (etapa(dia, duracion, periodo)) {
        Etapa.MENSTRUAL -> Explicacion(
            Etapa.MENSTRUAL,
            "El estrógeno y la progesterona están bajos. Esa baja hace que se desprenda el endometrio, la capa que recubre " +
                "el útero por dentro, y eso es el sangrado. Al mismo tiempo, la FSH empieza a subir para preparar el próximo ciclo.",
            "Puede haber cólicos (los provocan unas sustancias llamadas prostaglandinas), cansancio, dolor de espalda o de cabeza. " +
                "El calor local y moverse suelen aliviar.",
        )
        Etapa.FOLICULAR -> Explicacion(
            Etapa.FOLICULAR,
            "La FSH hace crecer varios folículos en los ovarios, y uno se vuelve el dominante. Ese folículo fabrica cada vez " +
                "más estrógeno, que engrosa de nuevo el endometrio.",
            "Con el estrógeno en subida muchas personas sienten más energía, mejor ánimo y la piel más linda. Buen momento " +
                "para entrenar más fuerte.",
        )
        Etapa.OVULATORIA -> Explicacion(
            Etapa.OVULATORIA,
            "El estrógeno llega a su pico y eso dispara un pico de LH. Entre 24 y 36 horas después, el folículo se rompe " +
                "y libera el óvulo: es la ovulación. El óvulo vive unas 24 horas.",
            "El flujo se vuelve más transparente y elástico, como clara de huevo. Puede subir el deseo sexual y hay quien " +
                "siente un pinchazo de un lado del bajo vientre. Son los días con más probabilidad de embarazo.",
        )
        Etapa.LUTEA -> Explicacion(
            Etapa.LUTEA,
            "Lo que queda del folículo se transforma en el cuerpo lúteo, que fabrica progesterona. La progesterona prepara " +
                "el endometrio por si hay un embarazo y sube unas décimas la temperatura del cuerpo.",
            "Es común tener más hambre, sueño o sentirse más \"para adentro\". El flujo se vuelve más espeso.",
        )
        Etapa.PREMENSTRUAL -> Explicacion(
            Etapa.PREMENSTRUAL,
            "Si no hubo embarazo, el cuerpo lúteo se apaga y el estrógeno y la progesterona caen. Esa caída es la que " +
                "provoca el próximo período.",
            "La baja de hormonas puede traer síntomas premenstruales: hinchazón, senos sensibles, irritabilidad, tristeza " +
                "o antojos. Si te afectan mucho, se pueden tratar.",
        )
    }

    private fun campana(x: Float, centro: Float, ancho: Float): Float {
        val z = (x - centro) / ancho
        return exp(-0.5f * z * z)
    }
}
