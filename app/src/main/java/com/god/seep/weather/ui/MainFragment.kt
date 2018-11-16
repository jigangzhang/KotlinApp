package com.god.seep.weather.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.god.seep.weather.R
import com.god.seep.weather.entity.generateWeather
import kotlinx.android.synthetic.main.fragment_main.view.*


class MainFragment : Fragment() {
    companion object {
        private const val TAG_FRAGMENT = "TAG_FRAGMENT"
        fun newInstance(tag: Int): MainFragment {
            val fragment = MainFragment()
            val bundle = Bundle()
            bundle.putInt(TAG_FRAGMENT, tag)
            fragment.arguments = bundle
            return fragment
        }
    }

    private var rootView: View? = null
    private var tag: Int = 0

    /**生命周期： setUserVisibleHint -> onCreate -> onCreateView -> setUserVisibleHint*/
    //setUserVisibleHint在 onCreate前调用，故第一个初始化时不能在 setUserVisibleHint中获取数据
    //fragment 懒加载--> setUserVisibleHint 时设置标识， onStart 时根据标识决定是否加载数据
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        rootView?.section_label?.text = "I'm $tag visible $isVisibleToUser"
        Log.e("tag", "position $tag $isVisibleToUser")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tag = arguments?.getInt(TAG_FRAGMENT) ?: 0
        Log.e("tag", "onCreate position $tag")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_main, container, false)
        rootView?.weather_list?.layoutManager = LinearLayoutManager(activity)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rootView?.section_label?.text = "view create position $tag"
        val adapter = WeatherAdapter(generateWeather())
        rootView?.weather_list?.adapter = adapter
        Log.e("tag", "view create position $tag")
    }

    inner class WeatherAdapter(val list: List<String>) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = list[position]
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = TextView(parent.context)
            val lp = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
            textView.run {
                textSize = 16f
                setTextColor(Color.CYAN)
                layoutParams = lp
            }
            return ViewHolder(textView)
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
    }
}