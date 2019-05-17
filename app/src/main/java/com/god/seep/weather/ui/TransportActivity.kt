package com.god.seep.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.god.seep.weather.R
import com.god.seep.weather.adapter.FileListAdapter
import com.god.seep.weather.adapter.FilePageAdapter
import com.god.seep.weather.dialog.ProgressDialog
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.util.handleFileList
import com.god.seep.weather.net.Command
import com.god.seep.weather.net.NetConnection
import com.god.seep.weather.util.hideKeyboardIfNeed
import com.god.seep.weather.util.receiveFile
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_transport.*

/**
 * 文件  C <--> S (相互传)
 * 消息  线程间交互
 */
class TransportActivity : AppCompatActivity() {
    private var thread: HThread? = null
    private var generetor: Observable<List<FileInfo>>? = null
    private var emitter: ObservableEmitter<List<FileInfo>>? = null
    private var progressDialog: ProgressDialog? = null

    private val mainHandler = @SuppressLint("HandlerLeak") object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {

                Command.STATE_CONNECTED -> {
                    input.visibility = View.GONE
                    hideKeyboardIfNeed(input)
                    state_show.text = getString(R.string.connected)
                }

                Command.STATE_DISCONNECT -> {
                    input.visibility = View.VISIBLE
                    state_show.text = getString(R.string.disconnect)
                }

                Command.STATE_CONNECTING -> state_show.text = getString(R.string.connecting)

                Command.SHOW_FILE_LIST -> {
                    val (success, data, error) = msg.obj as Entity<ArrayList<FileInfo>>
                    if (success)
                        emitter?.onNext(data)
                    else
                        toast(error)
                }

                Command.PROGRESS -> {
                    if (progressDialog == null)
                        progressDialog = ProgressDialog(this@TransportActivity, msg.obj.toString())
                    if (msg.arg1 != 100 && !progressDialog!!.isShowing)
                        progressDialog?.show()
                    progressDialog?.percent = msg.arg1

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
        initData()
    }

    private fun initData() {
        RxPermissions(this)
                .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { if (!it.granted) toast("请授权--" + it.name) }
                .isDisposed
        generetor = Observable.create { emitter: ObservableEmitter<List<FileInfo>> -> this.emitter = emitter }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
        val pageAdapter = FilePageAdapter(generetor!!, supportFragmentManager)
        fileViewPager.adapter = pageAdapter
        btn_connect.setOnClickListener {
            val ip = ip_address.text.toString()
            if (TextUtils.isEmpty(ip))
                toast("请输入服务端IP地址")
            else {
                thread = HThread(ip)
                thread?.start()
                pageAdapter.hThread = thread!!
            }
        }
    }

    override fun onDestroy() {
        thread?.quitSafely()
        super.onDestroy()
    }

    inner class THandler(looper: Looper, var connection: NetConnection) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            Log.e("tag", "msg -- " + msg?.obj)
            when (msg?.what) {
                Command.GET_FILE_LIST -> handleFileList(connection, mainHandler)
                Command.GET_FILE -> receiveFile(connection, mainHandler, msg.obj as FileInfo)
            }
        }
    }

    inner class HThread(ip: String) : HandlerThread("transport_thread") {
        private var connection: NetConnection? = null
        private var address = ip
        var tHandler: Handler? = null

        override fun onLooperPrepared() {
            mainHandler.sendEmptyMessage(Command.STATE_CONNECTING)
            connection = NetConnection(address)
            if (connection!!.isConnected())
                mainHandler.sendEmptyMessage(Command.STATE_CONNECTED)
            else
                mainHandler.sendEmptyMessage(Command.STATE_DISCONNECT)
            tHandler = THandler(looper, connection!!)
        }

        override fun quitSafely(): Boolean {
            connection?.close()
            return super.quitSafely()
        }
    }
}
