package com.god.seep.weather.util

import android.os.Handler
import android.os.Message
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.gson
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.google.gson.reflect.TypeToken

fun handleFileList(connection: NetConnection?, handler: Handler) {
    connection?.writeCommand(Command.FILE_LIST)
    val data = connection?.readCommand()
    val msg = Message.obtain()
    msg.run {
        what = Command.SHOW_FILE_LIST
        obj = data?.gson<List<FileInfo>>(object : TypeToken<Entity<List<FileInfo>>>() {}.type)
    }
    handler.sendMessage(msg)
}