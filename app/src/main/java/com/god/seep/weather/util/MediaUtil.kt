package com.god.seep.weather.util

import android.annotation.TargetApi
import android.app.Service
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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