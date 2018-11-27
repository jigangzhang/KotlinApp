package com.god.seep.weather.net

import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

class NetConnection {
    private val host: String = "10.0.0.12"
    private val port: Int = 9999
    private var socket: Socket? = null

    init {
        try {
            socket = Socket(host, port)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reConnect() {
        socket?.connect(InetSocketAddress(host, port))
    }

    fun getInputStream(): InputStream? = socket?.getInputStream()

    fun getOutputStream(): OutputStream? = socket?.getOutputStream()

    fun isConnected(): Boolean = socket?.isConnected ?: false

    fun close() = socket?.close()
}