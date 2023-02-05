package com.example.seeamo.core.utilize.helper

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import com.google.android.exoplayer2.ExoPlayer

@RequiresApi(Build.VERSION_CODES.O)
class ExoAudioFocusHelper(
    private val player: ExoPlayer,
    private val audioManager: AudioManager,
    private val audioAttributes: AudioAttributesCompat,
) : ExoPlayer by player {

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                abandonAudioFocus()
            }
        }
    }

    private val audioFocusRequest by lazy { buildAudioFocusRequest() }

    private fun buildAudioFocusRequest(): AudioFocusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes.unwrap() as AudioAttributes)
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()

    private fun requestAudioFocus() = audioManager.requestAudioFocus(audioFocusRequest)
    private fun abandonAudioFocus() = audioManager.abandonAudioFocusRequest(audioFocusRequest)

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) {
            val result = requestAudioFocus()
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                play()
        } else abandonAudioFocus()
    }

}