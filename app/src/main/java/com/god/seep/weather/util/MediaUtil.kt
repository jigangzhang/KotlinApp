package com.god.seep.weather.util

import android.annotation.TargetApi
import android.app.Service
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.god.seep.weather.R


fun playNotification(context: Context) {
    val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
    ringtone.play()
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun playDing(context: Context) {
    val manager = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
    var shouldPlay = true
    if (manager.ringerMode != AudioManager.RINGER_MODE_NORMAL)//不播放，return
        shouldPlay = false

    val player = MediaPlayer()
    player.setAudioAttributes(AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .build())
    player.setOnCompletionListener { mp -> mp.seekTo(0) }
    val fd = context.resources.openRawResourceFd(R.raw.ding)
    player.setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
    fd.close()
    player.setVolume(1f, 1f)
    player.prepare()
    player.start()
}

fun playVibrator(context: Context) {
    val vibrator = context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(100)
}

fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == this.authority
}

fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == this.authority
}

fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == this.authority
}

fun Uri.getRealPath(context: Context): String? {
    if (ContentResolver.SCHEME_FILE == this.scheme)
        return this.path
    if (ContentResolver.SCHEME_CONTENT == this.scheme) {
        var path: String? = null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            path = getPathFromUri(context, this, null, null)
        } else {
            if (DocumentsContract.isDocumentUri(context, this)) {
                if (this.isExternalStorageDocument()) {
                    val split = DocumentsContract.getDocumentId(this).split(":")
                    val type = split[0]
                    if ("primary" == type.toLowerCase())
                        path = Environment.getExternalStorageDirectory().path + "/" + split[1]
                } else if (this.isDownloadsDocument()) {
                    val documentId = DocumentsContract.getDocumentId(this)
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), documentId.toLong())
                    path = getPathFromUri(context, contentUri, null, null)
                } else if (this.isMediaDocument()) {
                    val split = DocumentsContract.getDocumentId(this).split(":")
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    path = getPathFromUri(context, contentUri, "_id=?", arrayOf(split[1]))
                }
            } else {
                path = getPathFromUri(context, this, null, null)
            }
        }
        return path
    }
    return null
}

fun getPathFromUri(context: Context, contentUri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
    if (contentUri == null) return null
    var path: String? = null
    val cursor = context.contentResolver.query(contentUri, arrayOf(MediaStore.Images.Media.DATA),
            selection, selectionArgs, null)
    if (cursor != null && cursor.moveToFirst()) {
        val index = cursor.getColumnIndexOrThrow("_data")
        path = cursor.getString(index)
    }
    cursor?.close()
    return path
}