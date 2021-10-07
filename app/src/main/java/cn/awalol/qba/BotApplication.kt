package cn.awalol.qba

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import splitties.init.injectAsAppCtx


class BotApplication : Application() {

    companion object{
        lateinit var context: BotApplication
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        //createNotificationChannel
        val mChannel = NotificationChannel("cn.awalol.qba", "Captcha", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "用于登陆验证"
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        injectAsAppCtx()
    }

    fun startBotService(){
        val intent = Intent(this,BotService::class.java).apply {
            putExtra("action",BotService.START_SERVICE)
            putExtra("account",AppSettings.account.toLong())
            putExtra("password",AppSettings.password)
        }
        startService(intent)
    }
}