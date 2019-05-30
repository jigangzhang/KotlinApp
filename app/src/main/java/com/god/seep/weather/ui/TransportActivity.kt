package com.god.seep.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.god.seep.weather.R
import com.god.seep.weather.adapter.FilePageAdapter
import com.god.seep.weather.aidl.FileInfo
import com.god.seep.weather.dialog.ProgressDialog
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.net.Command
import com.god.seep.weather.util.*
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_transport.*

/**
 * 文件  C <--> S (相互传)
 * 消息  线程间交互
 */
class TransportActivity : AppCompatActivity() {
    private lateinit var pageAdapter: FilePageAdapter
    private var generetor: Observable<List<FileInfo>>? = null
    private var emitter: ObservableEmitter<List<FileInfo>>? = null
    private var progressDialog: ProgressDialog? = null
    private var mService: TransportService? = null

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
                    if (progressDialog == null && msg.obj != null)
                        progressDialog = ProgressDialog(this@TransportActivity, msg.obj.toString())
                    progressDialog?.fileName = msg.obj.toString()
                    progressDialog?.type = msg.arg2
                    progressDialog?.percent = msg.arg1
                    if (msg.arg1 != 100 && !progressDialog!!.isShowing)
                        progressDialog?.show()
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

    var death = IBinder.DeathRecipient {
        Log.e("tag", "binder -- linkToDeath -- binderDied")
//        mService.asBinder.unlinkToDeath(this, 0)
        bindService(
                Intent(this@TransportActivity, TransportService::class.java),
                this@TransportActivity.conn,
                Context.BIND_AUTO_CREATE
        )
    }
    var conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("tag", "binder -- onServiceDisconnected -- ${name?.className}")
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.e("tag", "binder -- onBindingDied -- ${name?.className}")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("tag", "binder -- onServiceConnected -- ${name?.className}")
            mService = (service as TransportService.TransportBinder).getService()
            service.linkToDeath(
                    this@TransportActivity.death,
                    0
            )
            pageAdapter.hService = mService
            mService?.mainHandler = this@TransportActivity.mainHandler
            mService?.connect(ip_address.text.toString())
        }
    }

    private fun initData() {
        RxPermissions(this)
                .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { if (!it.granted) toast("请授权--" + it.name) }
                .isDisposed
        generetor = Observable.create { emitter: ObservableEmitter<List<FileInfo>> -> this.emitter = emitter }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
        pageAdapter = FilePageAdapter(generetor!!, supportFragmentManager)
        fileViewPager.adapter = pageAdapter
        btn_connect.setOnClickListener {
            val ip = ip_address.text.toString()
            if (TextUtils.isEmpty(ip))
                toast("请输入服务端IP地址")
            else {
                if (mService == null)
                    bindService(Intent(this, TransportService::class.java), conn, Context.BIND_AUTO_CREATE)
                else
                    mService?.connect(ip)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_user -> {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_upload -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && data != null) {
            val uri = data.data
            val message = Message.obtain()
            message.what = Command.UPLOAD_FILE
            message.obj = uri?.getRealPath(this)
            mService?.sendMessage(message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
//        stopService(Intent(this, TransportService::class.java))
    }
}
