package com.god.seep.weather.adapter

import android.os.Message
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.god.seep.weather.aidl.FileInfo
import com.god.seep.weather.aidl.ITransportManager
import com.god.seep.weather.net.Command
import com.god.seep.weather.ui.TransportFragment
import com.god.seep.weather.ui.TransportListener
import com.god.seep.weather.ui.TransportService
import io.reactivex.Observable

class FilePageAdapter(var generator: Observable<List<FileInfo>>, fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager), TransportListener {

    var hService: TransportService? = null
    var transportManager: ITransportManager? = null

    override fun fetchRemote(info: FileInfo): Boolean? {
//        val message = Message()
//        message.run {
//            what = Command.GET_FILE
//            obj = info
//        }
//        return hService?.sendMessage(message)
        transportManager?.downFile(info)
        return true
    }

    override fun fetchFileList(): Boolean? {
//        val msg = Message.obtain()
//        msg.run {
//            what = Command.GET_FILE_LIST
//        }
//        return hService?.sendMessage(msg)
        transportManager?.getFileList()
        return if (transportManager == null) null else true
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