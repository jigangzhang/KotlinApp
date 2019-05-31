package com.god.seep.weather.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
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

fun exit(context: Context) {
//    Process.killProcess(Process.myPid())//杀掉当前进程， 未触发 shutdownHook， 即未停掉虚拟机？ remote进程还在
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//    am.killBackgroundProcesses(context.packageName)//杀掉当前进程， 未触发 shutdownHook， 若触发 binderDied remote 进程被杀掉，未触发则存在
    for (process in am.runningAppProcesses) {
        Log.e("tag", "process -- pid ${process.pid} -- ${process.processName} -- uid ${process.uid}")
        Process.killProcess(process.pid)//杀掉所有进程， 未触发 shutdownHook， 注意：要在杀掉其他进程后在杀掉当前进程（暂时观察到当前进程在列表最后）
    }
    //先杀掉进程时， 杀掉虚拟机操作不执行，所以二者取其一
//    System.exit(0)//杀掉虚拟机， 触发 shutdownHook， 先杀掉虚拟机时不会执行 后续操作，应放到最后
}