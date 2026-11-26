package com.byfooglesframes.eirmena.educacion

data class Consejo(val tema: String, val texto: String)

/**
 * Sugerencias de bienestar por fase. Son hábitos generales de salud acomodados a lo que muchas personas
 * sienten en cada momento del ciclo; la evidencia de "sincronizar" la vida con el ciclo es limitada,
 * así que se presentan como ideas para probar, nunca como reglas. Fuentes: ACOG, NHS, OMS.
 */
object Bienestar {
    fun de(etapa: Etapa): List<Consejo> = when (etapa) {
        Etapa.MENSTRUAL -> listOf(
            Consejo("Comida", "Sumá alimentos con hierro (legumbres, carnes, hojas verdes) junto con vitamina C (cítricos, morrón) para absorberlo mejor: con el sangrado se pierde hierro."),
            Consejo("Movimiento", "Caminar, estirar o hacer yoga suave suele aliviar los cólicos. Si tenés energía, entrenar también está bien."),
            Consejo("Descanso", "Si sentís más cansancio, date permiso para bajar el ritmo y dormir un poco más."),
            Consejo("Alivio", "Calor en la panza o la espalda y un analgésico antiinflamatorio, según el prospecto, apenas empieza el dolor."),
        )
        Etapa.FOLICULAR -> listOf(
            Consejo("Comida", "Buen momento para sumar variedad: proteínas, frutas, verduras y granos integrales para acompañar la energía en subida."),
            Consejo("Movimiento", "Muchas personas rinden mejor en entrenamientos intensos o de fuerza estos días. Aprovechá si te pasa."),
            Consejo("Ánimo", "Con el estrógeno en subida suele haber más ganas de proyectos y de ver gente."),
            Consejo("Descanso", "Mantené horarios de sueño parejos: ayudan a que el ciclo sea más regular."),
        )
        Etapa.OVULATORIA -> listOf(
            Consejo("Cuerpo", "Un pinchazo de un lado del bajo vientre y un flujo elástico son señales comunes de ovulación."),
            Consejo("Movimiento", "La energía suele estar alta. Calentá bien antes de entrenar fuerte."),
            Consejo("Sexualidad", "El deseo puede subir. Si no buscás un embarazo, estos son los días de más riesgo: usá un método."),
            Consejo("Hidratación", "Tomá agua durante el día, sobre todo si entrenás."),
        )
        Etapa.LUTEA -> listOf(
            Consejo("Comida", "Es normal tener más hambre: el cuerpo gasta un poco más. Elegí comidas que sacien, con fibra y proteína, para evitar los picos de antojos."),
            Consejo("Movimiento", "Si sentís menos energía, probá con actividades de intensidad media: caminar, nadar, bici."),
            Consejo("Descanso", "La progesterona puede dar más sueño. Acostarte un poco antes ayuda."),
            Consejo("Ánimo", "Anotar cómo te sentís te ayuda a ver si los cambios se repiten cada ciclo."),
        )
        Etapa.PREMENSTRUAL -> listOf(
            Consejo("Comida", "Reducir sal, alcohol y cafeína puede aliviar la hinchazón y la irritabilidad. Comidas chicas y seguidas ayudan con los antojos."),
            Consejo("Movimiento", "El ejercicio aeróbico regular es una de las medidas con mejor evidencia para aliviar el síndrome premenstrual."),
            Consejo("Ánimo", "Si estos días te sentís más sensible, planificá menos y avisale a la gente cercana. Si te afecta mucho, consultá: tiene tratamiento."),
            Consejo("Preparate", "Dejá a mano lo que vas a necesitar para tu período."),
        )
    }

    /** Para la perimenopausia, en lugar de consejos por fase. */
    val perimenopausia = listOf(
        Consejo("Sofocos", "Ropa en capas, ambientes frescos y menos alcohol, cafeína y comidas muy picantes. Anotá qué los dispara."),
        Consejo("Huesos", "Calcio y vitamina D en la comida, y ejercicios con peso o de fuerza al menos dos veces por semana."),
        Consejo("Sueño", "Habitación fresca, horarios fijos y pocas pantallas antes de dormir."),
        Consejo("Consulta", "Si los síntomas te cambian la vida, existen tratamientos con y sin hormonas: se deciden según tu historia."),
    )

    /** Con métodos que frenan la ovulación no hay fases naturales: consejos generales. */
    val conMetodoHormonal = listOf(
        Consejo("Constancia", "Lo más importante es usar el método siempre igual: misma hora para la pastilla, fechas de cambio para parche y anillo."),
        Consejo("Movimiento", "Moverse con regularidad mejora el ánimo, el sueño y el dolor, en cualquier momento del mes."),
        Consejo("Comida", "Una alimentación variada con hierro, calcio y fibra te cuida en todas las etapas."),
        Consejo("Atención", "Si notás cambios de ánimo, de deseo o sangrados que te molestan desde que empezaste el método, consultalo: hay otras opciones."),
    )
}
