package com.god.seep.weather.net

import java.io.*
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket

class NetConnection {
    companion object {
    }

    private val host: String = "10.0.0.5"
    private val port: Int = 9999
    private var socket: Socket? = null
    private var mInputStream: InputStream? = null
    private var mOutputStream: OutputStream? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    init {
        try {
            socket = Socket(host, port)
            mInputStream = socket?.getInputStream()
            mOutputStream = socket?.getOutputStream()
            reader = BufferedReader(InputStreamReader(mInputStream))
            writer = PrintWriter(OutputStreamWriter(mOutputStream))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reConnect() {
        socket?.connect(InetSocketAddress(host, port))
        socket?.channel
        mInputStream = socket?.getInputStream()
        mOutputStream = socket?.getOutputStream()
        reader = BufferedReader(InputStreamReader(mInputStream))
        writer = PrintWriter(OutputStreamWriter(mOutputStream))
    }

    fun isConnected(): Boolean = socket?.isConnected ?: false

    fun getReader(): BufferedReader? = reader

    fun getWriter(): PrintWriter? = writer

    fun readCommand(): String = reader?.readLine() ?: ""

    fun writeCommand(command: String) {
        writer?.println(command)
        writer?.flush()
    }

    fun sendFile(file: File) {
        mOutputStream?.write(file.inputStream().read())
        mOutputStream?.flush()
    }

    fun saveFile(fileName: String) {
        if (mInputStream != null) {
            val fos = File(fileName).outputStream()
            fos.write(mInputStream!!.read())
            fos.flush()
        }
    }

    fun close() {
        reader?.close()
        writer?.close()
        socket?.close()
    }
}