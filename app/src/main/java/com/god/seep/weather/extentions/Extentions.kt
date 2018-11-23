package com.god.seep.weather.extentions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast


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
public var TextView.text: CharSequence
    get() = getText()
    set(value) = setText(value)

/**
 * 操作符重载(扩展函数)
 */
operator fun ViewGroup.get(position: Int): View = getChildAt(position)