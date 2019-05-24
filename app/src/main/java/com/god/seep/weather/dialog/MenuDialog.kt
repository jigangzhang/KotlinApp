package com.god.seep.weather.dialog

import android.app.Dialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.FileProvider
import com.god.seep.weather.R
import com.god.seep.weather.entity.getMIMEType
import com.god.seep.weather.extentions.toast
import kotlinx.android.synthetic.main.dialog_menu.*
import java.io.File

class MenuDialog(context: Context, var file: File, var onDeleteSuc: () -> Unit) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.dialog_menu)
        btn_cancel.setOnClickListener { dismiss() }
        delete.setOnClickListener {
            val delete = file.delete()
            if (delete)
                onDeleteSuc()
            dismiss()
            context.toast("删除${if (delete) "成功" else "失败"}")
        }
        open.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(context, "com.god.seep.weather.fileProvider", file.parentFile)
                intent.setDataAndType(uri, getMIMEType(file))
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            } else
                intent.setDataAndType(Uri.fromFile(file), getMIMEType(file))
            context.startActivity(intent)
            dismiss()
        }
        open_location.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val uri = FileProvider.getUriForFile(context, "com.god.seep.weather.fileProvider", file.parentFile)
                intent.setDataAndType(uri, "*/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } else {
                intent.setDataAndType(Uri.fromFile(file.parentFile), "*/*")
            }
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(Intent.createChooser(intent, "open"))
            dismiss()
        }
        val wm = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val lp = window?.attributes
        lp?.width = wm.defaultDisplay.width - (context.resources.displayMetrics.density * 40 + 0.5).toInt()
        window?.attributes = lp
    }
}