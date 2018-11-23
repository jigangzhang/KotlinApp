package com.god.seep.weather.entity

import android.util.Log
import java.net.URL


class Request(var city: String) {
    companion object {
        const val userId: String = "U0F03BA775"
        const val apiKey: String = "kqd4ggbuvcusbwge"
        const val url: String = "https://api.seniverse.com/v3/weather/now.json?"
    }

    fun run() {
        var params: String = ""
        val text = URL("$url$params&uid=$userId&sig=$apiKey&q=$city").readText()
        Log.e(javaClass.simpleName, text)
    }
}