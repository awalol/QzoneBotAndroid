package cn.awalol.qzoneBot

import android.util.Log
import cn.awalol.qzoneBot.bean.qqMusic.songInfo.SongInfo
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.PlainText

object QzoneBot{
    val qzoneCookie : HashMap<String, String> = HashMap()
    val objectMapper = ObjectMapper()
    val template = "#推歌意向征集#\n" +
            "《%s》（%s）"
    val singerBlackList = listOf("0011jjK40orUJx","002nXp292LIOGV","0022eAG537I1bg","0039JTTG0s4SCv")
    val client = HttpClient(CIO)
    val waitToRepublish : HashMap<String,Image?> = HashMap()

    fun start(bot: Bot){
        bot.globalEventChannel().filter { event: Event -> event is FriendMessageEvent || event is StrangerMessageEvent }.subscribeAlways<MessageEvent> {
            var image : Image? = null
            var content = ""
            message.forEach { singleMessage ->
                if(singleMessage is Image){
                    image = Image(singleMessage.imageId)
                    bot.logger.info(singleMessage.imageId)
                }else if(singleMessage is PlainText){
                    content = singleMessage.content
                    bot.logger.info("PlainText " + singleMessage.content)
                }
            }

            if(qzoneCookie.isNotEmpty() && content.isNotEmpty()) {
                if("((?<=《).+(?=》))".toRegex().containsMatchIn(content) && "(?<=[（|(]).+(?=[）|)])".toRegex().containsMatchIn(content)){
                    //获取歌曲信息
                    val songName = "((?<=《).+(?=》))".toRegex().findAll(content).first().value
                    val songSinger = "(?<=[（|(]).+(?=[）|)])".toRegex().findAll(content).last().value
                    bot.logger.info("$songName $songSinger")
                    val songInfo = MusicApi.qqMusicSongInfo(
                        MusicApi.qqMusicSearch(songName, songSinger)!!.songmid!!
                    )

                    try {
                        if (QzoneUtil.cookieIsValid(qzoneCookie)) {
                            push(songInfo,image,bot)

                            //重试发送未发送成功的说说
                            if(waitToRepublish.isNotEmpty()){
                                println("尝试重新发送之前发送失败的说说")
                                val iterator = waitToRepublish.iterator() //https://stackoverflow.com/questions/14673653/why-isnt-this-code-causing-a-concurrentmodificationexception
                                while (iterator.hasNext()){
                                    val item = iterator.next()
                                    push(MusicApi.qqMusicSongInfo(item.key),item.value,bot)
                                    iterator.remove()
                                }
                            }
                        }else{
                            throw Exception("QQ空间登陆失败")
                        }
                    }catch (e : Exception){
                        e.printStackTrace()
                        if(e.message?.contentEquals("QQ空间登陆失败") == true){
                            //TODO: 待修复
                        }
                        if(!waitToRepublish.containsKey(songInfo.data[0].mid)){
                            waitToRepublish[songInfo.data[0].mid] = image
                            bot.logger.error("发送失败，已添加到重试列表")
                        }
                    }
                }
            }else if(qzoneCookie.isEmpty()){
                bot.logger.error("QQ空间未登录")
                bot.getFriend(3512311532)!!.sendMessage("[Error] 说说发送失败，QQ空间未登录")
            }
        }
    }

    suspend fun push(songInfo : SongInfo, image : Image?, bot : Bot) {
        val songData = songInfo.data[0]

        //黑名单歌手判断
        songData.singer.forEach { singer ->
            if (singerBlackList.contains(singer.mid)) {
                bot.logger.error("黑名单歌手:" + singer.title)
                return
            }
        }

        if (image != null) {
            QzoneUtil.publishShuoshuo(
                template.format(
                    songData.title,
                    songData.singer.joinToString(separator = "/"){it.name}
                ),
                image.queryUrl()
            )
        } else {
            QzoneUtil.publishShuoshuo(
                template.format(
                    songData.title,
                    songData.singer.joinToString(separator = "/"){it.name}
                ),
                "http://y.gtimg.cn/music/photo_new/T002R800x800M000%s.jpg".format(songData.album.mid)
            )
        }
    }
}