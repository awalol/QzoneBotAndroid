package cn.awalol.qzoneBot.bean.qqMusic.songInfo


import com.fasterxml.jackson.annotation.JsonProperty

data class Ksong(
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("mid")
    val mid: String
)