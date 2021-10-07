package cn.awalol.qba

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import cn.awalol.qba.ui.MainActivity
import cn.awalol.qzoneBot.QzoneBot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.PlatformLogger
import splitties.systemservices.notificationManager
import java.io.File

class BotService : LifecycleService() {
    companion object{
        lateinit var bot : Bot
        const val START_SERVICE = 0
        const val STOP_SERVICE = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getIntExtra("action", START_SERVICE).let {
            when(it){
                START_SERVICE -> startBot(intent)
                STOP_SERVICE -> stopBot()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startBot(intent : Intent?){
        val account = intent!!.getLongExtra("account",0L)
        val password = intent.getStringExtra("password")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val handler = CoroutineExceptionHandler { _, throwable ->
            notificationManager.cancel(1)
            throwable.printStackTrace()
        }
        if(account.compareTo(0L) != 0 && !password.isNullOrEmpty()){
            bot = BotFactory.newBot(account, password){
                protocol = BotConfiguration.MiraiProtocol.valueOf(AppSettings.protocol)
                fileBasedDeviceInfo()
                workingDir = File(getExternalFilesDir("Mirai").toString())
//                redirectBotLogToFile()
                loginSolver = AndroidLoginSolver(baseContext)
            }
            lifecycleScope.launch(Dispatchers.Default + handler) {
                bot.login()
                notificationManager.notify(1,
                    NotificationCompat.Builder(NotificationFactory.context,"cn.awalol.qba")
                        .setAutoCancel(false)
                        //禁止滑动删除
                        .setOngoing(true)
                        //右上角的时间显示
                        .setShowWhen(true)
                        .setContentTitle("QzoneBotAndroid")
                        .setContentText("机器人已登录")
                        .setSmallIcon(R.drawable.ic_baseline_feedback_24)
                        .build()
                )
                QzoneBot.start(bot)
            }
        }
    }

    private fun stopBot(){
        bot.cancel()
    }
}