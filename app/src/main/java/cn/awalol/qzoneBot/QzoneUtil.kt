package cn.awalol.qzoneBot

import cn.awalol.qzoneBot.QzoneBot.client
import cn.awalol.qzoneBot.QzoneBot.objectMapper
import cn.awalol.qzoneBot.QzoneBot.qzoneCookie
import cn.awalol.qzoneBot.bean.qzoneSuosuo.PicInfo
import cn.awalol.qzoneBot.bean.qzoneSuosuo.PicinfoX
import cn.awalol.qzoneBot.bean.qzoneSuosuo.UploadPic
import io.ktor.client.request.*
import io.ktor.http.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.HashMap

const val clickScript = "var faces = document.getElementsByClassName(\"face\");\n" +
        "for(i = 0;i < faces.length;i++){\n" +
        "    if(faces[i].getAttribute(\"uin\") == \"%s\"){\n" +
        "        pt.qlogin.imgClick(faces[i])\n" +
        "    }\n" +
        "}"

object QzoneUtil {
    private fun getGtk(sKey: String): Long {
        var hash: Long = 5381
        for (element in sKey) {
            hash += (hash shl 5) + element.code.toLong()
        }
        return hash and 0x7fffffff
    }

    suspend fun publishShuoshuo(content : String, image : String) : String{
        //get image ByteArray
        val imageResponse : ByteArray = client.get(image)
        val imageBase64: String = Base64.getUrlEncoder().encodeToString(imageResponse)
        //upload Image to Qzone
        val uploadPic1Response : String = client.post{
            url("https://mobile.qzone.qq.com/up/cgi-bin/upload/cgi_upload_pic_v2?g_tk=" + getGtk(qzoneCookie.getValue("p_skey")))
            headers{
                append(HttpHeaders.Cookie,
                    "p_uin=${qzoneCookie.getValue("p_uin")}; " +
                        "p_skey=${qzoneCookie.getValue("p_skey")};"
                )
                append(HttpHeaders.ContentType,"application/x-www-form-urlencoded")
                append(HttpHeaders.UserAgent,"Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Mobile Safari/537.36")
            }
            body = "picture=$imageBase64&output_type=json&preupload=1&base64=1&hd_quality=90"
        }
        println(getStringMiddleContent(uploadPic1Response,"_Callback(",");"))
        val uploadPic : UploadPic = objectMapper.readValue(
            getStringMiddleContent(uploadPic1Response,"_Callback(",");"),
            UploadPic::class.java) //JSON反序列化

        Thread.sleep(1000)

        //upload Image to ablum
        val uploadPic2Response : String = client.post{
            url("https://mobile.qzone.qq.com/up/cgi-bin/upload/cgi_upload_pic_v2?g_tk=" + getGtk(qzoneCookie.getValue("p_skey")))
            headers{
                append(HttpHeaders.Cookie,
                    "p_uin=${qzoneCookie.getValue("p_uin")}; " +
                        "p_skey=${qzoneCookie.getValue("p_skey")};"
                )
                append(HttpHeaders.ContentType,"application/x-www-form-urlencoded")
                append(HttpHeaders.UserAgent,"Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Mobile Safari/537.36")
            }
            body = "output_type=json&preupload=2&md5=${uploadPic.filemd5}&filelen=${uploadPic.filelen}&refer=shuoshuo&albumtype=7"
        }
        val imageContent = getStringMiddleContent(uploadPic2Response,"_Callback([","]);")
        println(imageContent)
        val picInfo : PicinfoX = objectMapper.readValue(imageContent, PicInfo::class.java).picinfo

        //publish Shuoshuo
        val publishResponse : String = client.post{
            url("https://mobile.qzone.qq.com/mood/publish_mood?g_tk=" + getGtk(qzoneCookie.getValue("p_skey")))
            headers {
                append(
                    HttpHeaders.Cookie,
                    "p_uin=${qzoneCookie.getValue("p_uin")};" +
                            "p_skey=${qzoneCookie.getValue("p_skey")};"
                )
                append(HttpHeaders.UserAgent,"Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Mobile Safari/537.36")
            }
            body = "opr_type=publish_shuoshuo&content=${URLEncoder.encode(content,"UTF-8")}&format=json&richval=" + (picInfo.albumid + "," + picInfo.sloc + "," + picInfo.lloc + ",," + picInfo.height + "," + picInfo.width + ",,,")
        }
        val publishMap = objectMapper.readValue(publishResponse,Map::class.java) as Map<*, *>

        if(publishMap["code"].toString().contentEquals("0").not()){
            throw Exception("发送失败 -> $publishResponse")
        }

        return publishResponse
    }

    suspend fun publishShuoshuo(content: String) : String{
        return client.post{
            url("https://mobile.qzone.qq.com/mood/publish_mood?g_tk=" + getGtk(qzoneCookie.getValue("p_skey")))
            headers {
                append(
                    "cookie",
                    "p_uin=" + qzoneCookie.getValue("p_uin") + ";p_skey=" + qzoneCookie.getValue("p_skey") + ";"
                )
            }
            body = "opr_type=publish_shuoshuo&content=${URLEncoder.encode(content,"UTF-8")}&format=json"
        }
    }

    fun getStringMiddleContent (string: String,startString: String,endString: String) : String{
        val startIndex = string.indexOf(startString)
        val endIndex = string.indexOf(endString,startIndex)
        return string.substring(startIndex + startString.length,endIndex)
    }

    suspend fun cookieIsValid(cookie : HashMap<String,String>) : Boolean{
        //通过获取qq好友列表以判断Cookie是否存活
        val stringResponse : String = client.get("https://mobile.qzone.qq.com/friend/mfriend_list?g_tk=${getGtk(cookie.getValue("p_skey"))}&res_type=normal&format=json"){
            headers{
                append(HttpHeaders.UserAgent,"Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Mobile Safari/537.36")
                append(HttpHeaders.Cookie,
                    "skey=${cookie.getValue("skey")}; " +
                            "p_uin=${cookie.getValue("p_uin")};" +
                            "pt4_token=${cookie.getValue("pt4_token")}; " +
                            "p_skey=${cookie.getValue("p_skey")};"
                )
            }
        }
        return stringResponse.contains("\"code\":0")//懒得写JSON反序列化了，直接判断文本是否存在吧，如果出问题再改
    }
}