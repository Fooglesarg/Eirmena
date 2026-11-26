package com.byfooglesframes.eirmena

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.byfooglesframes.eirmena.ui.App
import com.byfooglesframes.eirmena.ui.TemaEirmena

class MainActivity : FragmentActivity() {
    private val vm: EirmenaViewModel by viewModels()
    private var salioA = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Al volver a la app después de más de 30 s afuera, se pide el PIN otra vez.
        // (El margen evita que se bloquee al elegir dónde guardar un respaldo.)
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { salioA = SystemClock.elapsedRealtime() }
            override fun onStart(owner: LifecycleOwner) {
                if (salioA != 0L && SystemClock.elapsedRealtime() - salioA > 30_000) vm.bloquear()
                if (salioA != 0L) vm.recargar()
            }
        })

        setContent {
            val datos by vm.datos.collectAsState()
            // Ocultar el contenido en "apps recientes" y en capturas, si la persona lo eligió.
            LaunchedEffect(datos.ajustes.ocultarEnRecientes) {
                if (datos.ajustes.ocultarEnRecientes) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
            TemaEirmena {
                App(vm, huellaDisponible = huellaDisponible(), pedirHuella = ::pedirHuella)
            }
        }
    }

    private fun huellaDisponible(): Boolean =
        BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS

    private fun pedirHuella(alConfirmar: () -> Unit) {
        if (!huellaDisponible()) return
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = alConfirmar()
            })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Abrir Eirmena")
                .setSubtitle("Usá tu huella o tu rostro")
                .setNegativeButtonText("Usar PIN")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()
        )
    }
}
