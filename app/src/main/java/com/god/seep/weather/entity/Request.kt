package com.god.seep.weather.entity

import android.util.Log
import java.net.URL


class Request(val url: String) {
    fun run() {
        val text = URL(url).readText()
        Log.e(javaClass.simpleName, text)
    }
}