package com.god.seep.weather.ui

import android.annotation.SuppressLint
import android.os.*
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.god.seep.weather.R
import com.god.seep.weather.adapter.FileListAdapter
import com.god.seep.weather.adapter.FilePageAdapter
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.util.handleFileList
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.god.seep.weather.util.receiveFile
import kotlinx.android.synthetic.main.activity_transport.*

/**
 * 文件  C <--> S (相互传)
 * 消息  线程间交互
 */
class TransportActivity : AppCompatActivity() {

    private var mAdapter: FileListAdapter? = null
    private var threadHandler: Handler? = null
    private var thread: HandlerThread? = null

    private val mainHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                Command.STATE_CONNECTED -> state_show.text = getString(R.string.connected)
                Command.STATE_DISCONNECT -> {
                    input.visibility = View.VISIBLE
                    state_show.text = getString(R.string.disconnect)
                }
                Command.STATE_CONNECTING -> state_show.text = getString(R.string.connecting)
                Command.SHOW_FILE_LIST -> {
                    val (success, data, error) = msg.obj as Entity<ArrayList<FileInfo>>
                    if (success)
                        mAdapter?.newData = data
                    else
                        toast(error)
                    process.text = null
                }
                Command.PROGRESS -> {
                    process.visibility = View.VISIBLE
                    if (msg.arg1 == 100) {
                        process.text = "${msg.obj}已接收"
                        process.visibility = View.GONE
                    } else
                        process.text = "进度：${msg.arg1}%"

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        process.visibility = View.GONE
        initData()
    }

    private fun initData() {
        val pageAdapter = FilePageAdapter(threadHandler)
        fileViewPager.adapter = pageAdapter
        btn_send.setOnClickListener {
            val msg = Message.obtain()
            msg.run {
                what = Command.GET_FILE_LIST
                obj = message
            }
            threadHandler?.sendMessage(msg)
        }
        btn_connect.setOnClickListener {
            val ip = ip_address.text.toString()
            if (TextUtils.isEmpty(ip))
                toast("请输入服务端IP地址")
            else {
                input.visibility = View.GONE
                thread = HThread(ip)
                thread?.start()
            }
        }
    }

    override fun onDestroy() {
        thread?.quitSafely()
        super.onDestroy()
    }

    inner class HThread(ip: String) : HandlerThread("transport_thread") {
        private var connection: NetConnection? = null
        private var address = ip

        override fun onLooperPrepared() {
            mainHandler.sendEmptyMessage(Command.STATE_CONNECTING)
            connection = NetConnection(address)
            if (connection!!.isConnected())
                mainHandler.sendEmptyMessage(Command.STATE_CONNECTED)
            else
                mainHandler.sendEmptyMessage(Command.STATE_DISCONNECT)
            threadHandler = object : Handler(looper) {
                override fun handleMessage(msg: Message?) {
                    super.handleMessage(msg)
                    when (msg?.what) {
                        Command.GET_FILE_LIST -> handleFileList(connection, mainHandler)
                        Command.GET_FILE -> receiveFile(connection, mainHandler, msg.obj as FileInfo)
                    }
                }
            }
        }

        override fun quitSafely(): Boolean {
            connection?.close()
            return super.quitSafely()
        }
    }
}
