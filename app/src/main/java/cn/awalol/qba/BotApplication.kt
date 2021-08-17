package cn.awalol.qba

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import cn.awalol.qba.ui.CaptchaActivity
import splitties.systemservices.notificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.spongycastle.jce.provider.BouncyCastleProvider
import splitties.init.injectAsAppCtx
import java.security.Security


class BotApplication : Application() {

    companion object{
        lateinit var context: BotApplication
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        val mChannel = NotificationChannel("cn.awalol.qba", "Captcha", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "用于登陆验证"
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        injectAsAppCtx()
        byPassECDHCHeck()
        Security.addProvider(BouncyCastleProvider())
    }

    //thanks MiraiAndroid
    /**
     * 在新版系统上无法使用ECDH算法，使用下面的代码绕过
     * */
    private fun byPassECDHCHeck() {
        try {
            val cls = Class.forName("sun.security.jca.Providers")
            val field = cls.getDeclaredField("maximumAllowableApiLevelForBcDeprecation")
            field.isAccessible = true
            field.setInt(null, 999)
        }
        catch(e : Exception) {}
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