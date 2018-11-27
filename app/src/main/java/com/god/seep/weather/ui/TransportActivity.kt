package com.god.seep.weather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.god.seep.weather.R
import com.god.seep.weather.net.NetConnection

class TransportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)
        Thread {
            val connection = NetConnection()
            Log.e("tag", "connect-->" + connection.isConnected())
        }.start()
    }
}
