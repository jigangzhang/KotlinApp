package com.god.seep.weather.adapter

import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.net.Command
import com.god.seep.weather.ui.TransportFragment
import com.god.seep.weather.ui.TransportListener

class FilePageAdapter(threadHandler: Handler?, fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager), TransportListener {

    override fun remote(info: FileInfo) {
        val message = Message()
        message.run {
            what = Command.GET_FILE
            obj = info
        }
        val isOK = handler?.sendMessage(message)
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
        )
    }

    private var handler = threadHandler

    override fun getPageTitle(position: Int): CharSequence? {
        return if (position == 0) "Remote" else "Local"
    }
}