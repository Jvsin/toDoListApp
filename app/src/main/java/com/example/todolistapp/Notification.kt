package com.example.todolistapp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.util.Log
import com.example.todolistapp.entities.Todo

const val notificationID = 121
const val channelID = "channel1"
//const val titleExtra = "titleExtra"
//const val messageExtra = "messageExtra"

class Notification : BroadcastReceiver() {

//    override fun onReceive(context: Context, intent: Intent) {
//        Log.v("powiadomienia KL", "onReceive called")
//
//        val todo = intent.getSerializableExtra("current_todo") as? Todo
//        val notificationTime = intent.getSerializableExtra("notification_time") as? Int
//
//        if (todo == null || notificationTime == null) {
//            Log.e("powiadomienia KL", "Received null data: todo or notificationTime is null")
//        }else {
//            Log.v("powiadomienia", "Received todo: ${todo.title}, notificationTime: $notificationTime")
//        }
//
//        Log.v("powiadomienia KL", "Received todo: ${todo?.title}, notificationTime: $notificationTime")
//
//        val activityIntent = Intent(context, AddTodoActivity::class.java).apply {
//            putExtra("current_todo", todo)
//            putExtra("notification_time", notificationTime)
//        }
//
//        val id = 0
//        val pendingIntent = PendingIntent.getActivity(context, id, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = NotificationCompat.Builder(context, channelID)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle(todo?.title)
//            .setContentText(todo?.note)
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        manager.notify(notificationID, notification)
//        Log.v("powiadomienia KL", "Notification sent with ID: ${notificationID}")
//    }

    override fun onReceive(context: Context, intent: Intent) {

        val todo = intent.getSerializableExtra("current_todo") as Todo
        val notificationTime = intent.getSerializableExtra("notification_time") as Int

        val activityIntent = Intent(context, AddTodoActivity::class.java)
        activityIntent.putExtra("current_todo", todo)
        activityIntent.putExtra("notification_time", notificationTime)
        Log.v("powiadomienia", "$todo $notificationTime")
        val id = 0
        val pendingIntent = PendingIntent.getActivity(context, id, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(todo.title)
            .setContentText(todo.note)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(notificationID, notification)
    }
}