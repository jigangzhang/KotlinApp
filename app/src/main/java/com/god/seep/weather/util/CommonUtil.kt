package com.god.seep.weather.util

import android.os.Handler
import android.os.Message
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.google.gson.Gson

fun handleFileList(connection: NetConnection?, handler: Handler) {
    connection?.writeCommand(Command.FILE_LIST)
    val data = connection?.readCommand()
    val msg = Message.obtain()
    msg.what = Command.SHOW_FILE_LIST
    msg.obj = responseConverter<FileInfo>(data ?: "")
    handler.sendMessage(msg)
}

fun <T> responseConverter(response: String): Entity<T> {
    return Gson().fromJson<Entity<T>>(
            response,
            Entity::class.java
    )
}