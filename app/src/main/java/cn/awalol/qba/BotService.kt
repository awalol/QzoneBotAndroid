package cn.awalol.qba

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import cn.awalol.qzoneBot.QzoneBot
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
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
        val handler = CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }
        if(account.compareTo(0L) != 0){
            bot = password?.let { BotFactory.newBot(account, it){
                protocol = BotConfiguration.MiraiProtocol.valueOf(AppSettings.protocol)
                fileBasedDeviceInfo()
                workingDir = File(getExternalFilesDir("Mirai").toString())
                redirectBotLogToFile()
                loginSolver = AndroidLoginSolver(baseContext)
            } }!!
            bot.let {
                lifecycleScope.launch(Dispatchers.Default + handler) {
                    it.login()
                    QzoneBot.start(it)
                }
            }
        }
    }

    private fun stopBot(){
        bot.cancel()
    }
}