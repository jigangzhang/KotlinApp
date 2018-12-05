package com.god.seep.weather.ui

import android.annotation.SuppressLint
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.god.seep.weather.R
import com.god.seep.weather.net.NetConnection
import kotlinx.android.synthetic.main.activity_transport.*
import java.lang.Exception

/**
 * 文件  C <--> S (相互传)
 * 消息  线程间交互
 */
class TransportActivity : AppCompatActivity() {
    private var threadHandler: Handler? = null
    private var thread: HandlerThread? = null
    private val mainHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                1 -> state_show.text = getString(R.string.connected)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)
        thread = HThread("transport_thread")
        thread?.start()
        initData()
    }

    private fun initData() {
        btn_send.setOnClickListener {
            val message = message.text.toString()
            if (!TextUtils.isEmpty(message)) {
                val msg = Message.obtain()
                msg.run {
                    what = 1
                    obj = message
                }
                threadHandler?.sendMessage(msg)
            }
        }
    }

    override fun onDestroy() {
        thread?.quitSafely()
        super.onDestroy()
    }

    inner class HThread(threadName: String) : HandlerThread(threadName) {
        private var connection: NetConnection? = null

        override fun onLooperPrepared() {
            connection = NetConnection()
            if (connection != null && connection!!.isConnected()) {
                try {
                    mainHandler.sendEmptyMessage(1)
                    threadHandler = object : Handler(looper) {
                        override fun handleMessage(msg: Message?) {
                            super.handleMessage(msg)
                            when (msg?.what) {
                                1 -> {
                                    connection?.writeCommand(msg.obj as String)
                                    Log.e("tag", "rev-->" + connection?.readCommand())
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("tag", "exception-->${e.message}" + e.printStackTrace())
                } finally {
//                    connection?.close()
                }
            }
        }

        override fun quitSafely(): Boolean {
            connection?.close()
            return super.quitSafely()
        }
    }
}
