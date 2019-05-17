package com.god.seep.weather.adapter

import android.os.Message
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.net.Command
import com.god.seep.weather.ui.TransportActivity
import com.god.seep.weather.ui.TransportFragment
import com.god.seep.weather.ui.TransportListener
import io.reactivex.Observable

class FilePageAdapter(var generator: Observable<List<FileInfo>>, fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager), TransportListener {

    var hThread: TransportActivity.HThread? = null

    override fun fetchRemote(info: FileInfo): Boolean? {
        val message = Message()
        message.run {
            what = Command.GET_FILE
            obj = info
        }
        return hThread?.tHandler?.sendMessage(message)
    }

    override fun fetchFileList(): Boolean? {
        val msg = Message.obtain()
        msg.run {
            what = Command.GET_FILE_LIST
        }
        return hThread?.tHandler?.sendMessage(msg)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return TransportFragment.newInstance(
                if (position == 0)
                    TransportFragment.FILE_TYPE_REMOTE
                else
                    TransportFragment.FILE_TYPE_LOCAL
                , generator, this
        )
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) "Remote" else "Local"
    }
}