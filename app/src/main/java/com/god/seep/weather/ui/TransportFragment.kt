package com.god.seep.weather.ui

import android.annotation.SuppressLint
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
import com.god.seep.weather.aidl.FileInfo
import com.god.seep.weather.dialog.MenuDialog
import com.god.seep.weather.dialog.NoticeDialog
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.net.NetConnection
import io.reactivex.Observable
import kotlinx.android.synthetic.main.pager_file_list.view.*
import java.io.File

class TransportFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var mRootView: View
    private lateinit var fileList: Array<File>
    private var mType: Int = FILE_TYPE_REMOTE
    private var mListener: TransportListener? = null

    private var generator: Observable<List<FileInfo>>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mRootView.fileList.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        val adapter = FileListAdapter()
        adapter.itemClickListener = object : OnItemClickListener {
            override fun onItemLongClick(item: FileInfo, position: Int): Boolean {
                if (mType == FILE_TYPE_LOCAL) {
                    MenuDialog(mContext, fileList[position]) {
                        adapter.newData = getFiles()
                    }.show()
                    return true
                }
                return false
            }

            override fun onItemClick(item: FileInfo, position: Int) {
                if (mType == FILE_TYPE_REMOTE)
                    NoticeDialog(mContext, item.fileName!!) {
                        mListener?.fetchRemote(item)
                    }.show()
            }
        }
        view.fileList.adapter = adapter
        if (mType == FILE_TYPE_REMOTE) {
            generator?.subscribe { list ->
                adapter.newData = list
//                mRootView.refresh.isRefreshing = false
                mRootView.refresh.finishRefresh()
            }
            mRootView.refresh.setOnRefreshListener {
                val result = mListener?.fetchFileList()
                if (result == null) {
                    mContext.toast("请检查连接是否已建立")
//                    mRootView.refresh.isRefreshing = false
                    mRootView.refresh.finishRefresh()
                }
            }
        } else {
            adapter.newData = getFiles()
            mRootView.refresh.setOnRefreshListener { adapter.newData = getFiles() }
        }
    }

    private fun getFiles(): List<FileInfo> {
        val folder = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + NetConnection.FOLDER_NAME)
        var list = emptyList<FileInfo>()
        if (folder.exists() && folder.isDirectory) {
            fileList = folder.listFiles()
            list = fileList.map { FileInfo(it.name, it.length(), it.lastModified(), false, true) }
        }
//        mRootView.refresh.isRefreshing = false
        mRootView.refresh.finishRefresh()
        return list
    }

    companion object {
        private const val TYPE_FILE = "TYPE_FILE"
        const val FILE_TYPE_LOCAL = 0x01
        const val FILE_TYPE_REMOTE = 0x02

        fun newInstance(type: Int, generator: Observable<List<FileInfo>>, listener: TransportListener): Fragment {
            val fragment = TransportFragment()
            fragment.generator = generator
            fragment.mListener = listener
            val bundle = Bundle()
            bundle.putInt(TYPE_FILE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}

interface TransportListener {
    fun fetchRemote(info: FileInfo): Boolean?

    fun fetchFileList(): Boolean?
}