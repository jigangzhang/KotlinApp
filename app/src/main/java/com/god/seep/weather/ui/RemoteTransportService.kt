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
import com.god.seep.weather.aidl.IStateListener
import com.god.seep.weather.aidl.ITransportManager
import com.god.seep.weather.aidl.UserInfo
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.god.seep.weather.util.getAllUser
import com.god.seep.weather.util.handleFileList
import com.god.seep.weather.util.receiveFile
import com.god.seep.weather.util.sendFile
import java.io.FileDescriptor
import java.io.PrintWriter

/**
 *  多进程：
 *      以 : 开头的进程属于当前应用的私有进程，其他应用组件不可以和它跑在同一进程中
 *      不以 : 开头，以 . 开头的进程处于全局进程，其他应用通过 ShareUID 方式可以和它跑在同一进程中（相同的UID和签名），共享数据（data目录、内存等）
 *      每个进程都分配一个独立的虚拟机
 *  跨进程通信方式：
 *      通过 Intent、 共享文件、 sharedPreference；
 *      基于 Binder 的 Messenger 、 AIDL；
 *      ContentProvider；
 *      Socket。
 *  IPC 传输数据时需要序列化 Serializable、Parcelable
 *  Serializable 需要指定 serialVersionUID 在反序列化时 判断是否同一个类
 *      静态成员变量属于类不属于对象，所以不会参与序列化、 transient 标记的成员变量不参与序列化
 *      需要大量 I/O 操作，开销大， 适用于 序列化到存储设备，网络传输等
 *  Parcelable: 系统实现类有： Intent、 Bundle、 Bitmap等， List、 Map 也可以（需要里面的元素可序列化）
 *      效率高，使用麻烦，适用于 Android系统
 *
 *
 *  注册多个 listener 时 使用 RemoteCallbackList 保存， 注销、遍历等操作
 *  耗时问题：
 *      客户端调用远程服务的方法时：
 *          被调用的方法运行在服务端的 Binder 线程池中，此时客户端线程被挂起，
 *          若服务端耗时，UI线程会ANR
 *          客户端的 onServiceConnected、onServiceDisconnected 运行在UI线程中，不可调用服务端耗时方法
 *          服务端方法运行在 Binder 线程池中，不在开线程
 *
 *      远程服务端调用客户端 listener 方法时：
 *          被调用的方法运行在客户端的 Binder 线程池中，此时服务端线程被挂起，
 *          若客户端方法耗时，UI线程会ANR（Service 在 UI线程中），应该在服务端中开线程调用
 *          客户端listener 方法 运行在 Binder线程池中，不可在其中直接访问 UI相关内容（使用 Handler切换到 UI线程）
 *
 *      远程服务 权限验证：
 *          onBind中验证：
 *              permission 验证：
 *                  Manifest 中申明权限：
 *                      <permission android:name="permission-name" android:protectionLevel="normal">
 *              onBind 中鉴权：
 *                  int check = checkCallingOrSelfPermission("permission-name")
 *                  if (check == DENIED) return null;
 *              使用服务时申请权限：
 *                      <users-permission   android:name="permission-name"/>
 *          onTransact 中权限验证：
 *                  验证方式同上： 返回 false
 *                  采用UID、PID验证：
 *                      通过getCallingUid、getCallingPid 拿到 Uid、Pid，通过这两个参数验证包名等
 *                          getPackageManager().getPackagesForUid(getCallingUid()) 等等
 */
class RemoteTransportService : Service() {

    private var mHThread: HThread? = null
    private var mListener: IStateListener? = null
    private val mTransportManager = object : ITransportManager.Stub() {

        override fun registerListener(listener: IStateListener?) {
            mListener = listener
        }

        override fun unregisterListener(listener: IStateListener?) {
            mListener = null
        }

        override fun connect(ip: String) {
            mHThread?.quitSafely()
            mHThread = HThread(ip)
            mHThread?.start()
        }

        override fun getConnState(): Int {
            var state: Int = Command.STATE_DISCONNECT
            val connected = mHThread?.connection?.isConnected()
            if (connected != null) {
                state = if (connected) Command.STATE_CONNECTED else Command.STATE_DISCONNECT
            }
            return state
        }

        override fun getFileList() {
            mHThread?.tHandler?.sendEmptyMessage(Command.GET_FILE_LIST)
        }

        override fun downFile(info: FileInfo?) {
            val message = Message.obtain()
            message.run {
                what = Command.GET_FILE
                obj = info
            }
            mHThread?.tHandler?.sendMessage(message)
        }

        override fun getUsers() {
            mHThread?.tHandler?.sendEmptyMessage(Command.GET_ALL_USER)
        }

        override fun sendFile(path: String?) {
            val message = Message.obtain()
            message.run {
                what = Command.UPLOAD_FILE
                obj = path
            }
            mHThread?.tHandler?.sendMessage(message)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        Log.e("tag", "onBind --")
        return mTransportManager
    }

    override fun onRebind(intent: Intent?) {
        Log.e("tag", "onRebind --")
    }

    override fun onCreate() {
        Log.e("tag", "onCreate --")
        startForeground(TransportService.NOTICES_ID, "连接未建立")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("tag", "onStartCommand --")
        startForeground(TransportService.NOTICES_ID, "后台连接中")
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

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e("tag", "onUnbind --")
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mHThread?.quitSafely()
        stopForeground(true)
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
                Command.GET_FILE_LIST -> handleFileList(connection, mListener)
                Command.GET_FILE -> receiveFile(connection, mListener, msg.obj as FileInfo)
                Command.GET_ALL_USER -> getAllUser(connection)
                Command.UPLOAD_FILE -> sendFile(connection, mListener, msg.obj as String)
            }
        }
    }

    inner class HThread(ip: String) : HandlerThread("transport_thread") {
        private var address = ip
        var connection: NetConnection? = null
        var tHandler: Handler? = null

        override fun onLooperPrepared() {
            mListener?.onConnectState(Command.STATE_CONNECTING)
            connection = NetConnection(address)
            if (connection!!.isConnected()) {
                mListener?.onConnectState(Command.STATE_CONNECTED)
                startForeground(TransportService.NOTICES_ID, "后台连接中")
            } else
                mListener?.onConnectState(Command.STATE_DISCONNECT)
            tHandler = THandler(looper, connection!!)
        }

        override fun quitSafely(): Boolean {
            connection?.close()
            return super.quitSafely()
        }
    }
}
