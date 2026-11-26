package com.byfooglesframes.eirmena.avisos

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.byfooglesframes.eirmena.MainActivity
import com.byfooglesframes.eirmena.R
import com.byfooglesframes.eirmena.datos.Datos
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Programa un solo despertador: el del próximo aviso. Cuando suena, [ReceptorAvisos] muestra lo que
 * corresponda y vuelve a llamar acá. Todo es local: no hay servidor de notificaciones.
 */
object Planificador {
    private const val CANAL = "recordatorios"
    private const val PREFS = "avisos"
    private const val ULTIMO = "ultimo"
    /** Si el teléfono estuvo apagado, se muestran avisos atrasados de hasta 6 horas. */
    private const val ATRASO_MAXIMO_MIN = 6 * 60L

    /** Programa el próximo aviso posterior a [desde]. Lo anterior a [desde] ya no se muestra. */
    fun programar(context: Context, datos: Datos, desde: LocalDateTime = LocalDateTime.now()) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(ULTIMO, desde.toString()).apply()

        val alarmas = context.getSystemService(AlarmManager::class.java)
        val intento = pendiente(context)
        alarmas.cancel(intento)
        val proximo = Avisos.eventos(datos, desde, desde.plusDays(40)).firstOrNull() ?: return
        val ms = proximo.momento.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT < 31 || alarmas.canScheduleExactAlarms()) {
            alarmas.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, intento)
        } else {
            // Sin permiso de alarmas exactas, Android puede demorarla unos minutos.
            alarmas.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, ms, intento)
        }
    }

    /** Muestra los avisos que vencieron desde la última vez y programa el siguiente. */
    fun sonar(context: Context, datos: Datos) {
        val ahora = LocalDateTime.now()
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val minimo = ahora.minusMinutes(ATRASO_MAXIMO_MIN)
        val ultimo = prefs.getString(ULTIMO, null)?.let(LocalDateTime::parse)?.takeIf { it > minimo } ?: minimo
        // Un poco de margen por si el despertador se adelanta unos segundos.
        val hasta = ahora.plusSeconds(30)
        val vencidos = Avisos.eventos(datos, ultimo, hasta)
        // Si hay varios del mismo tipo (p. ej. teléfono apagado), se muestra solo el último.
        vencidos.groupBy { it.tipo }.values.map { it.last() }.forEach { mostrar(context, datos, it) }
        programar(context, datos, hasta)
    }

    /** Muestra un aviso de prueba ahora mismo, para ver cómo se ve. */
    fun probar(context: Context, datos: Datos) = mostrar(
        context, datos,
        Evento(LocalDateTime.now(), TipoAviso.REGISTRO, "Así se ven los avisos de Eirmena", "Este es un aviso de prueba."),
    )

    fun puedeNotificar(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun puedeAlarmasExactas(context: Context): Boolean =
        Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun quitarNotificacion(context: Context, tipo: TipoAviso) =
        NotificationManagerCompat.from(context).cancel(tipo.ordinal + 1)

    private fun mostrar(context: Context, datos: Datos, e: Evento) {
        if (!puedeNotificar(context)) return
        crearCanal(context)
        val discretos = datos.ajustes.avisos.discretos
        val abrir = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        // Versión que se ve con el teléfono bloqueado: nunca dice nada del ciclo.
        val publica = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Eirmena")
            .setContentText("Tenés un recordatorio.")
            .build()
        val b = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setColor(0xFFC8694F.toInt())
            .setContentTitle(if (discretos) "Eirmena" else e.titulo)
            .setContentText(if (discretos) "Tenés un recordatorio." else e.texto)
            .setContentIntent(abrir)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publica)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        if (e.tipo == TipoAviso.PASTILLA) {
            val tomada = PendingIntent.getBroadcast(
                context, 1, Intent(context, ReceptorAvisos::class.java).setAction(ReceptorAvisos.TOMADA),
                PendingIntent.FLAG_IMMUTABLE,
            )
            b.addAction(0, if (discretos) "Listo" else "Ya la tomé", tomada)
        }
        try {
            NotificationManagerCompat.from(context).notify(e.tipo.ordinal + 1, b.build())
        } catch (_: SecurityException) {
        }
    }

    private fun crearCanal(context: Context) {
        val canal = NotificationChannel(CANAL, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Período, anticonceptivo y registro diario"
            lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    private fun pendiente(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, ReceptorAvisos::class.java).setAction(ReceptorAvisos.SONAR),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
