package com.byfooglesframes.eirmena.datos

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime

/**
 * Formato de los datos, legible por personas y por otros programas.
 * Los campos nuevos son opcionales, así los respaldos viejos se siguen pudiendo importar.
 * Si algún día cambia algo de forma incompatible, se sube FORMATO y se migra en [desdeJson].
 */
object Json {
    const val FORMATO = 1

    fun aJson(datos: Datos, version: String = ""): String {
        val a = datos.ajustes
        val ac = a.anticonceptivo
        val av = a.avisos
        val raiz = JSONObject()
            .put("app", "Eirmena")
            .put("formato", FORMATO)
            .put("version", version)
            .put("ajustes", JSONObject()
                .put("cicloHabitual", a.cicloHabitual)
                .put("periodoHabitual", a.periodoHabitual)
                .put("bienvenidaHecha", a.bienvenidaHecha)
                .put("ocultarEnRecientes", a.ocultarEnRecientes)
                .put("curiosidades", JSONArray(a.curiosidades))
                .putOpt("proximaCarta", a.proximaCarta)
                .putOpt("trofeo", a.trofeo?.toString())
                .put("etapa", a.etapa.name)
                .putOpt("embarazo", a.embarazo?.let { e ->
                    JSONObject().put("fum", e.fum.toString()).putOpt("fin", e.fin?.toString()).putOpt("resultado", e.resultado?.name)
                })
                .put("alertasVistas", JSONObject().apply { a.alertasVistas.forEach { (k, v) -> put(k, v.toString()) } })
                .put("anticonceptivo", JSONObject()
                    .put("metodo", ac.metodo.name)
                    .putOpt("inicio", ac.inicio?.toString())
                    .put("hora", ac.hora.toString())
                    .put("esquema", ac.esquema.name)
                    .put("frecuencia", ac.frecuencia.name)
                    .putOpt("vence", ac.vence?.toString()))
                .put("avisos", JSONObject()
                    .put("periodo", av.periodo)
                    .put("fertil", av.fertil)
                    .put("anticonceptivo", av.anticonceptivo)
                    .put("registroDiario", av.registroDiario)
                    .put("horaRegistro", av.horaRegistro.toString())
                    .put("discretos", av.discretos)))
        val dias = JSONArray()
        datos.dias.values.sortedBy { it.fecha }.forEach { d ->
            val o = JSONObject().put("fecha", d.fecha.toString())
            d.flujo?.let { o.put("flujo", it.name) }
            if (d.nota.isNotBlank()) o.put("nota", d.nota)
            if (d.sintomas.isNotEmpty()) o.put("sintomas", JSONArray(d.sintomas.map { it.name }))
            if (d.animo.isNotEmpty()) o.put("animo", JSONArray(d.animo.map { it.name }))
            d.energia?.let { o.put("energia", it.name) }
            d.sueno?.let { o.put("sueno", it.name) }
            d.deseo?.let { o.put("deseo", it.name) }
            if (d.relaciones.isNotEmpty()) o.put("relaciones", JSONArray(d.relaciones.map { it.name }))
            if (d.pastillaEmergencia) o.put("pastillaEmergencia", true)
            if (d.pastillaTomada) o.put("pastillaTomada", true)
            d.temperatura?.let { o.put("temperatura", it) }
            d.moco?.let { o.put("moco", it.name) }
            d.testOvulacion?.let { o.put("testOvulacion", it.name) }
            d.testEmbarazo?.let { o.put("testEmbarazo", it.name) }
            dias.put(o)
        }
        raiz.put("dias", dias)
        return raiz.toString(2)
    }

    /** Lanza [FormatoInvalido] si el texto no es un respaldo de Eirmena. */
    fun desdeJson(texto: String): Datos {
        val raiz = try { JSONObject(texto) } catch (e: Exception) { throw FormatoInvalido("El archivo no es un respaldo de Eirmena.") }
        if (raiz.optString("app") != "Eirmena") throw FormatoInvalido("El archivo no es un respaldo de Eirmena.")
        val formato = raiz.optInt("formato", 0)
        if (formato > FORMATO) throw FormatoInvalido("El respaldo es de una versión más nueva de Eirmena. Actualizá la app para importarlo.")

        val aj = raiz.optJSONObject("ajustes") ?: JSONObject()
        val base = Ajustes()
        val ac = aj.optJSONObject("anticonceptivo") ?: JSONObject()
        val av = aj.optJSONObject("avisos") ?: JSONObject()
        val ajustes = Ajustes(
            cicloHabitual = aj.optInt("cicloHabitual", base.cicloHabitual).coerceIn(Datos.CICLO_MIN, Datos.CICLO_MAX),
            periodoHabitual = aj.optInt("periodoHabitual", base.periodoHabitual).coerceIn(1, 15),
            bienvenidaHecha = aj.optBoolean("bienvenidaHecha", true),
            ocultarEnRecientes = aj.optBoolean("ocultarEnRecientes", base.ocultarEnRecientes),
            curiosidades = textos(aj.optJSONArray("curiosidades")),
            proximaCarta = aj.optString("proximaCarta").takeIf { it.isNotEmpty() },
            trofeo = fecha(aj.optString("trofeo")),
            etapa = enumDe(aj.optString("etapa"), EtapaVida.CICLO),
            embarazo = aj.optJSONObject("embarazo")?.let { e ->
                fecha(e.optString("fum"))?.let { fum ->
                    Embarazo(fum, fecha(e.optString("fin")), enumONull<FinEmbarazo>(e.optString("resultado")))
                }
            },
            alertasVistas = aj.optJSONObject("alertasVistas")?.let { o ->
                o.keys().asSequence().mapNotNull { k -> fecha(o.optString(k))?.let { k to it } }.toMap()
            }.orEmpty(),
            anticonceptivo = Anticonceptivo(
                metodo = enumDe(ac.optString("metodo"), Metodo.NINGUNO),
                inicio = fecha(ac.optString("inicio")),
                hora = hora(ac.optString("hora")) ?: base.anticonceptivo.hora,
                esquema = enumDe(ac.optString("esquema"), base.anticonceptivo.esquema),
                frecuencia = enumDe(ac.optString("frecuencia"), base.anticonceptivo.frecuencia),
                vence = fecha(ac.optString("vence")),
            ),
            avisos = Avisos(
                periodo = av.optBoolean("periodo", base.avisos.periodo),
                fertil = av.optBoolean("fertil", base.avisos.fertil),
                anticonceptivo = av.optBoolean("anticonceptivo", base.avisos.anticonceptivo),
                registroDiario = av.optBoolean("registroDiario", base.avisos.registroDiario),
                horaRegistro = hora(av.optString("horaRegistro")) ?: base.avisos.horaRegistro,
                discretos = av.optBoolean("discretos", base.avisos.discretos),
            ),
        )
        val dias = mutableMapOf<LocalDate, RegistroDia>()
        val lista = raiz.optJSONArray("dias") ?: JSONArray()
        for (i in 0 until lista.length()) {
            val o = lista.optJSONObject(i) ?: continue
            val f = fecha(o.optString("fecha")) ?: continue
            val registro = RegistroDia(
                fecha = f,
                flujo = enumONull<Flujo>(o.optString("flujo")),
                nota = o.optString("nota", ""),
                sintomas = conjunto<Sintoma>(o.optJSONArray("sintomas")),
                animo = conjunto<Animo>(o.optJSONArray("animo")),
                energia = enumONull<Nivel>(o.optString("energia")),
                sueno = enumONull<Nivel>(o.optString("sueno")),
                deseo = enumONull<Nivel>(o.optString("deseo")),
                relaciones = conjunto<Relacion>(o.optJSONArray("relaciones")),
                pastillaEmergencia = o.optBoolean("pastillaEmergencia", false),
                pastillaTomada = o.optBoolean("pastillaTomada", false),
                temperatura = o.optDouble("temperatura").takeIf { !it.isNaN() && it in 34.0..43.0 },
                moco = enumONull<Moco>(o.optString("moco")),
                testOvulacion = enumONull<Test>(o.optString("testOvulacion")),
                testEmbarazo = enumONull<Test>(o.optString("testEmbarazo")),
            )
            if (!registro.vacio) dias[f] = registro
        }
        return Datos(ajustes, dias)
    }

    private fun textos(l: JSONArray?): List<String> =
        if (l == null) emptyList() else (0 until l.length()).mapNotNull { l.optString(it).takeIf(String::isNotEmpty) }

    private fun fecha(s: String): LocalDate? = try { if (s.isEmpty()) null else LocalDate.parse(s) } catch (e: Exception) { null }
    private fun hora(s: String): LocalTime? = try { if (s.isEmpty()) null else LocalTime.parse(s) } catch (e: Exception) { null }

    // Valores desconocidos (p. ej. de una versión más nueva) se ignoran en vez de romper la importación.
    private inline fun <reified E : Enum<E>> enumONull(s: String): E? = enumValues<E>().firstOrNull { it.name == s }
    private inline fun <reified E : Enum<E>> enumDe(s: String, defecto: E): E = enumONull<E>(s) ?: defecto
    private inline fun <reified E : Enum<E>> conjunto(l: JSONArray?): Set<E> = textos(l).mapNotNull { enumONull<E>(it) }.toSet()
}

class FormatoInvalido(mensaje: String) : Exception(mensaje)
