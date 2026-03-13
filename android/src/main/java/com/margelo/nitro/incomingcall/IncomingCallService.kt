package com.margelo.nitro.incomingcall

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service that keeps the incoming call alive when the app is in the
 * background or in the killed state. It immediately starts [IncomingCallActivity]
 * so the full-screen UI appears above the lock screen.
 */
class IncomingCallService : Service() {

    companion object {
        private const val CHANNEL_ID = "incoming_call_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uuid = intent?.getStringExtra("uuid") ?: ""
        val callerName = intent?.getStringExtra("callerName") ?: "Unknown"
        val avatar = intent?.getStringExtra("avatar")
        val backgroundColor = intent?.getStringExtra("backgroundColor")
        val callType = intent?.getStringExtra("callType") ?: "audio"
        val timeout = intent?.getLongExtra("timeout", 30000L) ?: 30000L

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(callerName, uuid))

        // Launch full-screen incoming call activity
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("uuid", uuid)
            putExtra("callerName", callerName)
            putExtra("avatar", avatar)
            putExtra("backgroundColor", backgroundColor)
            putExtra("callType", callType)
            putExtra("timeout", timeout)
        }
        startActivity(activityIntent)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Notification helpers
    // ──────────────────────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Displays incoming call alerts"
                setSound(null, null)
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(callerName: String, uuid: String): Notification {
        // Full-screen intent so the activity shows on the lock screen
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("uuid", uuid)
            putExtra("callerName", callerName)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Incoming Call")
            .setContentText(callerName)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }
}
