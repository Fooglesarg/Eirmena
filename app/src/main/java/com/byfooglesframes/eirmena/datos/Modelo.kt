package com.byfooglesframes.eirmena.datos

import java.time.LocalDate
import java.time.LocalTime

/** Intensidad del sangrado de un día. MANCHADO no cuenta como inicio de período. */
enum class Flujo(val nombre: String) {
    MANCHADO("Manchado"),
    LEVE("Leve"),
    MEDIO("Medio"),
    ABUNDANTE("Abundante");

    val cuentaComoPeriodo: Boolean get() = this != MANCHADO
}

/** [soloPerimenopausia]: se ofrecen en la hoja del día solo en esa etapa (o si ya están marcados). */
enum class Sintoma(val nombre: String, val soloPerimenopausia: Boolean = false) {
    COLICOS("Cólicos"),
    DOLOR_CABEZA("Dolor de cabeza"),
    HINCHAZON("Hinchazón"),
    SENOS("Senos sensibles"),
    ACNE("Acné"),
    DOLOR_ESPALDA("Dolor de espalda"),
    CANSANCIO("Cansancio"),
    NAUSEAS("Náuseas"),
    ANTOJOS("Antojos"),
    DIARREA("Diarrea"),
    ESTRENIMIENTO("Estreñimiento"),
    MAREOS("Mareos"),
    FLUJO_VAGINAL("Flujo vaginal"),
    DOLOR_OVULACION("Dolor de ovulación"),
    SOFOCOS("Sofocos", true),
    SUDORES_NOCTURNOS("Sudores nocturnos", true),
    SEQUEDAD_VAGINAL("Sequedad vaginal", true),
    INSOMNIO("Insomnio", true),
    NIEBLA_MENTAL("Dificultad para concentrarte", true),
    DOLOR_ARTICULAR("Dolor de articulaciones", true),
    PALPITACIONES("Palpitaciones", true),
}

/** Estados de ánimo como sustantivos, así sirven para cualquier persona. */
enum class Animo(val nombre: String) {
    CALMA("Calma"),
    ALEGRIA("Alegría"),
    ENERGIA_SOCIAL("Ganas de ver gente"),
    SENSIBILIDAD("Sensibilidad"),
    TRISTEZA("Tristeza"),
    IRRITABILIDAD("Irritabilidad"),
    ANSIEDAD("Ansiedad"),
    CAMBIOS("Cambios de humor"),
    DESGANO("Desgano"),
}

/** Escala simple de tres niveles para energía, sueño y deseo. */
enum class Nivel { BAJO, MEDIO, ALTO }

/** Moco o flujo cervical, de menos a más fértil. */
enum class Moco(val nombre: String, val fertil: Boolean) {
    SECO("Seco o nada", false),
    PEGAJOSO("Pegajoso", false),
    CREMOSO("Cremoso", false),
    ACUOSO("Acuoso", true),
    CLARA("Elástico, como clara de huevo", true),
}

enum class Test(val nombre: String) { NEGATIVO("Negativo"), POSITIVO("Positivo") }

/** Etapa de vida que elige la persona; cambia lo que muestra Hoy, las alertas y los avisos. */
enum class EtapaVida(val nombre: String, val descripcion: String) {
    CICLO("Registrar mi ciclo", "Seguir tu período, síntomas y ánimo."),
    BUSCO_EMBARAZO("Busco un embarazo", "Suma temperatura basal, flujo y tests de ovulación para encontrar tus días fértiles."),
    EMBARAZO("Estoy cursando un embarazo", "Semana a semana, fecha probable de parto, controles y señales de alarma."),
    PERIMENOPAUSIA("Perimenopausia o menopausia", "Ciclos que cambian, sofocos y otros síntomas de esta etapa."),
    PRIMEROS_CICLOS("Mis primeros ciclos", "Para la primera menstruación y los primeros años, cuando todo es nuevo."),
}

enum class FinEmbarazo(val nombre: String) {
    NACIMIENTO("Nacimiento"),
    PERDIDA("Pérdida del embarazo"),
    INTERRUPCION("Interrupción del embarazo"),
}

/** [fum]: primer día de la última menstruación; las semanas se cuentan desde ahí. */
data class Embarazo(val fum: LocalDate, val fin: LocalDate? = null, val resultado: FinEmbarazo? = null) {
    val fechaProbable: LocalDate get() = fum.plusDays(280)
}

enum class Relacion(val nombre: String) {
    CON_PRESERVATIVO("Con preservativo"),
    SIN_PRESERVATIVO("Sin preservativo"),
    MASTURBACION("Masturbación"),
}

/** Lo que la persona anotó en un día. */
data class RegistroDia(
    val fecha: LocalDate,
    val flujo: Flujo? = null,
    val nota: String = "",
    val sintomas: Set<Sintoma> = emptySet(),
    val animo: Set<Animo> = emptySet(),
    val energia: Nivel? = null,
    val sueno: Nivel? = null,
    val deseo: Nivel? = null,
    val relaciones: Set<Relacion> = emptySet(),
    val pastillaEmergencia: Boolean = false,
    /** Para quien usa pastilla anticonceptiva: si la tomó ese día. */
    val pastillaTomada: Boolean = false,
    /** Temperatura basal en °C, tomada al despertar. */
    val temperatura: Double? = null,
    val moco: Moco? = null,
    val testOvulacion: Test? = null,
    val testEmbarazo: Test? = null,
) {
    val vacio: Boolean
        get() = flujo == null && nota.isBlank() && sintomas.isEmpty() && animo.isEmpty() &&
            energia == null && sueno == null && deseo == null && relaciones.isEmpty() &&
            !pastillaEmergencia && !pastillaTomada && temperatura == null && moco == null &&
            testOvulacion == null && testEmbarazo == null

    /** Tiene algo anotado además del sangrado (para el punto del calendario). */
    val tieneDetalle: Boolean get() = !copy(flujo = null).vacio
}

data class Avisos(
    val periodo: Boolean = true,
    val fertil: Boolean = false,
    val anticonceptivo: Boolean = true,
    val registroDiario: Boolean = false,
    val horaRegistro: LocalTime = LocalTime.of(21, 0),
    /** El texto de la notificación no dice nada del ciclo (por si otra persona ve el teléfono). */
    val discretos: Boolean = true,
)

data class Ajustes(
    /** Duración del ciclo que la persona cree tener; se usa hasta tener historial propio. */
    val cicloHabitual: Int = 28,
    val periodoHabitual: Int = 5,
    val bienvenidaHecha: Boolean = false,
    val ocultarEnRecientes: Boolean = true,
    /** Cartas de "¿Sabías que…?" ya descubiertas, en orden. */
    val curiosidades: List<String> = emptyList(),
    /** Carta que sale en la próxima visita (sorteada entre todas, puede ser repetida). */
    val proximaCarta: String? = null,
    /** Día en que se completó la colección y se ganó el trofeo, o null. */
    val trofeo: LocalDate? = null,
    /** Alertas de salud que la persona marcó como vistas, con la fecha. */
    val alertasVistas: Map<String, LocalDate> = emptyMap(),
    val anticonceptivo: Anticonceptivo = Anticonceptivo(),
    val avisos: Avisos = Avisos(),
    val etapa: EtapaVida = EtapaVida.CICLO,
    /** Embarazo en curso (sin [Embarazo.fin]) o el último que terminó. */
    val embarazo: Embarazo? = null,
)

/** Todo lo que guarda Eirmena. Es exactamente lo que se exporta y se importa. */
data class Datos(
    val ajustes: Ajustes = Ajustes(),
    val dias: Map<LocalDate, RegistroDia> = emptyMap(),
) {
    fun conDia(registro: RegistroDia): Datos =
        copy(dias = if (registro.vacio) dias - registro.fecha else dias + (registro.fecha to registro))

    fun dia(fecha: LocalDate): RegistroDia = dias[fecha] ?: RegistroDia(fecha)

    /** Une un respaldo con lo actual; ante el mismo día, gana lo importado. */
    fun combinarCon(otros: Datos): Datos = copy(
        dias = dias + otros.dias,
        ajustes = ajustes.copy(curiosidades = (ajustes.curiosidades + otros.ajustes.curiosidades).distinct()),
    )

    companion object {
        const val CICLO_MIN = 15
        const val CICLO_MAX = 90
    }
}
