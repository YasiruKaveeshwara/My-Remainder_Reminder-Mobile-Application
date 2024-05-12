package com.example.myremainder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Locale

class RemainderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val id = intent.getIntExtra("id", 0)


        val notificationIntent = Intent(context, UpdateRemainderActivity::class.java).apply {
            putExtra("id", id)
        }

        val pendingIntent = PendingIntent.getActivity(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("RemainderChannel", "Remainder Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "RemainderChannel")
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)

        // Get the shared preferences
        val sharedPreferences = context.getSharedPreferences("RemainderPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get the current count of notifications sent for this remainder
        val currentCount = sharedPreferences.getInt("RemainderCount$id", 0)

        // Increment the count
        editor.putInt("RemainderCount$id", currentCount + 1)
        editor.apply()

        val db = RemainderDbHelper(context)
        val remainder = db.getRemainderById(id)

        if (remainder != null) {
            val repeatCount = remainder.repeat.toInt()

            // If the count equals the repeat count, set the remainder as inactive
            if (currentCount + 1 >= repeatCount) {
                val updatedRemainder = Remainder(id, remainder.title, remainder.content, remainder.time, remainder.date, remainder.meridian, remainder.repeat, "false")
                db.updateRemainder(updatedRemainder)
            }
        }

    }



}