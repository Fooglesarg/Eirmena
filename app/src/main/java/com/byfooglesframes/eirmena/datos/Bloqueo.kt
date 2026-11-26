package com.byfooglesframes.eirmena.datos

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * PIN de la app. Solo se guarda un resumen (hash) con sal, nunca el PIN.
 * Sin servidor no hay forma de recuperarlo: si se olvida, se borra todo y se importa un respaldo.
 */
class Bloqueo(context: Context) {
    private val prefs = context.getSharedPreferences("bloqueo", Context.MODE_PRIVATE)

    val tienePin: Boolean get() = prefs.contains(HASH)
    var usarHuella: Boolean
        get() = prefs.getBoolean(HUELLA, false)
        set(valor) { prefs.edit().putBoolean(HUELLA, valor).apply() }

    /** Momento (ms) hasta el que hay que esperar por demasiados intentos fallidos. */
    val esperarHasta: Long get() = prefs.getLong(ESPERA, 0)

    fun ponerPin(pin: String) {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val b64 = Base64.getEncoder()
        prefs.edit()
            .putString(SAL, b64.encodeToString(sal))
            .putString(HASH, b64.encodeToString(resumen(pin, sal)))
            .putInt(FALLOS, 0)
            .apply()
    }

    fun quitarPin() {
        prefs.edit().clear().apply()
    }

    fun comprobar(pin: String, ahora: Long = System.currentTimeMillis()): Boolean {
        if (ahora < esperarHasta) return false
        val b64 = Base64.getDecoder()
        val sal = b64.decode(prefs.getString(SAL, null) ?: return false)
        val guardado = b64.decode(prefs.getString(HASH, null) ?: return false)
        val ok = MessageDigest.isEqual(guardado, resumen(pin, sal))
        if (ok) {
            prefs.edit().putInt(FALLOS, 0).apply()
        } else {
            val fallos = prefs.getInt(FALLOS, 0) + 1
            val editor = prefs.edit().putInt(FALLOS, fallos)
            // A partir del 5.º error, cada error obliga a esperar 30 s.
            if (fallos >= 5) editor.putLong(ESPERA, ahora + 30_000)
            editor.apply()
        }
        return ok
    }

    private fun resumen(pin: String, sal: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), sal, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            .also { spec.clearPassword() }
    }

    private companion object {
        const val HASH = "hash"
        const val SAL = "sal"
        const val HUELLA = "huella"
        const val FALLOS = "fallos"
        const val ESPERA = "espera"
    }
}
