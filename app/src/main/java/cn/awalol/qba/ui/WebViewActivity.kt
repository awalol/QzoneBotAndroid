package cn.awalol.qba.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import cn.awalol.qba.AndroidLoginSolver
import cn.awalol.qba.R
import cn.awalol.qzoneBot.QzoneBot
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import kotlinx.serialization.json.Json

class WebViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView(){
        val webView = findViewById<WebView>(R.id.webView)
        WebView.setWebContentsDebuggingEnabled(true)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            if(intent.getBooleanExtra("captcha",false)){
                userAgentString = "Mozilla/5.0 (Linux; Android 5.1; OPPO R9tm Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043128 Safari/537.36 V1_AND_SQ_7.0.0_676_YYB_D PA QQ/7.0.0.3135 NetType/4G WebP/0.3.0 Pixel/1080  Edg/92.0.4515.131"
            }
        }


        webView.webViewClient = object : WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request != null) {
//                    Log.d("WebView|Url",view!!.url.toString())
//                    Log.d("WebView|Path", request.url!!.path!!)
                    //jsBridge
                    if(request.url!!.scheme.equals("jsbridge")){
                        //滑块验证 https://github.com/mzdluo123/TxCaptchaHelper/blob/master/app/src/main/java/io/github/mzdluo123/txcaptchahelper/CaptchaActivity.kt
                        if(request.url.path.equals("/onVerifyCAPTCHA")){
                            val p = request.url.getQueryParameter("p")
                            val objectMapper = ObjectMapper()
                            val ticket = objectMapper.readValue(p,JsonNode::class.java)["ticket"].asText()
                            Log.d("LoginSolver|SliderTicket",ticket)
                            AndroidLoginSolver.verificationResult.complete(ticket)
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.cancel(2)
                            finish()
                            return false
                        }

                        //openUrl
                        if(request.url.path.equals("/openUrl")){
                            val p = request.url.getQueryParameter("p")
                            val objectMapper = ObjectMapper()
                            val url = objectMapper.readValue(p,JsonNode::class.java)["url"].asText()
                            view!!.loadUrl(url)
                            return false
                        }

                        //closeWebView
                        if(request.url.path.equals("/closeWebViews")){
                            if(intent.getBooleanExtra("captcha",false)){
                                AndroidLoginSolver.verificationResult.complete("")
                                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                notificationManager.cancel(2)
                            }
                            finish()
                            return false
                        }
                    }

                    //判断是否跳转到QQ (QQ快捷登陆)
                    if(request.url!!.scheme.equals("wtloginmqq")){
                        val intent = Intent(Intent.ACTION_VIEW,request.url)
                        startActivity(intent)
                        return true
                    }
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if(url!!.startsWith("https://h5.qzone.qq.com")){
                    val cookie = CookieManager.getInstance().getCookie(url)
                    cookie.split("; ").forEach {
                        val var1 = it.split("=") //分割Cookie的Name和Value
                        QzoneBot.qzoneCookie[var1[0]] = var1[1] //将Cookie添加到变量
                    }
                    setResult(0)
                    finish()
                }
            }
        }

        webView.loadUrl(intent.dataString.toString())
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        findViewById<WebView>(R.id.webView).loadUrl(intent!!.dataString.toString())
    }

    //左上角返回键
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finish()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}