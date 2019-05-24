package com.god.seep.weather.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.god.seep.weather.R
import kotlinx.android.synthetic.main.dialog_notice.*

class NoticeDialog(context: Context, var fileName: String, var function: () -> Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.dialog_notice)
        message.text = "${fileName}，下载该文件吗？"
        btn_cancel.setOnClickListener { dismiss() }
        btn_confirm.setOnClickListener {
            function()
            dismiss()
        }
    }
}