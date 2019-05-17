package com.god.seep.weather.util

import android.app.Service
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.gson
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.google.gson.reflect.TypeToken
import java.io.File

fun handleFileList(connection: NetConnection?, handler: Handler) {
    try {
        connection?.writeCommand(Command.FILE_LIST)
        val data = connection?.readCommand()
        val msg = Message.obtain()
        msg.run {
            what = Command.SHOW_FILE_LIST
            obj = data?.gson<List<FileInfo>>(object : TypeToken<Entity<List<FileInfo>>>() {}.type)
        }
        handler.sendMessage(msg)
    } catch (e: Exception) {
        Log.e("tag", e.message)
        handler.sendEmptyMessage(Command.STATE_DISCONNECT)
    }
}

fun receiveFile(connection: NetConnection?, handler: Handler, fileInfo: FileInfo) {
    val path = Environment.getExternalStorageDirectory().absolutePath + File.separator + NetConnection.FOLDER_NAME + File.separator + fileInfo.fileName
    val file = File(path)
    if (file.exists()) {
        val message = Message()
        message.what = Command.PROGRESS
        message.arg1 = 100
        message.obj = fileInfo.fileName
        handler.sendMessage(message)
        return
    }
    connection?.writeCommand(Command.SEND_FILE + fileInfo.fileName)
    handler.sendEmptyMessage(Command.PROGRESS)
    connection?.saveFile(handler, fileInfo)
}

fun hideKeyboardIfNeed(view: View) {
    val manager = view.context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
    if (manager.isActive)
        manager.hideSoftInputFromWindow(view.windowToken, 0)
}