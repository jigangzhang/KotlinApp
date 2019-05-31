package com.god.seep.weather.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import com.god.seep.weather.R
import com.god.seep.weather.adapter.UserListAdapter
import com.god.seep.weather.aidl.UserInfo
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.net.Command
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {
    private lateinit var mService: TransportService
    private lateinit var userAdapter: UserListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        RxBus.get().post(Command.ALL_USER)
        RxBus.get().register(this)
        bindService(Intent(this, TransportService::class.java), conn, Context.BIND_AUTO_CREATE)
        initData()
    }

    var conn = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e("tag", "user binder -- onServiceDisconnected -- ${name?.className}")
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e("tag", "user binder -- onServiceConnected -- ${name?.className}")
            mService = (service as TransportService.TransportBinder).getService()
            mService.sendEmptyMessage(Command.GET_ALL_USER)
        }
    }

    private fun initData() {
        refresh.isRefreshing = true
        refresh.setOnRefreshListener {
            refresh.isRefreshing = true
            mService.sendEmptyMessage(Command.GET_ALL_USER)
        }
        userAdapter = UserListAdapter()
        userList.adapter = userAdapter
        userList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    @Subscribe
    fun onUserEvent(users: Entity<List<UserInfo>>) {
        refresh.isRefreshing = false
        if (users.success)
            userAdapter.data = users.data
    }

    override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
    }
}
