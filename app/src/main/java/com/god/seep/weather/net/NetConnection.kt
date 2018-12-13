package com.god.seep.weather.net

import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import com.god.seep.weather.entity.FileInfo
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.Exception

class NetConnection {
    companion object {
        val FOLDER_NAME = "shared"
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

    fun saveFile(handler: Handler, fileInfo: FileInfo) {
        if (mInputStream != null) {
            val folder = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + FOLDER_NAME)
            if (!folder.exists())
                folder.mkdirs()
            val path = Environment.getExternalStorageDirectory().absolutePath + File.separator + FOLDER_NAME + File.separator + fileInfo.fileName
            val file = File(path)
            file.createNewFile()
            try {
                val fos = file.outputStream()
                val bytes = ByteArray(2048)
                var revLength = 0
                var read = mInputStream!!.read(bytes)
                while (read != -1) {
                    revLength += bytes.size
                    val message = Message()
                    message.arg1 = ((revLength / fileInfo.fileSize) * 100).toInt()
                    message.obj = fileInfo.fileName
                    message.what = Command.PROGRESS
                    handler.sendMessage(message)
                    fos.write(bytes, 0, read)
                    fos.flush()
                    read = mInputStream!!.read(bytes)
                }
                fos.flush()
            } catch (e: Exception) {
                Log.e("tag", e.message)
                handler.sendEmptyMessage(Command.STATE_DISCONNECT)
            }
        }
    }

    fun close() {
        reader?.close()
        writer?.close()
        socket?.close()
    }
}