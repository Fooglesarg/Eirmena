package com.byfooglesframes.eirmena.educacion

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Semana(val numero: Int, val tamano: String, val bebe: String, val vos: String)

data class Control(val desde: Int, val hasta: Int, val titulo: String, val texto: String)

/**
 * Embarazo semana a semana. Las semanas se cuentan desde el primer día de la última menstruación (FUM),
 * como en los controles: la fecha probable de parto es FUM + 280 días (40 semanas).
 * Tamaños aproximados y desarrollo según ACOG y NHS; cada embarazo es distinto.
 */
object Gestacion {
    fun dias(fum: LocalDate, hoy: LocalDate): Int = ChronoUnit.DAYS.between(fum, hoy).toInt()

    fun semanas(fum: LocalDate, hoy: LocalDate): Int = dias(fum, hoy) / 7
    fun diasExtra(fum: LocalDate, hoy: LocalDate): Int = dias(fum, hoy) % 7

    fun trimestre(semana: Int): Int = when {
        semana < 14 -> 1
        semana < 28 -> 2
        else -> 3
    }

    fun semana(n: Int): Semana = when {
        n < 4 -> Semana(n, "todavía microscópico",
            "Las primeras dos semanas se cuentan desde tu última menstruación, antes de la concepción. Alrededor de la semana 2 " +
                "es la ovulación y la fecundación, y en la 3 el embrión viaja hacia el útero para implantarse.",
            "Si buscás un embarazo, tomá ácido fólico todos los días.")
        n > 40 -> Semana(n, "listo para nacer",
            "Pasar la fecha probable es común: solo una minoría nace justo ese día.",
            "Pasada la semana 40 los controles se hacen más seguidos. Seguí las indicaciones de tu equipo de salud sobre cuándo internarte.")
        else -> semanas[n - 4]
    }

    private val semanas = listOf(
        Semana(4, "una semilla de amapola", "El embrión se implanta en el útero y empieza a formarse la placenta.", "Es la semana del primer día de atraso: un test de embarazo ya puede dar positivo."),
        Semana(5, "una semilla de sésamo", "Se forma el tubo neural, que será el cerebro y la médula espinal.", "Pueden aparecer cansancio, senos sensibles y ganas de orinar seguido."),
        Semana(6, "una lenteja", "Empieza a latir un corazón muy primitivo, que a veces se ve en una ecografía.", "Las náuseas suelen empezar estas semanas. Comer poco y seguido ayuda."),
        Semana(7, "un arándano", "Se forman los esbozos de brazos y piernas.", "Si todavía no lo hiciste, pedí tu primer control prenatal."),
        Semana(8, "una frambuesa", "Se forman los dedos y se mueve, aunque todavía no lo sientas.", "Las náuseas y el cansancio suelen estar en su punto más alto."),
        Semana(9, "una aceituna", "Ya tiene todos los órganos principales en formación. Deja de ser embrión y pasa a ser feto.", "En el primer control te piden análisis de sangre y orina."),
        Semana(10, "una frutilla", "Se forman las uñas y los órganos empiezan a funcionar.", "Podés notar la ropa más ajustada en la cintura."),
        Semana(11, "un higo", "Los huesos empiezan a endurecerse.", "Entre las semanas 11 y 14 se hace la ecografía del primer trimestre."),
        Semana(12, "una lima", "Tiene reflejos: abre y cierra las manos.", "Para muchas personas las náuseas empiezan a aflojar."),
        Semana(13, "un limón", "Las cuerdas vocales se están formando.", "Última semana del primer trimestre."),
        Semana(14, "un durazno", "Puede hacer gestos con la cara.", "Empieza el segundo trimestre: suele ser el más llevadero."),
        Semana(15, "una manzana", "Percibe la luz a través de los párpados cerrados.", "Puede aparecer congestión nasal o sangrado de encías: es por las hormonas."),
        Semana(16, "una palta", "Los músculos de la espalda se fortalecen y se estira.", "Algunas personas empiezan a sentir los primeros movimientos, como burbujitas."),
        Semana(17, "una granada", "Empieza a acumular grasa bajo la piel.", "Dormir de costado, sobre todo el izquierdo, se vuelve más cómodo."),
        Semana(18, "un morrón", "Puede oír sonidos de adentro de tu cuerpo, como tu corazón.", "Si es tu primer embarazo, los movimientos suelen notarse entre la semana 18 y la 22."),
        Semana(19, "un mango", "Una capa protectora (vérnix) le cubre la piel.", "Pueden aparecer dolores en los costados de la panza: son los ligamentos que se estiran."),
        Semana(20, "una banana", "Mitad del embarazo. Traga líquido amniótico y practica.", "Entre las semanas 20 y 24 se hace la ecografía morfológica. Desde la 20 te pueden dar la vacuna triple bacteriana acelular."),
        Semana(21, "una zanahoria", "Los movimientos son más fuertes y coordinados.", "Puede aparecer acidez: comer porciones chicas ayuda."),
        Semana(22, "un choclo", "Los sentidos se afinan: reconoce tu voz.", "Hablarle o ponerle música es una linda forma de conectar."),
        Semana(23, "un pomelo", "Los pulmones se preparan para respirar.", "Si se te hinchan los pies, levantalos un rato y tomá agua."),
        Semana(24, "un melón chico", "Tiene ciclos de sueño y vigilia.", "Entre las semanas 24 y 28 se hace la prueba de glucosa para detectar diabetes gestacional."),
        Semana(25, "una coliflor", "Responde a tu voz y al tacto sobre la panza.", "Pueden aparecer calambres en las piernas, sobre todo de noche."),
        Semana(26, "una lechuga", "Abre los ojos.", "Si tu grupo sanguíneo es Rh negativo, pueden indicarte una inyección de inmunoglobulina."),
        Semana(27, "un repollo", "El cerebro crece mucho.", "Última semana del segundo trimestre."),
        Semana(28, "una berenjena", "Puede parpadear y tiene pestañas.", "Empieza el tercer trimestre. Desde ahora prestá atención a sus movimientos: si se mueve mucho menos, consultá."),
        Semana(29, "un zapallito", "Los huesos ya están formados, aunque todavía blandos.", "Puede costar más dormir: una almohada entre las rodillas ayuda."),
        Semana(30, "un repollo grande", "Regula mejor su temperatura.", "Es normal sentir falta de aire: el útero empuja el diafragma."),
        Semana(31, "un coco", "Gira la cabeza de lado a lado.", "Pueden aparecer contracciones de práctica (Braxton Hicks): irregulares y sin dolor fuerte."),
        Semana(32, "un ananá chico", "Practica respirar y tiene uñas completas.", "Entre las semanas 32 y 36 se da la vacuna contra el virus sincicial respiratorio, que protege al bebé."),
        Semana(33, "un ananá", "El sistema inmune se fortalece con tus anticuerpos.", "Es buen momento para preparar el bolso para la internación."),
        Semana(34, "un melón", "Los pulmones maduran mucho estas semanas.", "Si tenés contracciones regulares antes de la semana 37, andá a la guardia."),
        Semana(35, "un melón grande", "Gana peso rápido.", "Entre las semanas 35 y 37 se hace el estudio del estreptococo."),
        Semana(36, "una papaya", "Suele acomodarse cabeza abajo.", "Los controles pasan a ser cada semana o cada dos."),
        Semana(37, "un atado de acelga", "Se considera a término desde esta semana.", "Aprendé a reconocer el trabajo de parto: contracciones regulares que se hacen más seguidas, o pérdida de líquido."),
        Semana(38, "un puerro", "Practica succionar y agarrar.", "Descansá todo lo que puedas."),
        Semana(39, "una sandía chica", "Está listo para nacer.", "Andá a la guardia si rompés bolsa, tenés sangrado o deja de moverse como siempre."),
        Semana(40, "un zapallo", "Llegó la fecha probable de parto.", "Solo una minoría nace justo hoy: es normal que se adelante o se atrase."),
    )

    /** Controles y vacunas recomendados por el Ministerio de Salud de la Nación y la OMS. */
    val controles = listOf(
        Control(4, 13, "Primer control", "Lo antes posible: historia clínica, presión, análisis de sangre (grupo y factor, hemograma, glucemia, VIH, sífilis, hepatitis B, toxoplasmosis, Chagas) y orina."),
        Control(11, 14, "Ecografía del primer trimestre", "Confirma la edad gestacional y puede medir la translucencia nucal."),
        Control(4, 40, "Vacuna antigripal", "En cualquier trimestre, durante la temporada de gripe."),
        Control(20, 40, "Vacuna triple bacteriana acelular", "Desde la semana 20, en cada embarazo: protege al bebé de la tos convulsa."),
        Control(20, 24, "Ecografía morfológica", "Revisa en detalle cómo se está formando el bebé."),
        Control(24, 28, "Prueba de glucosa", "Detecta la diabetes gestacional."),
        Control(32, 36, "Vacuna contra el virus sincicial respiratorio", "En la temporada indicada por el calendario (otoño e invierno). Protege al bebé de la bronquiolitis en sus primeros meses."),
        Control(35, 37, "Estudio del estreptococo", "Un hisopado para saber si hace falta antibiótico en el parto."),
    )

    fun controlesDe(semana: Int): List<Control> = controles.filter { semana in it.desde..it.hasta }
}
