package com.god.seep.weather.adapter

import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewpager.widget.PagerAdapter
import com.god.seep.weather.R
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.net.Command
import kotlinx.android.synthetic.main.pager_file_list.view.*

class FilePageAdapter(threadHandler: Handler?) : PagerAdapter() {
    private var handler = threadHandler
    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context).inflate(R.layout.pager_file_list, container, false)
        view.fileList.addItemDecoration(DividerItemDecoration(container.context, DividerItemDecoration.VERTICAL))
        val adapter = FileListAdapter()
        adapter.itemClickListener = object : OnItemClickListener {
            override fun onItemClick(item: FileInfo, position: Int) {
                container.context.toast(item.fileName)
                val message = Message()
                message.run {
                    what = Command.GET_FILE
                    obj = item
                }
                val isOK = handler?.sendMessage(message)
            }
        }
        view.fileList.adapter = adapter
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "list"
    }
}