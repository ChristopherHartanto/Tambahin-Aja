package com.ta.tambahinaja

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*
import java.util.concurrent.TimeUnit

class DailyWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    val mContext = ctx

    override fun doWork(): Result {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution around 05:00:00 AM
        dueDate.set(Calendar.HOUR_OF_DAY, 8)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val dailyWorkRequest = OneTimeWorkRequestBuilder<DailyWorker>()
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build()
        WorkManager.getInstance(applicationContext)
                .enqueue(dailyWorkRequest)
        sendNotification()
        return Result.success()
    }

    private fun sendNotification() {
        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "WorkManager_00"
        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "WorkManager", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val bitmap = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.logo_transparent)

        val notification = NotificationCompat.Builder(mContext, channelId)
                .setContentTitle("Good Morning")
                .setContentText("Start Playing Tambahin Aja")
                .setSmallIcon(R.drawable.logo_transparent).setLargeIcon(bitmap)
                .setVibrate(longArrayOf(200,200))

        notificationManager.notify(1, notification.build())
    }
}