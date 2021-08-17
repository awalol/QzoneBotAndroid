package cn.awalol.qba.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import cn.awalol.qba.AndroidLoginSolver
import cn.awalol.qba.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CaptchaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_captcha)


        val data : ByteArray = Base64.getDecoder().decode(intent.getStringExtra("data"))
        lifecycleScope.launch(Dispatchers.Main) {
            val bitmap = BitmapFactory.decodeByteArray(data,0,data.size)
            findViewById<ImageView>(R.id.captcha_imageView).setImageBitmap(bitmap)
        }
    }

    override fun onStart() {
        super.onStart()
        findViewById<Button>(R.id.captcha_btn).setOnClickListener {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(2)

            AndroidLoginSolver.verificationResult.complete(findViewById<EditText>(R.id.captcha_input).text.toString())
            finish()
        }
    }
}