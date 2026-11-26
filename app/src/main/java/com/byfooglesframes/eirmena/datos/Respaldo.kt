package com.byfooglesframes.eirmena.datos

import org.json.JSONObject
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Archivo de respaldo cifrado con contraseña (.eirmena).
 *
 * Es un JSON con la sal, el IV y los datos cifrados con AES-256-GCM. La clave sale de la
 * contraseña con PBKDF2-HMAC-SHA256. Sin la contraseña no se puede leer: si se olvida,
 * el respaldo se pierde (no hay servidor ni "recuperar contraseña").
 */
object Respaldo {
    const val TIPO = "eirmena-respaldo"
    private const val ITERACIONES = 210_000
    private val azar = SecureRandom()

    fun cifrar(json: String, contrasena: CharArray, iteraciones: Int = ITERACIONES): String {
        val sal = ByteArray(16).also { azar.nextBytes(it) }
        val iv = ByteArray(12).also { azar.nextBytes(it) }
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, clave(contrasena, sal, iteraciones), GCMParameterSpec(128, iv))
        cifrador.updateAAD(TIPO.toByteArray())
        val datos = cifrador.doFinal(json.toByteArray(Charsets.UTF_8))
        val b64 = Base64.getEncoder()
        return JSONObject()
            .put("tipo", TIPO)
            .put("version", 1)
            .put("aviso", "Respaldo cifrado de Eirmena. Para abrirlo: Eirmena > Ajustes > Importar respaldo, con la contraseña que elegiste al exportarlo.")
            .put("kdf", "PBKDF2WithHmacSHA256")
            .put("iteraciones", iteraciones)
            .put("cifrado", "AES-256-GCM")
            .put("sal", b64.encodeToString(sal))
            .put("iv", b64.encodeToString(iv))
            .put("datos", b64.encodeToString(datos))
            .toString(2)
    }

    /** Devuelve el JSON original. Lanza [ContrasenaIncorrecta] o [FormatoInvalido]. */
    fun descifrar(archivo: String, contrasena: CharArray): String {
        val o = try { JSONObject(archivo) } catch (e: Exception) { throw FormatoInvalido("El archivo no es un respaldo de Eirmena.") }
        if (o.optString("tipo") != TIPO) throw FormatoInvalido("El archivo no es un respaldo de Eirmena.")
        val b64 = Base64.getDecoder()
        try {
            val sal = b64.decode(o.getString("sal"))
            val iv = b64.decode(o.getString("iv"))
            val datos = b64.decode(o.getString("datos"))
            val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
            cifrador.init(Cipher.DECRYPT_MODE, clave(contrasena, sal, o.getInt("iteraciones")), GCMParameterSpec(128, iv))
            cifrador.updateAAD(TIPO.toByteArray())
            return String(cifrador.doFinal(datos), Charsets.UTF_8)
        } catch (e: AEADBadTagException) {
            throw ContrasenaIncorrecta()
        } catch (e: IllegalArgumentException) {
            throw FormatoInvalido("El respaldo está dañado.")
        } catch (e: org.json.JSONException) {
            throw FormatoInvalido("El respaldo está dañado.")
        }
    }

    /** Un archivo es cifrado si es JSON con tipo "eirmena-respaldo"; si no, se intenta como JSON plano. */
    fun estaCifrado(archivo: String): Boolean =
        try { JSONObject(archivo).optString("tipo") == TIPO } catch (e: Exception) { false }

    private fun clave(contrasena: CharArray, sal: ByteArray, iteraciones: Int): SecretKeySpec {
        val spec = PBEKeySpec(contrasena, sal, iteraciones, 256)
        try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}

class ContrasenaIncorrecta : Exception("La contraseña no es correcta.")
