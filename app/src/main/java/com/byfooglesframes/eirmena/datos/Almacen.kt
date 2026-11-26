package com.byfooglesframes.eirmena.datos

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Guarda los datos en un único archivo privado de la app, cifrado con una clave del
 * almacén seguro de Android (Keystore). La clave nunca sale del teléfono: aunque alguien
 * copie el archivo, no lo puede leer en otro aparato.
 */
class Almacen(context: Context) {
    private val archivo = File(context.filesDir, "eirmena.dat")
    private val temporal = File(context.filesDir, "eirmena.dat.tmp")

    fun cargar(): Datos {
        if (!archivo.exists()) return Datos()
        val bytes = archivo.readBytes()
        val iv = bytes.copyOfRange(0, 12)
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, iv))
        val json = String(cifrador.doFinal(bytes, 12, bytes.size - 12), Charsets.UTF_8)
        return Json.desdeJson(json)
    }

    fun guardar(datos: Datos) {
        val cifrador = Cipher.getInstance("AES/GCM/NoPadding")
        cifrador.init(Cipher.ENCRYPT_MODE, clave())
        val cifrado = cifrador.doFinal(Json.aJson(datos).toByteArray(Charsets.UTF_8))
        // Se escribe aparte y se reemplaza de una vez, así un corte de batería no deja el archivo a medias.
        temporal.writeBytes(cifrador.iv + cifrado)
        if (!temporal.renameTo(archivo)) {
            archivo.delete()
            temporal.renameTo(archivo)
        }
    }

    fun borrarTodo() {
        archivo.delete()
        temporal.delete()
    }

    private fun clave(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    companion object {
        private const val ALIAS = "eirmena-datos"

        private val _cambios = kotlinx.coroutines.flow.MutableStateFlow(0L)
        /** Sube cada vez que algo fuera de la pantalla (p. ej. una notificación) cambia los datos. */
        val cambios: kotlinx.coroutines.flow.StateFlow<Long> = _cambios

        fun avisarCambio() { _cambios.value = _cambios.value + 1 }
    }
}
