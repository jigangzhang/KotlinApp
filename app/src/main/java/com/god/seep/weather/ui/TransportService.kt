package com.god.seep.weather.ui

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.god.seep.weather.R
import com.god.seep.weather.aidl.FileInfo
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.god.seep.weather.util.getAllUser
import com.god.seep.weather.util.handleFileList
import com.god.seep.weather.util.receiveFile
import com.god.seep.weather.util.sendFile
import java.io.FileDescriptor
import java.io.PrintWriter

class TransportService : Service() {
    companion object {
        const val NOTICES_ID = 0x01
    }

    var mHThread: HThread? = null
    var mainHandler: Handler? = null
    override fun onBind(intent: Intent): IBinder {
        Log.e("tag", "onBind --")
        return TransportBinder()
    }

    inner class TransportBinder : Binder() {

        fun getService(): TransportService {
            return this@TransportService
        }
    }

    /**
     *只有在 onUnbind 返回 true 时才会调用
     */
    override fun onRebind(intent: Intent?) {
        Log.e("tag", "onRebind --")
    }

    override fun onCreate() {
        Log.e("tag", "onCreate --")
//        startForeground(NOTICES_ID, "连接未建立")
    }

    fun connect(ip: String) {
        mHThread?.quitSafely()
        mHThread = HThread(ip)
        mHThread?.start()
    }

    fun sendMessage(message: Message): Boolean? {
        return mHThread?.tHandler?.sendMessage(message)
    }

    fun sendEmptyMessage(what: Int) {
        mHThread?.tHandler?.sendEmptyMessage(what)
    }

    var called = false

    /**
     * bindService时，为触发此方法
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("tag", "onStartCommand --")
        if (!called) {
            startForeground(NOTICES_ID, "后台连接中")
            called = true
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground(id: Int, content: String) {
        val intent = PendingIntent.getActivity(this, 1,
                Intent(this, TransportActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("transport", "transport", NotificationManager.IMPORTANCE_HIGH)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, "transport")
                .setContentTitle("连接")
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(intent)
                .build()
        startForeground(id, notification)
    }

    /**
     * 当所有连接断开时触发
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Log.e("tag", "onUnbind --")
        return true
    }

    /**
     * service 销毁时触发，当所有 activity 销毁后 service 也销毁了？
     * 未以 startService 方式开启过 service，即只以 bindService 方式启动 Service时，
     * 在最后一个连接断开时 （unbindService） service 也同时销毁了
     */
    override fun onDestroy() {
        super.onDestroy()
        mHThread?.quitSafely()
        Log.e("tag", "onDestroy -- service")
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        Log.e("tag", "onConfigurationChanged --")
    }


    override fun dump(fd: FileDescriptor?, writer: PrintWriter?, args: Array<out String>?) {
        super.dump(fd, writer, args)
        Log.e("tag", "dump -- ${fd.toString()}")
    }

    override fun onLowMemory() {
        Log.e("tag", "onLowMemory --")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.e("tag", "onTaskRemoved --")
    }

    override fun onTrimMemory(level: Int) {
        Log.e("tag", "onTrimMemory --")
    }

    inner class THandler(looper: Looper, var connection: NetConnection) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            Log.e("tag", "msg -- obj ${msg?.obj}  what -- ${msg?.what}")
            when (msg?.what) {
                Command.GET_FILE_LIST -> handleFileList(connection, mainHandler)
                Command.GET_FILE -> receiveFile(connection, mainHandler, msg.obj as FileInfo)
                Command.GET_ALL_USER -> getAllUser(connection)
                Command.UPLOAD_FILE -> sendFile(connection, mainHandler, msg.obj as String)
            }
        }
    }

    inner class HThread(ip: String) : HandlerThread("transport_thread") {
        private var address = ip
        var connection: NetConnection? = null
        var tHandler: Handler? = null

        override fun onLooperPrepared() {
            mainHandler?.sendEmptyMessage(Command.STATE_CONNECTING)
            connection = NetConnection(address)
            if (connection!!.isConnected()) {
                mainHandler?.sendEmptyMessage(Command.STATE_CONNECTED)
                startForeground(NOTICES_ID, "后台连接中")
            } else
                mainHandler?.sendEmptyMessage(Command.STATE_DISCONNECT)
            tHandler = THandler(looper, connection!!)
        }

        override fun quitSafely(): Boolean {
            connection?.close()
            return super.quitSafely()
        }
    }
}
