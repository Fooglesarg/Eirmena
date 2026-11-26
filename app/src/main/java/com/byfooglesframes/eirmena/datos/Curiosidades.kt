package com.byfooglesframes.eirmena.datos

/** Una carta del minijuego "¿Sabías que…?". */
data class Curiosidad(
    val id: String,
    val cultura: String,
    /** Lo que se ve con la carta boca abajo. */
    val pista: String,
    val titulo: String,
    val texto: String,
    /** Qué tiene que ver con Eirmena o con el cuerpo. */
    val conexion: String,
)

/**
 * Cada visita saca una carta al azar entre todas, aunque ya se tenga (como figuritas repetidas):
 * completar la colección lleva unas 60 visitas. La carta sorteada se guarda en los datos, así la
 * pista que se ve en Hoy es la de la carta que después se da vuelta.
 * Para agregar cartas, sumarlas con un id nuevo: los ids ya descubiertos se guardan en los datos.
 */
object Curiosidades {
    val todas = listOf(
        Curiosidad(
            id = "eir",
            cultura = "Mitología nórdica",
            pista = "…en el nombre de esta app se esconde una diosa vikinga?",
            titulo = "Eir, la mejor de las sanadoras",
            texto = "En la Edda de Snorri, Eir aparece entre las diosas nórdicas y se la describe como \"la mejor de las médicas\". " +
                "Un poema antiguo la ubica en Lyfjaberg, la \"colina de la curación\": se decía que toda mujer que " +
                "subía, aunque llevara mucho tiempo enferma, se curaba. Su nombre también figura en una lista de valquirias.",
            conexion = "Eir es la primera mitad de Eirmena: el cuidado y la salud.",
        ),
        Curiosidad(
            id = "mena",
            cultura = "Mitología romana",
            pista = "…los romanos tenían una diosa solo para la menstruación?",
            titulo = "Mena, la diosa del ciclo",
            texto = "Los romanos tenían un dios o una diosa para casi cada momento de la vida. Mena se ocupaba del flujo " +
                "menstrual. Sabemos de ella porque San Agustín la nombra en La ciudad de Dios, donde cuenta que la " +
                "llamaban hija de Júpiter. Su nombre viene de la misma raíz que \"mes\".",
            conexion = "Mena es la segunda mitad de Eirmena. Juntas, una nórdica y una romana, hablan del bienestar del cuerpo y del ciclo.",
        ),
        Curiosidad(
            id = "palabra-mes",
            cultura = "Latín y griego",
            pista = "…\"menstruación\" y \"luna\" son palabras parientes?",
            titulo = "Mes, luna y menstruación",
            texto = "\"Menstruación\" viene del latín menstruus, \"que pasa cada mes\", y mensis, \"mes\". En griego, mēnē " +
                "era la luna. Las dos palabras vienen de una raíz muy antigua que servía para medir el tiempo, porque los " +
                "meses se contaban con la luna.",
            conexion = "Por eso el ícono de Eirmena es una luna creciente.",
        ),
        Curiosidad(
            id = "luna-ciclo",
            cultura = "Ciencia",
            pista = "…la luna no maneja tu ciclo, aunque se parezcan?",
            titulo = "La luna y el ciclo: parecidos, no iguales",
            texto = "Una vuelta completa de fases lunares dura 29,5 días y el ciclo menstrual promedio ronda los 28 o 29. " +
                "De ese parecido nacieron muchos mitos. Pero los estudios con miles de ciclos no encontraron una relación " +
                "clara entre la luna y el día en que empieza el período. Tampoco se comprobó que las amigas que conviven \"se sincronicen\".",
            conexion = "Por eso Eirmena calcula con tu historial y no con el calendario lunar.",
        ),
        Curiosidad(
            id = "juno-lucina",
            cultura = "Mitología romana",
            pista = "…dar a luz tenía su propia diosa de la luz?",
            titulo = "Juno Lucina",
            texto = "A Juno, reina de los dioses romanos, se la invocaba en los partos como Lucina, \"la que trae a la luz\". " +
                "De ahí viene nuestra expresión \"dar a luz\". El 1 de marzo, en la fiesta de las Matronalia, las mujeres " +
                "llevaban flores a su templo.",
            conexion = "Más adelante Eirmena va a sumar un modo de embarazo, semana a semana.",
        ),
        Curiosidad(
            id = "carmenta",
            cultura = "Mitología romana",
            pista = "…había una diosa para los bebés que vienen de cola?",
            titulo = "Carmenta y sus dos caras",
            texto = "Carmenta era una diosa romana del parto y de la profecía. Se la invocaba con dos nombres: Prorsa, para " +
                "los bebés que nacen de cabeza, y Postverta, para los que vienen de nalgas. Sus fiestas, las Carmentalia, " +
                "se hacían en enero.",
            conexion = "Ya entonces sabían que cada parto es distinto.",
        ),
        Curiosidad(
            id = "ilitia",
            cultura = "Mitología griega",
            pista = "…una diosa celosa demoró un parto nueve días?",
            titulo = "Ilitía, la partera de los dioses",
            texto = "Ilitía era la diosa griega de los dolores de parto. Según el Himno homérico a Apolo, Hera, celosa, la " +
                "retuvo en el Olimpo para que no ayudara a Leto, que estuvo nueve días y nueve noches en trabajo de parto. " +
                "Recién cuando Ilitía llegó a la isla de Delos, nació Apolo.",
            conexion = "Sin una buena partera, ni los dioses la pasaban bien.",
        ),
        Curiosidad(
            id = "artemisa",
            cultura = "Mitología griega",
            pista = "…la diosa cazadora también cuidaba a las parturientas?",
            titulo = "Artemisa de Braurón",
            texto = "Artemisa, la diosa de la caza, también protegía a las mujeres en el parto. Se contaba que, apenas nacida, " +
                "ayudó a su madre a dar a luz a su mellizo Apolo. En su santuario de Braurón, cerca de Atenas, las mujeres " +
                "le ofrecían ropa después de parir.",
            conexion = "Una diosa que acompañaba a las mujeres en todas las etapas de la vida.",
        ),
        Curiosidad(
            id = "higia",
            cultura = "Mitología griega",
            pista = "…\"higiene\" es el nombre de una diosa?",
            titulo = "Higía, la salud en persona",
            texto = "Higía era hija de Asclepio, el dios de la medicina. Mientras su padre curaba enfermedades, ella representaba " +
                "mantenerse sano y prevenir. De su nombre viene la palabra \"higiene\". Su hermana Panacea, \"la que todo lo " +
                "cura\", también nos dejó una palabra.",
            conexion = "Prevenir y conocer tu cuerpo es justamente la idea de Eirmena.",
        ),
        Curiosidad(
            id = "palabras-diosas",
            cultura = "Grecia y Roma",
            pista = "…usás nombres de diosas sin darte cuenta?",
            titulo = "Diosas en el diccionario",
            texto = "\"Afrodisíaco\" viene de Afrodita, la diosa griega del amor y del deseo. \"Venéreo\", como en " +
                "\"enfermedad venérea\", viene de Venus, su versión romana. Y una de las explicaciones que da Ovidio para \"junio\" es que " +
                "lleva el nombre de Juno, protectora del matrimonio.",
            conexion = "La salud sexual también tiene su mitología.",
        ),
        Curiosidad(
            id = "freya",
            cultura = "Mitología nórdica",
            pista = "…la diosa nórdica del deseo viajaba en un carro tirado por gatos?",
            titulo = "Freya y sus gatos",
            texto = "Freya era la diosa nórdica del amor, del deseo y de la fertilidad. La Edda de Snorri cuenta que viajaba " +
                "en un carro tirado por dos gatos y que le gustaban las canciones de amor, así que convenía pedirle ayuda en " +
                "esos asuntos.",
            conexion = "El deseo y la libido cambian a lo largo del ciclo; Eirmena te va a dejar anotarlo.",
        ),
        Curiosidad(
            id = "frigg",
            cultura = "Mitología nórdica",
            pista = "…en los partos vikingos se rezaba a dos diosas a la vez?",
            titulo = "Frigg y Freya, juntas en el parto",
            texto = "En un poema de la Edda poética, una mujer que asiste un parto difícil desea a la madre que la ayuden " +
                "\"Frigg y Freya y otros dioses\". Frigg, esposa de Odín, era la protectora del hogar y de las madres.",
            conexion = "Nadie atraviesa un parto en soledad, ni siquiera en las sagas.",
        ),
        Curiosidad(
            id = "isis",
            cultura = "Antiguo Egipto",
            pista = "…existía un amuleto llamado \"la sangre de Isis\"?",
            titulo = "El nudo de Isis",
            texto = "El tyet, o nudo de Isis, era un amuleto egipcio que solía tallarse en jaspe rojo. El Libro de los Muertos " +
                "lo relaciona con \"la sangre de Isis\", la gran diosa madre, y se usaba como protección. Algunos " +
                "investigadores creen que representa un paño menstrual, aunque no hay acuerdo.",
            conexion = "Hace más de 3000 años, la sangre de una diosa ya era símbolo de protección y no de vergüenza.",
        ),
        Curiosidad(
            id = "tauret",
            cultura = "Antiguo Egipto",
            pista = "…una hipopótama era la protectora de las embarazadas?",
            titulo = "Tauret, la hipopótama protectora",
            texto = "Tauret tenía cuerpo de hipopótama embarazada, patas de león y cola de cocodrilo. Daba miedo a propósito: " +
                "espantaba a los peligros del embarazo y del parto. En las casas egipcias había amuletos y muebles " +
                "decorados con su figura.",
            conexion = "Cuidar el embarazo era tan importante que hacía falta una guardiana feroz.",
        ),
        Curiosidad(
            id = "mama-quilla",
            cultura = "Mundo andino",
            pista = "…para los incas, la luna era una madre que cuidaba a las mujeres?",
            titulo = "Mama Killa",
            texto = "Mama Killa, \"Madre Luna\", era la diosa inca de la luna. Marcaba el calendario de las fiestas y era " +
                "protectora de las mujeres y del matrimonio. Su templo en el Cusco estaba cubierto de plata, el metal que " +
                "representaba sus lágrimas.",
            conexion = "Otra cultura que unió la luna con el tiempo y con el cuidado de las mujeres.",
        ),
        Curiosidad(
            id = "pachamama",
            cultura = "Mundo andino",
            pista = "…en agosto se le da de comer a la tierra?",
            titulo = "La Pachamama y sus ciclos",
            texto = "En el noroeste argentino y en buena parte de los Andes, el 1 de agosto se le hace una ofrenda a la " +
                "Pachamama, la Madre Tierra. Se cava un pozo y se le da comida, bebida y hojas de coca para agradecerle y " +
                "pedirle fertilidad para el año que empieza.",
            conexion = "La tierra también tiene ciclos de descanso y de fertilidad, como el cuerpo.",
        ),
        Curiosidad(
            id = "toci",
            cultura = "Mundo mexica",
            pista = "…un baño de vapor tenía su propia diosa abuela?",
            titulo = "Toci y el temazcal",
            texto = "Los mexicas honraban a Toci, \"nuestra abuela\", como protectora de las parteras y de las curanderas. " +
                "Estaba ligada al temazcal, el baño de vapor que se usaba para sanar y para cuidar a las mujeres antes y " +
                "después del parto. En México todavía se usa en el posparto.",
            conexion = "Sanar y cuidar, como Eir, pero del otro lado del océano.",
        ),
        Curiosidad(
            id = "brigid",
            cultura = "Mitología celta",
            pista = "…una diosa irlandesa se volvió patrona de las parteras?",
            titulo = "Brigid, del fuego a las parteras",
            texto = "Brigid era la diosa irlandesa de la sanación, la poesía y la herrería. Su fiesta, Imbolc, el 1 de febrero, " +
                "anunciaba el fin del invierno. Con el cristianismo su figura se mezcló con Santa Brígida, que sigue siendo " +
                "patrona de las parteras y de los recién nacidos.",
            conexion = "Una sanadora que, como Eir, cruzó siglos.",
        ),
    )

    /** El dato que se gana junto con el trofeo, al completar la colección. */
    val datoDelTrofeo = Curiosidad(
        id = "trofeo",
        cultura = "Dato del trofeo",
        pista = "",
        titulo = "El espejo de Venus",
        texto = "El símbolo ♀ que hoy se usa para \"femenino\" era el de la diosa Venus, de su planeta y del cobre en la " +
            "alquimia. En 1751 el botánico Carlos Linneo lo usó por primera vez para marcar las plantas femeninas, y de " +
            "ahí pasó a toda la biología. Se suele decir que representa el espejo de mano de Venus, pero los " +
            "historiadores creen que viene de una abreviatura griega del nombre del planeta.",
        conexion = "Subiste toda la colina de Lyfjaberg: ahora conocés a las diosas que cuidaron los ciclos, los partos y la salud en todo el mundo.",
    )

    fun completa(vistas: List<String>): Boolean = todas.all { it.id in vistas }

    fun porId(id: String?): Curiosidad? = todas.firstOrNull { it.id == id }

    /** Sortea la próxima carta entre todas; solo evita repetir la que acaba de salir. */
    fun sortear(anterior: String?, azar: kotlin.random.Random = kotlin.random.Random): Curiosidad =
        todas.filter { it.id != anterior }.random(azar)
}
