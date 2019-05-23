package com.god.seep.weather.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.god.seep.weather.R
import com.god.seep.weather.extentions.toast
import com.god.seep.weather.util.playDing
import com.god.seep.weather.util.playVibrator
import kotlinx.android.synthetic.main.dialog_progress.*

class ProgressDialog(context: Context, var fileName: String, type: Int = TYPE_DOWNLOAD) : Dialog(context) {
    var percent: Int = 0
        set(value) {
            field = value
            if (progress != null)
                progress.progress = value
            if (progress_desc != null)
                progress_desc.text = "$value%"
            if (value == 100) {
//                playNotification(context)
                playDing(context)
                playVibrator(context)
                context.toast(if (type == TYPE_DOWNLOAD) "下载完毕" else "上传完毕")
                if (isShowing)
                    dismiss()
            }//后面奔溃
        }
    var type: Int = TYPE_DOWNLOAD
        set(value) {
            field = value
            if (title != null)
                title.text = "${fileName}，" + if (value == TYPE_DOWNLOAD) "正在下载。。。" else "正在上传。。。"
        }

    init {
        this.type = type
        this.fileName = fileName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.dialog_progress)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = lp

        title.text = "${fileName}，" + if (type == TYPE_DOWNLOAD) "正在下载。。。" else "正在上传。。。"
    }


    companion object {
        const val TYPE_DOWNLOAD = 0x01
        const val TYPE_UPLOAD = 0x02
    }
}