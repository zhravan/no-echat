package com.zhravan.noechat.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zhravan.noechat.MainActivity
import com.zhravan.noechat.R
import com.zhravan.noechat.domain.EmergencyPacket
import com.zhravan.noechat.ui.copy.UserCopy

class IncomingAlertNotifier(
    private val context: Context
) {
    fun notify(packet: EmergencyPacket) {
        if (!canPostNotifications()) return

        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.incoming_alert_notification_title))
            .setContentText(buildBody(packet))
            .setStyle(NotificationCompat.BigTextStyle().bigText(buildBody(packet)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setContentIntent(createContentIntent(packet))
            .setSound(ALARM_URI)
            .build()

        NotificationManagerCompat.from(context).notify(packet.publicId.hashCode(), notification)
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.incoming_alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.incoming_alert_channel_description)
            enableVibration(true)
            setSound(
                ALARM_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildBody(packet: EmergencyPacket): String {
        val status = UserCopy.emergencyStatus(packet.status)
        val note = packet.note?.trim().orEmpty()
        return if (note.isEmpty()) status else "$status\n$note"
    }

    private fun createContentIntent(packet: EmergencyPacket): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PUBLIC_ID, packet.publicId)
        }
        return PendingIntent.getActivity(
            context,
            packet.publicId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_PUBLIC_ID = "incoming_public_id"

        private const val CHANNEL_ID = "incoming_alerts"
        private val ALARM_URI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }
}
