package com.god.seep.weather.ui

import android.annotation.SuppressLint
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.god.seep.weather.R
import com.god.seep.weather.adapter.FileListAdapter
import com.god.seep.weather.adapter.OnItemClickListener
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
                Command.STATE_DISCONNECT -> state_show.text = getString(R.string.disconnect)
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
                    if (msg.arg1 == 100)
                        process.text = "${msg.obj}已接收"
                    else
                        process.text = "进度：${msg.arg1}%"

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)
        initData()
        thread = HThread("transport_thread")
        thread?.start()
    }

    private fun initData() {
        fileList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        mAdapter = FileListAdapter()
        mAdapter?.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(item: FileInfo, position: Int) {
                toast(item.fileName)
                val message = Message()
                message.run {
                    what = Command.GET_FILE
                    obj = item
                }
                val isOK = threadHandler?.sendMessage(message)
            }
        }
        fileList.adapter = mAdapter
        btn_send.setOnClickListener {
            val msg = Message.obtain()
            msg.run {
                what = Command.GET_FILE_LIST
                obj = message
            }
            threadHandler?.sendMessage(msg)
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
