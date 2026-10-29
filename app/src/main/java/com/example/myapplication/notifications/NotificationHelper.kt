package com.example.myapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ALERTS = "channel_alerts"
        const val CHANNEL_REMINDERS = "channel_reminders"
        const val CHANNEL_ANNOUNCEMENTS = "channel_announcements"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Data Alerts & Changes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for data changes, sync updates, and message alerts"
            }

            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Room & Call Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled live conversations and language practice"
            }

            val announcementsChannel = NotificationChannel(
                CHANNEL_ANNOUNCEMENTS,
                "Important Announcements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Broadcasting important platform announcements and feature updates"
            }

            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(remindersChannel)
            notificationManager.createNotificationChannel(announcementsChannel)
        }
    }

    fun showDataChangeAlert(title: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify((1000..9999).random(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAnnouncement(title: String, content: String) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ANNOUNCEMENTS)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("📢 $title")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setAutoCancel(true)

            notificationManager.notify((1000..9999).random(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleRoomReminder(roomId: String, title: String = "Upcoming VoxBridge Room") {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏰ Reminder: $title")
                .setContentText("Your live conversation $roomId is starting soon!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify((1000..9999).random(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
