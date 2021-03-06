package com.god.seep.weather.util

import android.app.Service
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.god.seep.weather.aidl.FileInfo
import com.god.seep.weather.aidl.IStateListener
import com.god.seep.weather.aidl.UserInfo
import com.god.seep.weather.dialog.ProgressDialog
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.extentions.gson
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hwangjr.rxbus.RxBus
import java.io.File

fun handleFileList(connection: NetConnection?, handler: Handler?) {
    try {
        connection?.writeCommand(Command.FILE_LIST)
        val data = connection?.readCommand()
        val msg = Message.obtain()
        msg.run {
            what = Command.SHOW_FILE_LIST
            obj = data?.gson<List<FileInfo>>(object : TypeToken<Entity<List<FileInfo>>>() {}.type)
        }
        handler?.sendMessage(msg)
    } catch (e: Exception) {
        Log.e("tag", e.message)
        handler?.sendEmptyMessage(Command.STATE_DISCONNECT)
    }
}

fun handleFileList(connection: NetConnection?, listener: IStateListener?) {
    try {
        connection?.writeCommand(Command.FILE_LIST)
        val data = connection?.readCommand()
        val entity = data?.gson<List<FileInfo>>(object : TypeToken<Entity<List<FileInfo>>>() {}.type)
        if (entity?.success!!)
            listener?.onRevFileList(entity.data)
        else
            Log.e("tag", entity.toString())
    } catch (e: Exception) {
        Log.e("tag", e.message)
        listener?.onConnectState(Command.STATE_DISCONNECT)
    }
}

fun receiveFile(connection: NetConnection?, handler: Handler?, fileInfo: FileInfo) {
    val path = Environment.getExternalStorageDirectory().absolutePath + File.separator + NetConnection.FOLDER_NAME + File.separator + fileInfo.fileName
    val file = File(path)
    val message = Message()
    message.what = Command.PROGRESS
    message.obj = fileInfo.fileName
    message.arg2 = ProgressDialog.TYPE_DOWNLOAD
    if (file.exists()) {
        message.arg1 = 100
        handler?.sendMessage(message)
        return
    }
    connection?.writeCommand(Command.SEND_FILE + fileInfo.fileName)
    message.arg1 = 0
    handler?.sendMessage(message)
    connection?.saveFile(handler, fileInfo)
}

fun receiveFile(connection: NetConnection?, listener: IStateListener?, fileInfo: FileInfo) {
    val path = Environment.getExternalStorageDirectory().absolutePath + File.separator + NetConnection.FOLDER_NAME + File.separator + fileInfo.fileName
    val file = File(path)
    if (file.exists()) {
        listener?.onProgress(fileInfo.fileName, ProgressDialog.TYPE_DOWNLOAD, 100)
        return
    }
    connection?.writeCommand(Command.SEND_FILE + fileInfo.fileName)
    listener?.onProgress(fileInfo.fileName, ProgressDialog.TYPE_DOWNLOAD, 0)
    connection?.saveFile(listener, fileInfo)
}

fun sendFile(connection: NetConnection?, handler: Handler?, path: String) {
    val file = File(path)
    val message = Message()
    message.what = Command.PROGRESS
    message.obj = file.name
    message.arg1 = 0
    message.arg2 = ProgressDialog.TYPE_UPLOAD
    handler?.sendMessage(message)
    val info = FileInfo(file.name, file.length(), file.lastModified(), false, true)
    val json = Gson().toJson(info)
    connection?.writeCommand(Command.REV_FILE + json)
    connection?.sendFile(handler, file)

}

fun sendFile(connection: NetConnection?, listener: IStateListener?, path: String) {
    val file = File(path)
    listener?.onProgress(file.name, ProgressDialog.TYPE_UPLOAD, 0)
    val info = FileInfo(file.name, file.length(), file.lastModified(), false, true)
    val json = Gson().toJson(info)
    connection?.writeCommand(Command.REV_FILE + json)
    connection?.sendFile(listener, file)

}

fun getAllUser(connection: NetConnection?) {
    connection?.writeCommand(Command.ALL_USER)
    val line = connection?.readCommand()
    Log.e("tag", "user --> $line")
    val gson = line?.gson<List<UserInfo>>(object : TypeToken<Entity<List<UserInfo>>>() {}.type)
    Log.e("tag", "users --> ${gson.toString()}")
    RxBus.get().post(gson)
}

fun hideKeyboardIfNeed(view: View) {
    val manager = view.context.getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
    if (manager.isActive)
        manager.hideSoftInputFromWindow(view.windowToken, 0)
}