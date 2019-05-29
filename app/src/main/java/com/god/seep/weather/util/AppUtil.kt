package com.god.seep.weather.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

fun isServiceWorking(context: Context, serviceName: String): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningServices = am.getRunningServices(10)
    for (info in runningServices) {
        if (info.service.className == serviceName)
            return true
    }
    return false
}

fun isActivityForeground(context: Context, activityName: String): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningTasks = am.getRunningTasks(10)
    val componentName = runningTasks[0].topActivity
    if (activityName == componentName.className)
        return true
    return false
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun moveTaskToFront(context: Context) {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = am.appTasks
    tasks[0].moveToFront()
}