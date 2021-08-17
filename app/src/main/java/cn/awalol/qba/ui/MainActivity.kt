package cn.awalol.qba.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import cn.awalol.qba.BotApplication
import cn.awalol.qba.BotService
import cn.awalol.qba.MiraiAndroidLogger
import cn.awalol.qba.R
import cn.awalol.qzoneBot.QzoneBot
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.MiraiLogger

class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var log : CompletableDeferred<String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.Main) {
            while (true){
                log = CompletableDeferred()
                findViewById<TextView>(R.id.log_text).append(log.await() + "\n")
            }
        }
    }

    fun startOnClick(view : View){
        if(QzoneBot.qzoneCookie.isNullOrEmpty()){
            Snackbar.make(view,"QQ空间未登陆",Snackbar.LENGTH_LONG)
                .setAction("登陆") { loginQzoneOnClick(view) }
                .show()
            return
        }
        (application as BotApplication).startBotService()
        MiraiLogger.setDefaultLoggerCreator {
            MiraiAndroidLogger(it)
        }
    }

    fun settingOnClick(view : View){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun loginQzoneOnClick(view : View){
        val intent = Intent(this,WebViewActivity::class.java).apply {
            data = ("https://ui.ptlogin2.qq.com/cgi-bin/login?pt_hide_ad=1&style=9&appid=549000929&pt_no_auth=1&pt_wxtest=1&daid=5&s_url=https%3A%2F%2Fh5.qzone.qq.com%2Fmqzone%2Findex".toUri())
        }
        startActivityForResult(intent,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == 0 && !QzoneBot.qzoneCookie.isNullOrEmpty()){
            Snackbar.make(findViewById<CoordinatorLayout>(R.id.main_activity),"QQ空间登陆成功",Snackbar.LENGTH_SHORT).show()
        }
    }

}