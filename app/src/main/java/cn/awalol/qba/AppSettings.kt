package cn.awalol.qba

import splitties.preferences.Preferences

object AppSettings : Preferences("settings"){
    var account by StringPref("account","")
    var password by StringPref("password","")
    var protocol by StringPref("protocol","")
}