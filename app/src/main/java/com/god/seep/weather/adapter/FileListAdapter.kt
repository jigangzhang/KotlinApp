package com.god.seep.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.god.seep.weather.R
import com.god.seep.weather.entity.FileInfo
import com.god.seep.weather.extentions.toDate
import kotlinx.android.synthetic.main.item_file_info.view.*


class FileListAdapter : RecyclerView.Adapter<ViewHolder>() {
    var newData: List<FileInfo>? = null
        set(value) {
            field = value ?: ArrayList<FileInfo>()
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_info, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return newData?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(newData!![position])
    }
}

class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {

    fun setData(fileInfo: FileInfo?) {
        view.fileName.text = fileInfo?.fileName
        view.fileSize.text = fileInfo?.fileSize.toString()
        view.updateTime.text = fileInfo?.modifyTime?.toDate()
    }
}
