package com.god.seep.weather.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.god.seep.weather.R
import com.god.seep.weather.net.NetConnection
import java.io.*
import java.lang.Exception

/**
 * 文件  C <--> S (相互传)
 * 消息  线程间交互
 */
class TransportActivity : AppCompatActivity() {
    private var connection: NetConnection? = null
    private var disconnect: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transport)
        Thread {//service？？
            connection = NetConnection()
            if (connection != null && connection!!.isConnected()) {
                var reader: BufferedReader? = null
                var writer: PrintWriter? = null
                try {
                    Log.e("tag", "connect-->" + connection?.isConnected())
                    reader = BufferedReader(InputStreamReader(connection?.getInputStream()))
                    writer = PrintWriter(OutputStreamWriter(connection?.getOutputStream()))
                    writer.println("hello!")
                    writer.flush()
                    var line = reader.readLine()
                    while (disconnect || line != "ok") {
                        Log.e("tag", "server-->$line")
                        writer.println("are you ok?")
                        writer.flush()
                        line = reader.readLine()//阻塞
                    }
                } catch (e: Exception) {
                    Log.e("tag", "exception-->${e.message}" + e.printStackTrace())
                } finally {
                    reader?.close()
                    writer?.flush()
                    writer?.close()
                    connection?.close()
                }
            }
        }.start()
    }

    override fun onDestroy() {
        disconnect = true
        super.onDestroy()
    }
}
