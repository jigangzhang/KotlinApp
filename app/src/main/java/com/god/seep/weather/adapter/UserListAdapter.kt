package com.god.seep.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.god.seep.weather.R
import com.god.seep.weather.aidl.UserInfo
import kotlinx.android.synthetic.main.item_user_info.view.*


class UserListAdapter : RecyclerView.Adapter<UserHolder>() {
    var data: List<UserInfo>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_info, parent, false)
        return UserHolder(view)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.bindData(data!![position])
    }

}

class UserHolder(var view: View) : RecyclerView.ViewHolder(view) {
    fun bindData(info: UserInfo) {
        view.text_ip.text = info.ip
        view.text_port.text = info.port.toString()
    }
}