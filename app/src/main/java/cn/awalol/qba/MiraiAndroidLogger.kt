package cn.awalol.qba

import android.util.Log
import cn.awalol.qba.ui.MainActivity
import net.mamoe.mirai.utils.MiraiLoggerPlatformBase

class MiraiAndroidLogger(override val identity: String?) : MiraiLoggerPlatformBase(){
    override fun debug0(message: String?, e: Throwable?) {
        if(message != null){
            Log.d("Mirai",message)
        }
    }

    override fun error0(message: String?, e: Throwable?) {
        if(message != null){
            Log.e("Mirai",message)
            MainActivity.log.complete("E: $message")
        }
    }

    override fun info0(message: String?, e: Throwable?) {
        if(message != null){
            Log.i("Mirai",message)
            MainActivity.log.complete("I: $message")
        }
    }

    override fun verbose0(message: String?, e: Throwable?) {
        if(message != null){
            Log.v("Mirai",message)
        }
    }

    override fun warning0(message: String?, e: Throwable?) {
        if(message != null){
            Log.w("Mirai",message)
        }
    }
}