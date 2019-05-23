package com.god.seep.weather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import com.god.seep.weather.R
import com.god.seep.weather.adapter.UserListAdapter
import com.god.seep.weather.entity.Entity
import com.god.seep.weather.entity.UserInfo
import com.god.seep.weather.net.Command
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import kotlinx.android.synthetic.main.activity_user.*

class UserActivity : AppCompatActivity() {
    private lateinit var userAdapter: UserListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        RxBus.get().post(Command.ALL_USER)
        RxBus.get().register(this)
        initData()
    }

    private fun initData() {
        refresh.isRefreshing = true
        refresh.setOnRefreshListener {
            refresh.isRefreshing = true
            RxBus.get().post(Command.ALL_USER)
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
