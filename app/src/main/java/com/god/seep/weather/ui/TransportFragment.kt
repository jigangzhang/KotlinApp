package com.god.seep.weather.ui

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.god.seep.weather.R
import com.god.seep.weather.adapter.FileListAdapter
import com.god.seep.weather.adapter.OnItemClickListener
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.net.NetConnection
import kotlinx.android.synthetic.main.pager_file_list.view.*
import java.io.File

class TransportFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var mRootView: View
    private var mType: Int = FILE_TYPE_REMOTE
    var mListener: TransportListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context != null)
            mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mType = arguments?.getInt(TYPE_FILE)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.pager_file_list, container, false)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRootView.fileList.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        val adapter = FileListAdapter()
        adapter.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(item: FileInfo, position: Int) {
                mContext.toast(item.fileName)
                mListener?.remote(item)
            }
        }
        view.fileList.adapter = adapter
        if (mType == FILE_TYPE_REMOTE) {
        } else {
            val folder = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + NetConnection.FOLDER_NAME)
            if (folder.exists() && folder.isDirectory) {
                val list = folder.listFiles()
                        .map { FileInfo(it.name, it.length(), it.lastModified(), false, true) }
                adapter.newData = list
            }
        }
    }

    companion object {
        private const val TYPE_FILE = "TYPE_FILE"
        const val FILE_TYPE_LOCAL = 0x01
        const val FILE_TYPE_REMOTE = 0x02

        fun newInstance(type: Int): Fragment {
            val fragment = TransportFragment()
            val bundle = Bundle()
            bundle.putInt(TYPE_FILE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}

interface TransportListener {
    fun remote(info: FileInfo)
}