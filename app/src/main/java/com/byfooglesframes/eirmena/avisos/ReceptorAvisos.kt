package com.byfooglesframes.eirmena.avisos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.byfooglesframes.eirmena.datos.Almacen
import java.time.LocalDate

/** Despertador de avisos, reinicio del teléfono, cambio de hora y el botón "Ya la tomé". */
class ReceptorAvisos : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pendiente = goAsync()
        Thread {
            try {
                val almacen = Almacen(app)
                val datos = almacen.cargar()
                when (intent.action) {
                    // Al prender el teléfono también se muestran los avisos que se perdieron mientras estaba apagado.
                    SONAR, Intent.ACTION_BOOT_COMPLETED -> Planificador.sonar(app, datos)
                    TOMADA -> {
                        val hoy = LocalDate.now()
                        val nuevos = datos.conDia(datos.dia(hoy).copy(pastillaTomada = true))
                        almacen.guardar(nuevos)
                        Almacen.avisarCambio()
                        Planificador.quitarNotificacion(app, TipoAviso.PASTILLA)
                        Planificador.programar(app, nuevos)
                    }
                    else -> Planificador.programar(app, datos) // cambio de hora o actualización de la app
                }
            } catch (_: Exception) {
                // Sin datos legibles no hay nada que avisar.
            } finally {
                pendiente.finish()
            }
        }.start()
    }

    companion object {
        const val SONAR = "com.byfooglesframes.eirmena.SONAR"
        const val TOMADA = "com.byfooglesframes.eirmena.TOMADA"
    }
}
