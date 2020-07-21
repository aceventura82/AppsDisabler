package com.servoz.appsdisabler.tools

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.servoz.appsdisabler.LauncherActivity
import com.servoz.appsdisabler.R

class Notifications{

    fun create(context: Context, title:String, content:String, channel:String="MAIN") {
        val id=101010
        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val intent = Intent(context, LauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        builder.setContentIntent(PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}