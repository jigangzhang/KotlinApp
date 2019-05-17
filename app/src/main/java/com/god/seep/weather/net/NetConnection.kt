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

class NetConnection(ip_address: String) {
    companion object {
        const val FOLDER_NAME = "shared"
    }

    private val host: String = ip_address
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
            val file = File(folder, fileInfo.fileName)
            file.createNewFile()
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
                var bytes = ByteArray(2048)
                var revLength = 0.0
                var read = mInputStream!!.read(bytes)
                var percent = 0
                //此处可能是死循环
                while (read != -1) {
                    revLength += read
                    fos.write(bytes, 0, read)
                    val p = ((revLength / fileInfo.fileSize) * 100).toInt()
                    if (p > percent) {
                        percent = p
                        val message = Message()
                        message.arg1 = percent
                        message.obj = fileInfo.fileName
                        message.what = Command.PROGRESS
                        handler.sendMessage(message)
                    }
                    if (revLength.compareTo(fileInfo.fileSize) == 0)
                        break
                    read = mInputStream!!.read(bytes)
                }
            } catch (e: Exception) {
                Log.e("tag", "e-->${e.message}")
                handler.sendEmptyMessage(Command.STATE_DISCONNECT)
            } catch (e: IOException) {
                Log.e("tag", "IO exception-->${e.message}")
            } finally {
                fos?.flush()
                fos?.close()
            }
        }
    }

    fun close() {
        reader?.close()
        writer?.close()
        socket?.close()
    }
}