package com.god.seep.weather.extentions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.god.seep.weather.entity.Entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*


val toast: Toast? = null
/**
 * 扩展函数
 */
fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

/**
 * 扩展属性
 */
var TextView.text: CharSequence
    get() = getText()
    set(value) = setText(value)

/**
 * 操作符重载(扩展函数)
 */
operator fun ViewGroup.get(position: Int): View = getChildAt(position)

inline fun <reified T> String.gson(type: Type): Entity<T> {
    return Gson().fromJson<Entity<T>>(
            this,
            type
    )
}

fun Long.toDate(): String {
    val date = Date(this)
    return SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(date)
}