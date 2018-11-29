package com.god.seep.weather.net

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

class NetConnection {
    companion object {
        val COMMAND_REV_FILE: String = "command_rev_file"
        val COMMAND_SEND_FILE: String = "command_send_file"
        val COMMAND_DENY: String = "command_deny"
        val COMMAND_ACCEPT: String = "command_accept"
    }

    private val host: String = "10.0.0.12"
    private val port: Int = 9999
    private var socket: Socket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null

    init {
        try {
            socket = Socket(host, port)
            mInputStream = socket?.getInputStream()
            mOutputStream = socket?.getOutputStream()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reConnect() {
        socket?.connect(InetSocketAddress(host, port))
        mInputStream = socket?.getInputStream()
        mOutputStream = socket?.getOutputStream()
    }

    fun getInputStream(): InputStream? = socket?.getInputStream()

    fun getOutputStream(): OutputStream? = socket?.getOutputStream()

    fun isConnected(): Boolean = socket?.isConnected ?: false

    fun readCommand(): String = mInputStream?.readBytes().toString()

    fun writeCommand(command: String) {
        mOutputStream?.write(command.toByteArray())
        mOutputStream?.flush()
    }

    fun sendFile(file: File) {
        mOutputStream?.write(file.inputStream().read())
    }

    fun saveFile(fileName: String) {
        if (mInputStream != null) {
            val fos = File(fileName).outputStream()
            fos.write(mInputStream!!.read())
            fos.flush()
        }
    }

    fun close() = socket?.close()
}