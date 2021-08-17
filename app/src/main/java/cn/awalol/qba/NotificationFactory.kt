package cn.awalol.qba

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import cn.awalol.qba.ui.CaptchaActivity
import cn.awalol.qba.ui.MainActivity

//https://github.com/mzdluo123/MiraiAndroid/blob/master/app/src/main/java/io/github/mzdluo123/mirai/android/NotificationFactory.kt
object NotificationFactory {

    val context by lazy {
        BotApplication.context
    }

    fun captchaNotification(callback: (context : Context) -> Intent) : Notification{
        val intent = TaskStackBuilder.create(BotApplication.context)
            .addParentStack(MainActivity::class.java)
            .addNextIntent(callback(context))
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context,"cn.awalol.qba")
            .setContentIntent(intent)
            .setAutoCancel(false)
            //禁止滑动删除
            .setOngoing(false)
            //右上角的时间显示
            .setShowWhen(true)
            .setContentTitle("本次登录需要进行登录验证")
            .setContentText("点击这里开始验证")
            .setSmallIcon(R.drawable.ic_baseline_feedback_24)
            .build()
    }
}