package cn.awalol.qba

import android.content.Context
import cn.awalol.qba.ui.CaptchaActivity
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import java.util.*
import android.app.NotificationManager

import android.app.NotificationChannel
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import cn.awalol.qba.ui.WebViewActivity
import splitties.systemservices.notificationManager


class AndroidLoginSolver (val context: Context): LoginSolver(){
    companion object{
        lateinit var verificationResult : CompletableDeferred<String>
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String {
        Log.d("Test","pic")
        verificationResult = CompletableDeferred()
        val base64Data = Base64.getEncoder().encodeToString(data)

        NotificationManagerCompat.from(context).notify(2,NotificationFactory.captchaNotification {
            Intent(it, CaptchaActivity::class.java).apply {
                putExtra("data",base64Data)
            }
        })
        return verificationResult.await()
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        Log.d("Test|Slider",url)
        verificationResult = CompletableDeferred()

        notificationManager.notify(2,NotificationFactory.captchaNotification{
            Intent(it,WebViewActivity::class.java).apply {
                data = url.toUri()
                putExtra("captcha",true)
            }
        })

        return verificationResult.await()
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        Log.d("Test|Unsafe",url)
        verificationResult = CompletableDeferred()

        notificationManager.notify(2,NotificationFactory.captchaNotification{
            Intent(it,WebViewActivity::class.java).apply {
                data = url.toUri()
                putExtra("captcha",true)
            }
        })

        return verificationResult.await()
    }

    override val isSliderCaptchaSupported: Boolean
        get() = true
}