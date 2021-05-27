package com.example.autoupdatetest1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.autoupdatetest1.ScheduledService.Companion.NEW_VERSION_NAME_KEY
import com.example.autoupdatetest1.ScheduledService.Companion.NEW_VERSION_URL_KEY
import kotlin.random.Random


class OneTimeScheduleWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {


    override fun doWork(): Result {
        val url =  inputData.getString(NEW_VERSION_URL_KEY)
        val versionName =  inputData.getString(NEW_VERSION_NAME_KEY)
//        val notificationIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserDownloadUrl))
//
//        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        val intent = Intent(context, UpdatesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra("fromNotification", true)
        intent.putExtra("versionName", versionName)
        intent.putExtra("versionUrl", url)
//        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // both of these approaches now work: FLAG_CANCEL, FLAG_UPDATE; the uniqueInt may be the real solution.
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueInt, showFullQuoteIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            uniqueInt,
            intent,
            //0
            PendingIntent.FLAG_CANCEL_CURRENT
        )


        val id = context.getString(R.string.notification_update_channel_id)
        val title = context.getString(R.string.update_notification_title)
        val content = context.getString(R.string.update_notification_content)

        val builder = NotificationCompat.Builder(context, id)
            .setSmallIcon(R.drawable.ic_baseline_update_24)
            .setContentTitle(title)
            .setContentText(content)
//            .setContentIntent(contentIntent)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        createNotificationChannel()

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(Random.nextInt(), builder.build())
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = context.getString(R.string.notification_update_channel_id)
            val name = context.getString(R.string.notification_update_channel_name)
            val descriptionText = context.getString(R.string.update_notification_content)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}