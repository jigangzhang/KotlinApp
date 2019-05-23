package com.god.seep.weather.net

import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import com.god.seep.weather.dialog.ProgressDialog
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
        get() {
            if (field == null && socket != null)
                return socket?.getOutputStream()
            return field
        }
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

    init {
        try {
            socket = Socket(host, port)
            mInputStream = socket?.getInputStream()
            mOutputStream = socket?.getOutputStream()
            reader = BufferedReader(InputStreamReader(socket?.getInputStream()))
            writer = PrintWriter(OutputStreamWriter(socket?.getOutputStream()))
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
                //此处可能是死循环，对文件时结束符返回-1，此处接收文件，不能以-1判断
                while (read != -1) {
                    revLength += read
                    fos.write(bytes, 0, read)
                    val p = ((revLength / fileInfo.fileSize) * 100).toInt()
                    if (p > percent) {
                        percent = p
                        val message = Message()
                        message.arg1 = percent
                        message.arg2 = ProgressDialog.TYPE_DOWNLOAD
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

    fun sendFile(handler: Handler, file: File) {
        val bytes = ByteArray(2048)
        val totalLength = file.length()
        var sendLength = 0.0
        var percent = 0
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(file)
            var read = fis.read(bytes)
            while (read != -1) {
                sendLength += read
                mOutputStream?.write(bytes, 0, read)
                val p = ((sendLength / totalLength) * 100).toInt()
                if (p > percent) {
                    percent = p
                    val message = Message.obtain()
                    message.run {
                        arg1 = percent
                        obj = file.name
                        arg2 = ProgressDialog.TYPE_UPLOAD
                        what = Command.PROGRESS
                    }
                    handler.sendMessage(message)
                }
//                if (sendLength >= totalLength)
//                    break
                read = fis.read(bytes)
            }
            Log.e("tag", "send finish")
        } catch (e: Exception) {
            Log.e("tag", "e --> ${e.message}")
            //e 为 socket相关时 连接断开
        } finally {
            mOutputStream?.flush()
            fis?.close()
        }
    }

    fun close() {
        reader?.close()
        writer?.close()
        socket?.close()
    }
}