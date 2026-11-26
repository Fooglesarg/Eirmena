package com.byfooglesframes.eirmena.educacion

data class Seccion(val titulo: String, val texto: String)

data class Articulo(
    val id: String,
    val categoria: String,
    val titulo: String,
    val resumen: String,
    val secciones: List<Seccion>,
    val fuentes: List<String>,
)

/**
 * Artículos offline. Información general basada en las fuentes citadas en cada uno; no reemplaza la consulta.
 * Revisados: noviembre de 2026.
 */
object Biblioteca {
    const val REVISION = "noviembre de 2026"

    private const val OMS = "Organización Mundial de la Salud (OMS)"
    private const val ACOG = "Colegio Americano de Obstetras y Ginecólogos (ACOG)"
    private const val NHS = "Servicio Nacional de Salud del Reino Unido (NHS)"
    private const val MSAL = "Ministerio de Salud de la Nación Argentina"
    private const val FIGO = "Federación Internacional de Ginecología y Obstetricia (FIGO)"
    private const val CDC = "Centros para el Control y la Prevención de Enfermedades de EE. UU. (CDC)"

    val articulos = listOf(
        // ---------- Ciclo y hormonas ----------
        Articulo(
            "ciclo", "Ciclo y hormonas", "Cómo funciona el ciclo menstrual",
            "Qué es un ciclo, cuánto dura y qué se considera normal.",
            listOf(
                Seccion("Qué es", "El ciclo menstrual es la forma en que el cuerpo se prepara cada mes para un posible embarazo. " +
                    "Se cuenta desde el primer día del período hasta el día anterior al siguiente."),
                Seccion("Cuánto dura", "Un ciclo normal dura entre 24 y 38 días, y el período, hasta 8 días. No hace falta que dure " +
                    "28: ese número es solo un promedio. También es normal que varíe un poco de un mes a otro, hasta 7 a 9 días " +
                    "entre el más corto y el más largo."),
                Seccion("Sus fases", "Menstruación: se desprende el endometrio. Fase folicular: crece un folículo en el ovario y sube " +
                    "el estrógeno. Ovulación: se libera un óvulo. Fase lútea: sube la progesterona y, si no hay embarazo, las " +
                    "hormonas caen y empieza un nuevo período."),
                Seccion("Cambia con la edad", "En los primeros años después de la primera menstruación y en los años previos a la " +
                    "menopausia los ciclos suelen ser más irregulares. Es esperable."),
            ),
            listOf(FIGO, ACOG, NHS),
        ),
        Articulo(
            "hormonas", "Ciclo y hormonas", "Las hormonas del ciclo",
            "Quién es quién: estrógeno, progesterona, LH, FSH y testosterona.",
            listOf(
                Seccion("Un trabajo en equipo", "El ciclo lo coordinan el cerebro y los ovarios. El hipotálamo le avisa a la " +
                    "hipófisis, que libera FSH y LH; esas hormonas le dicen a los ovarios qué hacer, y los ovarios responden " +
                    "fabricando estrógeno y progesterona."),
                Seccion("FSH", "Hormona folículo estimulante. Al principio del ciclo hace crecer los folículos del ovario, que son " +
                    "las bolsitas donde madura cada óvulo."),
                Seccion("Estrógeno", "Lo fabrica el folículo que va creciendo. Engrosa el endometrio, vuelve el flujo más elástico " +
                    "y, cuando llega a su pico, dispara la LH. También cuida los huesos, la piel y el corazón."),
                Seccion("LH", "Hormona luteinizante. Su pico provoca la ovulación entre 24 y 36 horas después. Los tests de " +
                    "ovulación de farmacia detectan justamente este pico en la orina."),
                Seccion("Progesterona", "Después de ovular, el cuerpo lúteo fabrica progesterona, que prepara el útero para un " +
                    "posible embarazo y sube unas décimas la temperatura del cuerpo. Si no hay embarazo, baja y llega el período."),
                Seccion("Testosterona", "Los ovarios también fabrican un poco de testosterona. Influye en el deseo sexual y la energía. " +
                    "Cuando hay de más, como en el SOP, puede aparecer acné o más vello."),
            ),
            listOf(ACOG, NHS),
        ),
        Articulo(
            "desbalances", "Ciclo y hormonas", "Desbalances hormonales",
            "Las causas más comunes de ciclos alterados y cómo se estudian.",
            listOf(
                Seccion("Qué puede pasar", "Cuando alguna hormona está de más o de menos, el ciclo puede volverse irregular, " +
                    "desaparecer o traer síntomas nuevos. Casi siempre tiene tratamiento."),
                Seccion("Síndrome de ovario poliquístico (SOP)", "Es la causa más frecuente de ciclos irregulares. Ver el artículo propio."),
                Seccion("Tiroides", "Tanto el hipotiroidismo como el hipertiroidismo pueden alterar el ciclo. También dan cansancio, " +
                    "cambios de peso, de ánimo o de temperatura. Se detecta con un análisis de sangre (TSH)."),
                Seccion("Prolactina alta", "La prolactina es la hormona de la lactancia. Si está alta sin estar amamantando, puede " +
                    "frenar el período y a veces hacer salir líquido de los pezones. Algunos medicamentos la suben."),
                Seccion("Estrés, peso y ejercicio", "El estrés intenso, comer poco, bajar mucho de peso o entrenar muy fuerte pueden " +
                    "\"apagar\" el ciclo desde el cerebro (amenorrea hipotalámica). Se recupera al cuidar esas causas."),
                Seccion("Perimenopausia", "En los años previos a la menopausia, por lo general después de los 40, las hormonas " +
                    "suben y bajan de forma despareja: los ciclos se acortan, se alargan o aparecen sofocos."),
                Seccion("Cómo se estudia", "Con una consulta, un análisis de sangre en un día indicado del ciclo y, a veces, una " +
                    "ecografía. Llevá a la consulta el informe de tus ciclos: ayuda mucho."),
            ),
            listOf(ACOG, NHS, OMS),
        ),

        // ---------- Salud menstrual ----------
        Articulo(
            "dolor", "Salud menstrual", "Dolor menstrual",
            "Por qué duele, qué alivia y cuándo no es normal.",
            listOf(
                Seccion("Por qué duele", "Para desprender el endometrio, el útero se contrae. Esas contracciones las provocan unas " +
                    "sustancias llamadas prostaglandinas: cuantas más hay, más duele. Suele empezar uno o dos días antes del " +
                    "sangrado y durar entre 1 y 3 días."),
                Seccion("Qué alivia", "Calor en la panza o la espalda, actividad física suave, dormir bien. Los antiinflamatorios " +
                    "como el ibuprofeno bajan las prostaglandinas y funcionan mejor si se toman apenas empieza el dolor; usalos " +
                    "según el prospecto o lo que te indique tu profesional. Algunos anticonceptivos hormonales también lo reducen."),
                Seccion("Cuándo consultar", "Si el dolor no se calma con analgésicos comunes, te hace faltar al trabajo o al estudio, " +
                    "empeora con los años, duele al tener relaciones o al ir al baño, o aparece fuera del período. Puede ser " +
                    "endometriosis, adenomiosis o miomas, y todos tienen tratamiento."),
            ),
            listOf(ACOG, NHS),
        ),
        Articulo(
            "endometriosis", "Salud menstrual", "Endometriosis",
            "Una enfermedad frecuente que tarda años en diagnosticarse.",
            listOf(
                Seccion("Qué es", "Tejido parecido al endometrio crece fuera del útero, por ejemplo en los ovarios o en el peritoneo, " +
                    "y provoca inflamación y dolor. Según la OMS afecta a alrededor del 10 % de las mujeres y niñas en edad " +
                    "reproductiva: unos 190 millones en el mundo."),
                Seccion("Síntomas", "Dolor menstrual fuerte, dolor pélvico fuera del período, dolor en las relaciones sexuales, al " +
                    "orinar o al defecar durante el período, cansancio y, en algunos casos, dificultad para quedar embarazada. " +
                    "También puede no dar síntomas."),
                Seccion("Por qué tarda el diagnóstico", "Porque muchas veces se toma el dolor menstrual fuerte como algo normal. " +
                    "No lo es. Anotar el dolor y los síntomas en cada ciclo ayuda mucho en la consulta."),
                Seccion("Tratamiento", "No tiene cura definitiva, pero sí tratamiento: analgésicos, tratamientos hormonales y, en " +
                    "algunos casos, cirugía. Lo indica un equipo de ginecología."),
            ),
            listOf(OMS, ACOG),
        ),
        Articulo(
            "sop", "Salud menstrual", "Síndrome de ovario poliquístico (SOP)",
            "El trastorno hormonal más común en edad reproductiva.",
            listOf(
                Seccion("Qué es", "Un desbalance hormonal en el que los ovarios fabrican más andrógenos (hormonas como la " +
                    "testosterona) y la ovulación se vuelve irregular. Según la OMS afecta a entre el 8 y el 13 % de las " +
                    "mujeres en edad reproductiva, y hasta el 70 % no lo sabe."),
                Seccion("Cómo se diagnostica", "Se necesitan 2 de estas 3 cosas: ciclos irregulares o ausentes, signos de " +
                    "andrógenos altos (acné, más vello en cara o cuerpo, caída de pelo, o un análisis) y ovarios con muchos " +
                    "folículos en la ecografía. Antes se descartan otras causas, como la tiroides."),
                Seccion("Por qué importa", "Además de los ciclos, el SOP se asocia a resistencia a la insulina, más riesgo de " +
                    "diabetes tipo 2 y dificultad para quedar embarazada. Con seguimiento se puede manejar bien."),
                Seccion("Tratamiento", "Depende de lo que más te afecte: hábitos de alimentación y movimiento, anticonceptivos " +
                    "hormonales para regular el ciclo y la piel, medicamentos para la insulina o para ayudar a ovular si buscás " +
                    "embarazo."),
            ),
            listOf(OMS, ACOG, NHS),
        ),
        Articulo(
            "abundante", "Salud menstrual", "Sangrado abundante",
            "Cómo darte cuenta de si sangrás de más.",
            listOf(
                Seccion("Señales", "Cambiar la toalla o el tampón cada 1 o 2 horas, o necesitar dos a la vez. Despertarte de noche " +
                    "para cambiarte. Coágulos más grandes que una moneda grande. Período de más de 7 u 8 días. Manchar la ropa " +
                    "seguido o dejar de hacer cosas por el sangrado."),
                Seccion("Por qué importa", "Perder mucha sangre todos los meses puede causar anemia por falta de hierro: cansancio, " +
                    "falta de aire, mareos, palidez."),
                Seccion("Causas", "Miomas, pólipos, adenomiosis, problemas de coagulación, desbalances hormonales, el DIU de cobre " +
                    "o algunos medicamentos. Muchas veces no se encuentra una causa, y aun así se puede tratar."),
                Seccion("Qué hacer", "Consultá. Hay tratamientos con y sin hormonas. Si empapás una toalla por hora durante más de " +
                    "dos horas seguidas, o te mareás mucho, andá a la guardia."),
            ),
            listOf(ACOG, NHS, FIGO),
        ),
        Articulo(
            "spm", "Salud menstrual", "Síndrome premenstrual (SPM) y TDPM",
            "Cuando la semana anterior al período se hace cuesta arriba.",
            listOf(
                Seccion("SPM", "Síntomas físicos y emocionales que aparecen en los días previos al período y se van cuando empieza " +
                    "o a los pocos días: hinchazón, senos sensibles, dolor de cabeza, irritabilidad, tristeza, ansiedad, antojos. " +
                    "Es muy común."),
                Seccion("TDPM", "El trastorno disfórico premenstrual es una forma intensa, que afecta a una minoría (alrededor de 3 a " +
                    "8 de cada 100). Los síntomas emocionales son tan fuertes que complican el trabajo, el estudio o los vínculos."),
                Seccion("Por qué pasa", "No es que las hormonas estén mal: algunas personas son más sensibles a la caída normal del " +
                    "estrógeno y la progesterona, que afecta a la serotonina."),
                Seccion("Qué ayuda", "Moverse, dormir bien, reducir alcohol, sal y cafeína esos días. Si no alcanza, hay tratamientos: " +
                    "algunos anticonceptivos hormonales, antidepresivos en dosis bajas y terapia. Anotar tu ánimo en Eirmena " +
                    "ayuda a mostrar el patrón en la consulta."),
            ),
            listOf(ACOG, NHS),
        ),

        // ---------- Anticoncepción ----------
        Articulo(
            "metodos", "Anticoncepción", "Métodos anticonceptivos y su eficacia",
            "Cuánto protege cada método en la vida real.",
            listOf(
                Seccion("Uso típico", "Los números dicen cuántas de cada 100 personas que usan el método quedan embarazadas en un " +
                    "año, usándolo como se usa en la vida real, con olvidos y errores."),
                Seccion("Los más eficaces", "Implante: menos de 1. DIU hormonal: menos de 1. DIU de cobre: menos de 1. No dependen " +
                    "de acordarse de nada."),
                Seccion("Muy eficaces si se usan bien", "Inyección: 4. Pastilla, parche y anillo: 7. Fallan sobre todo por olvidos."),
                Seccion("Menos eficaces", "Preservativo externo: 13. Preservativo interno: 21. Retiro (\"acabar afuera\"): 20. " +
                    "Métodos basados en el calendario o los síntomas del ciclo: entre 2 y 23, según cómo se usen. Las apps de " +
                    "ciclo, como esta, no son un método anticonceptivo."),
                Seccion("Doble protección", "El preservativo es el único método que además protege de las infecciones de " +
                    "transmisión sexual. Se puede combinar con cualquier otro."),
                Seccion("En Argentina", "Por la Ley 25.673 los métodos anticonceptivos son gratuitos en hospitales y centros de salud " +
                    "públicos, y las obras sociales y prepagas tienen que cubrirlos."),
            ),
            listOf(CDC, OMS, MSAL),
        ),
        Articulo(
            "emergencia", "Anticoncepción", "Anticoncepción de emergencia",
            "La \"pastilla del día después\": cuándo y cómo.",
            listOf(
                Seccion("Cuándo usarla", "Después de una relación sin protección o si falló el método: se rompió el preservativo, " +
                    "olvidaste pastillas, se salió el anillo."),
                Seccion("Cuanto antes, mejor", "La pastilla de levonorgestrel funciona mejor cuanto antes se tome, idealmente en las " +
                    "primeras 12 a 24 horas. Se puede tomar hasta 72 horas después, y algunas guías la extienden hasta 120 horas " +
                    "con menor eficacia."),
                Seccion("Qué no es", "No es abortiva: actúa retrasando la ovulación y no afecta un embarazo ya instalado. Tampoco " +
                    "protege de las relaciones que tengas después: usá preservativo hasta tu próximo período."),
                Seccion("Efectos", "Puede dar náuseas o dolor de cabeza, y adelantar o atrasar unos días el próximo período. Si el " +
                    "período se atrasa más de una semana, hacete un test de embarazo."),
                Seccion("Otra opción", "El DIU de cobre colocado dentro de los 5 días es la anticoncepción de emergencia más eficaz, " +
                    "y después sigue funcionando como método."),
                Seccion("En Argentina", "Se entrega gratis en hospitales y centros de salud, y también se consigue en farmacias."),
            ),
            listOf(OMS, MSAL, ACOG),
        ),
        Articulo(
            "olvido", "Anticoncepción", "Si te olvidaste la pastilla",
            "Qué hacer según cuántas te olvidaste (pastillas combinadas).",
            listOf(
                Seccion("Primero", "Estas son las reglas generales para pastillas combinadas (las que tienen estrógeno y progestágeno). " +
                    "Las pastillas solo de progestágeno tienen otras reglas. Ante la duda, seguí el prospecto de tu marca o " +
                    "consultá en la farmacia."),
                Seccion("Una sola (menos de 48 horas desde la última)", "Tomala apenas te acuerdes, aunque sean dos el mismo día, y " +
                    "seguí como siempre. La protección se mantiene."),
                Seccion("Dos o más (48 horas o más)", "Tomá la última que olvidaste, descartá las otras y seguí con el blíster. " +
                    "Usá preservativo durante los próximos 7 días."),
                Seccion("Si fue en la primera semana", "Si además tuviste relaciones sin preservativo en los días anteriores, " +
                    "considerá la anticoncepción de emergencia."),
                Seccion("Si fue en la tercera semana", "Terminá las activas y empezá el blíster siguiente sin hacer la pausa (o sin " +
                    "tomar las de placebo)."),
                Seccion("Vómitos o diarrea", "Si vomitaste dentro de las 3 horas de tomarla, o tenés diarrea fuerte, cuenta como " +
                    "olvido."),
            ),
            listOf(OMS, NHS),
        ),

        // ---------- Salud sexual ----------
        Articulo(
            "its", "Salud sexual", "ITS: prevención y controles",
            "Infecciones de transmisión sexual, preservativo, vacunas y chequeos.",
            listOf(
                Seccion("Qué son", "Infecciones que se transmiten en las relaciones sexuales, como el VIH, la sífilis, la gonorrea, " +
                    "la clamidia, el herpes o el VPH. Muchas no dan síntomas, por eso conviene hacerse controles."),
                Seccion("Cómo prevenir", "El preservativo (externo o interno) y el campo de látex en el sexo oral son la forma más " +
                    "eficaz. Ningún otro anticonceptivo protege de las ITS."),
                Seccion("Vacunas", "La vacuna contra el VPH está en el calendario nacional a los 11 años, para todas las personas, y " +
                    "previene la mayoría de los cánceres de cuello de útero. La de hepatitis B también es gratuita."),
                Seccion("Controles", "Los tests de VIH, sífilis y hepatitis son gratuitos y confidenciales en los hospitales y " +
                    "centros de salud públicos. Para el cáncer de cuello de útero, el Ministerio de Salud recomienda el PAP a " +
                    "partir de los 25 años y, donde está disponible, el test de VPH a partir de los 30."),
                Seccion("Señales para consultar", "Flujo con olor o color distinto, ardor al orinar, llagas, verrugas, picazón, dolor " +
                    "en las relaciones o sangrado después de tenerlas."),
            ),
            listOf(MSAL, OMS),
        ),

        // ---------- Etapas ----------
        Articulo(
            "buscar-embarazo", "Etapas", "Buscar un embarazo",
            "Días fértiles, ácido fólico y cuándo consultar.",
            listOf(
                Seccion("Tus días fértiles", "Son unos 6 por ciclo: los 5 días antes de la ovulación y el día de la ovulación. " +
                    "El esperma vive hasta 5 días en el cuerpo y el óvulo, unas 24 horas. Las chances más altas están en los 2 o 3 " +
                    "días previos a ovular."),
                Seccion("Qué tan seguido", "Tener relaciones cada 1 o 2 días durante la ventana fértil da las mejores chances. " +
                    "No hace falta calcular el minuto exacto, y no hay posiciones que ayuden más."),
                Seccion("Ácido fólico", "Tomá 400 microgramos de ácido fólico por día desde al menos un mes antes de buscar el " +
                    "embarazo y durante los primeros meses: previene defectos del tubo neural. Se consigue gratis en los centros de salud."),
                Seccion("Antes de buscar", "Una consulta preconcepcional sirve para revisar vacunas, análisis, medicación que tomes " +
                    "y enfermedades previas. Dejar el tabaco y el alcohol también ayuda."),
                Seccion("Cuándo consultar", "Si después de 12 meses de relaciones sin protección no hubo embarazo, o después de " +
                    "6 meses si tenés 35 años o más. Consultá antes si tenés más de 40, ciclos muy irregulares, endometriosis " +
                    "conocida o algún antecedente."),
            ),
            listOf(ACOG, OMS, MSAL),
        ),
        Articulo(
            "temperatura-moco", "Etapas", "Temperatura basal, flujo y tests de ovulación",
            "Cómo leer las señales de tu cuerpo para encontrar la ovulación.",
            listOf(
                Seccion("Temperatura basal", "Es tu temperatura en reposo. Medila al despertar, antes de levantarte, hablar o tomar " +
                    "agua, a la misma hora y con un termómetro basal (de dos decimales). Después de ovular, la progesterona la sube " +
                    "unos 0,2 a 0,5 °C y se queda alta hasta el período."),
                Seccion("Qué te muestra", "La suba confirma que la ovulación ya pasó; no la anticipa. Con algunos ciclos anotados " +
                    "vas a ver en qué día sueles ovular. Dormir mal, el alcohol, la fiebre o los viajes la alteran: anotalo."),
                Seccion("El flujo", "A medida que se acerca la ovulación, el flujo pasa de seco o pegajoso a cremoso, y después a " +
                    "acuoso y elástico como clara de huevo. Ese último es el más fértil: ayuda al esperma a avanzar."),
                Seccion("Tests de ovulación", "Detectan el pico de LH en la orina. Un positivo indica que vas a ovular en las " +
                    "próximas 24 a 36 horas: ese día y el siguiente son los mejores para buscar. Hacelos a la tarde y sin tomar " +
                    "mucho líquido antes."),
                Seccion("En Eirmena", "Si anotás la temperatura o un test positivo, Eirmena ajusta la fecha de tu próximo período " +
                    "a partir de la ovulación que detecta. No uses estas señales como método anticonceptivo."),
            ),
            listOf(ACOG, NHS),
        ),
        Articulo(
            "controles-embarazo", "Etapas", "Controles del embarazo",
            "Qué estudios y vacunas tocan en cada trimestre.",
            listOf(
                Seccion("Cuántos", "La OMS recomienda al menos 8 contactos con el equipo de salud durante el embarazo. El primero, " +
                    "lo antes posible, idealmente antes de la semana 12."),
                Seccion("Primer trimestre", "Análisis de sangre (grupo y factor, hemograma, glucemia, VIH, sífilis, hepatitis B, " +
                    "toxoplasmosis y Chagas) y de orina. Entre las semanas 11 y 14, la primera ecografía."),
                Seccion("Segundo trimestre", "Entre las semanas 20 y 24, la ecografía morfológica. Entre la 24 y la 28, la prueba " +
                    "de glucosa para la diabetes gestacional. Desde la semana 20, la vacuna triple bacteriana acelular."),
                Seccion("Tercer trimestre", "Entre las semanas 32 y 36, la vacuna contra el virus sincicial respiratorio (en la " +
                    "temporada indicada). Entre la 35 y la 37, el estudio del estreptococo. Los controles se hacen más seguidos."),
                Seccion("Siempre", "La vacuna antigripal se da en cualquier trimestre. En Argentina los controles, estudios y " +
                    "vacunas del embarazo son gratuitos en el sistema público."),
            ),
            listOf(OMS, MSAL),
        ),
        Articulo(
            "alarma-embarazo", "Etapas", "Señales de alarma en el embarazo",
            "Cuándo ir a la guardia sin esperar al próximo control.",
            listOf(
                Seccion("Andá a la guardia si", "• Tenés sangrado vaginal.\n" +
                    "• Perdés líquido por la vagina.\n" +
                    "• Tenés dolor de panza fuerte o que no se va.\n" +
                    "• Tenés fiebre.\n" +
                    "• Te duele mucho la cabeza, ves borroso o con lucecitas, o se te hinchan de golpe la cara y las manos: puede " +
                    "ser preeclampsia.\n" +
                    "• Después de la semana 28 el bebé se mueve mucho menos que siempre.\n" +
                    "• Tenés contracciones regulares antes de la semana 37.\n" +
                    "• Te arde al orinar o tenés vómitos que no te dejan retener nada."),
                Seccion("En el primer trimestre", "Un dolor fuerte de un lado del bajo vientre, con o sin sangrado, o mareos y " +
                    "desmayos, pueden ser un embarazo ectópico: es una urgencia."),
                Seccion("Ante la duda", "Consultá. En el embarazo nunca está de más."),
            ),
            listOf(MSAL, NHS, ACOG),
        ),
        Articulo(
            "posparto", "Etapas", "Después del embarazo",
            "El período, la lactancia y la anticoncepción después de un parto o una pérdida.",
            listOf(
                Seccion("Los primeros días", "Después de un parto hay un sangrado llamado loquios, que dura unas 4 a 6 semanas y " +
                    "se va aclarando. No es un período."),
                Seccion("Cuándo vuelve el período", "Sin lactancia, suele volver entre las 6 y 8 semanas. Con lactancia exclusiva " +
                    "puede tardar meses. Después de una pérdida o una interrupción, suele volver en 4 a 6 semanas."),
                Seccion("Podés quedar embarazada antes", "La ovulación llega antes que el primer período, así que se puede quedar " +
                    "embarazada sin haber menstruado. Si no buscás otro embarazo, hablá de anticoncepción en tu control: hay " +
                    "métodos compatibles con la lactancia."),
                Seccion("Cómo te sentís", "Es común sentir tristeza o llanto fácil los primeros días. Si dura más de dos semanas, " +
                    "o sentís angustia, que no podés con todo o pensamientos de hacerte daño, pedí ayuda: la depresión posparto es " +
                    "frecuente y tiene tratamiento. Después de una pérdida, el duelo también necesita su tiempo y su espacio."),
                Seccion("Señales de alarma", "Fiebre, sangrado que empapa una toalla por hora, dolor fuerte, o una mama roja, " +
                    "caliente y dolorosa: consultá enseguida."),
            ),
            listOf(OMS, NHS, MSAL),
        ),
        Articulo(
            "perimenopausia", "Etapas", "Perimenopausia y menopausia",
            "Qué cambia, qué es normal y qué se puede hacer.",
            listOf(
                Seccion("Qué es cada una", "La perimenopausia son los años previos a la menopausia, cuando las hormonas empiezan a " +
                    "subir y bajar de forma despareja. Suele empezar entre los 40 y los 50. La menopausia es el último período: se " +
                    "confirma después de 12 meses seguidos sin menstruar. En promedio llega alrededor de los 50."),
                Seccion("Síntomas frecuentes", "Ciclos más cortos, más largos o salteados; sofocos y sudores nocturnos; problemas " +
                    "para dormir; sequedad vaginal; cambios de ánimo; dificultad para concentrarte; dolor de articulaciones. No " +
                    "todas las personas los tienen."),
                Seccion("Qué ayuda", "Ropa en capas, dormir en un ambiente fresco, moverse, reducir alcohol y cafeína. Para los " +
                    "síntomas que molestan mucho existe la terapia hormonal y otros tratamientos sin hormonas: se decide en la " +
                    "consulta según tu historia. Los lubricantes e hidratantes vaginales alivian la sequedad."),
                Seccion("Cuidá tus huesos y tu corazón", "Con menos estrógeno se pierde masa ósea más rápido y sube el riesgo " +
                    "cardiovascular. Calcio, vitamina D, ejercicio con peso y controles ayudan."),
                Seccion("Todavía podés quedar embarazada", "Hasta que pasen 12 meses sin período, si no buscás un embarazo seguí " +
                    "usando un método anticonceptivo."),
                Seccion("Consultá si", "Sangrás después de 12 meses sin período, los sangrados son muy abundantes o muy seguidos, " +
                    "o los síntomas te cambian la vida."),
            ),
            listOf(OMS, ACOG, NHS),
        ),
        Articulo(
            "primera-menstruacion", "Etapas", "Tu primera menstruación",
            "Qué esperar, cómo prepararte y qué es normal al principio.",
            listOf(
                Seccion("Cuándo llega", "Suele llegar entre los 10 y los 15 años, unos 2 a 3 años después de que empiezan a " +
                    "crecer los pechos. Unos meses antes es común notar un flujo blanco o transparente en la bombacha."),
                Seccion("Armá tu kit", "Llevá en la mochila una toallita o dos, una bombacha de repuesto y unos pañuelitos. Si te " +
                    "viene en la escuela, podés pedir ayuda a una persona adulta de confianza: es algo normal."),
                Seccion("Qué es normal al principio", "Los primeros años los ciclos suelen ser irregulares: pueden durar entre 21 y " +
                    "45 días, o saltearse algún mes. El sangrado dura de 2 a 7 días y puede ser marrón al principio o al final."),
                Seccion("El dolor", "Algunos cólicos son normales. El calor y moverse ayudan. Si el dolor no te deja ir a la " +
                    "escuela, contale a una persona adulta y consultá."),
                Seccion("Consultá si", "Cumpliste 15 años y todavía no te vino; pasaron 3 años desde que te crecieron los pechos " +
                    "y no te vino; pasan más de 90 días sin período; sangrás más de 7 días o cambiás la toallita cada 1 o 2 horas."),
                Seccion("Hablarlo", "No tiene nada de vergonzoso. Preguntar es la mejor forma de cuidarte."),
            ),
            listOf(ACOG, NHS, MSAL),
        ),

        // ---------- Gestión menstrual ----------
        Articulo(
            "productos", "Gestión menstrual", "Toallitas, tampones, copa y bombacha menstrual",
            "Cómo usar cada uno de forma segura.",
            listOf(
                Seccion("Toallitas", "Cambialas cada 4 a 6 horas, o antes si se llenan. Las de tela se lavan y se reusan."),
                Seccion("Tampones", "Usá la menor absorción que necesites y cambialo cada 4 a 8 horas. No lo dejes toda la noche más " +
                    "de 8 horas. Usar tampón no tiene nada que ver con la virginidad."),
                Seccion("Copa menstrual", "Se puede dejar hasta 12 horas. Vaciala, enjuagala y esterilizala hirviéndola al final de " +
                    "cada período. Si tenés DIU, consultá antes: al sacarla, el vacío podría mover los hilos."),
                Seccion("Bombacha menstrual", "Absorbe el sangrado y se lava a mano con agua fría. Según el flujo, dura de unas horas " +
                    "a un día."),
                Seccion("Síndrome de shock tóxico", "Es muy raro, pero grave. Si usás tampón o copa y aparece fiebre alta de golpe, " +
                    "vómitos, diarrea, mareo o una erupción parecida a una quemadura de sol, sacalo y andá a la guardia."),
            ),
            listOf(NHS, ACOG),
        ),

        // ---------- Mitos ----------
        Articulo(
            "mitos", "Mitos y verdades", "Mitos sobre la menstruación",
            "Lo que te contaron y no es cierto.",
            listOf(
                Seccion("\"Durante el período no te podés quedar embarazada\"", "Falso. Es menos probable, pero el esperma vive hasta " +
                    "5 días en el cuerpo y con ciclos cortos la ovulación puede llegar pronto."),
                Seccion("\"No te bañes ni te laves el pelo\"", "Falso. Bañarse es higiene y hasta alivia los cólicos con el agua caliente."),
                Seccion("\"No hagas ejercicio\"", "Falso. Moverse suele aliviar el dolor. Si un día tenés poca energía, bajá la " +
                    "intensidad, nada más."),
                Seccion("\"Las amigas que conviven se sincronizan\"", "Los estudios con muchos ciclos no lo comprobaron. Es una " +
                    "coincidencia frecuente porque los ciclos varían."),
                Seccion("\"La sangre menstrual es sucia\"", "Falso. Es sangre y tejido del útero, nada tóxico ni sucio."),
                Seccion("\"El ciclo tiene que durar 28 días\"", "Falso. Entre 24 y 38 días es normal."),
                Seccion("\"La pastilla del día después es abortiva\"", "Falso. Retrasa la ovulación y no afecta un embarazo ya instalado."),
            ),
            listOf(ACOG, NHS, OMS),
        ),

        // ---------- Ayuda ----------
        Articulo(
            "guardia", "Ayuda", "Cuándo ir a la guardia",
            "Señales que no pueden esperar a un turno.",
            listOf(
                Seccion("Andá a la guardia si", "• Empapás una toalla o un tampón por hora durante más de 2 horas seguidas.\n" +
                    "• Tenés un dolor de panza muy fuerte y repentino, sobre todo de un lado, y tu período está atrasado o el test " +
                    "dio positivo: puede ser un embarazo ectópico.\n" +
                    "• Tenés fiebre con dolor en la pelvis o flujo con mal olor.\n" +
                    "• Te desmayás o te mareás mucho.\n" +
                    "• Estás embarazada y sangrás o tenés dolor fuerte.\n" +
                    "• Usás tampón o copa y tenés fiebre alta, vómitos y una erupción en la piel."),
                Seccion("Pedí un turno pronto si", "• Sangrás después de la menopausia (un año o más sin período).\n" +
                    "• Sangrás después de tener relaciones.\n" +
                    "• Pasaste 3 meses sin período sin estar embarazada."),
                Seccion("Emergencias", "Llamá al 107 (SAME, en CABA) o al 911."),
            ),
            listOf(NHS, ACOG, MSAL),
        ),
        Articulo(
            "lineas", "Ayuda", "Líneas de ayuda en Argentina",
            "Teléfonos gratuitos y confidenciales.",
            listOf(
                Seccion("Salud sexual: 0800 222 3444", "Línea del Ministerio de Salud de la Nación. Gratuita y confidencial, desde " +
                    "todo el país. Informa sobre métodos anticonceptivos, anticoncepción de emergencia, ITS, interrupción del " +
                    "embarazo y te orienta si en un centro de salud no te atienden como corresponde."),
                Seccion("Violencia de género: 144", "Atiende las 24 horas, todos los días, en todo el país. También por WhatsApp " +
                    "al 11 2771 6463."),
                Seccion("Emergencias: 911 o 107", "Para una urgencia médica o si estás en peligro."),
                Seccion("Interrupción del embarazo", "En Argentina, la Ley 27.610 garantiza el acceso a la interrupción voluntaria " +
                    "del embarazo hasta la semana 14 inclusive, y después en caso de violación o riesgo para la vida o la salud. " +
                    "Es gratuita en el sistema público. La línea 0800 222 3444 informa dónde acceder."),
            ),
            listOf(MSAL),
        ),
    )

    val categorias: List<String> get() = articulos.map { it.categoria }.distinct()

    fun porId(id: String): Articulo? = articulos.firstOrNull { it.id == id }
}
