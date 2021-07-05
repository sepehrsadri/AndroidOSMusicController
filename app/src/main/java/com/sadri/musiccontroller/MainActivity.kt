package com.sadri.musiccontroller

import android.content.Intent
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import kotlin.properties.Delegates


const val TAG = "WTF"

class MainActivity : AppCompatActivity() {

  private var isPlay by Delegates.observable(false) { property, oldValue, newValue ->
    Log.d(TAG, " is play changed : $newValue")

    if (::playPauseButton.isInitialized.not()) return@observable

    val playPauseText = if (newValue) "Pause" else "play"
    playPauseButton.text = playPauseText


    if (
      newValue.not() &&
      ::titleText.isInitialized
    ) {
      titleText.text = ""
      artistText.text = ""
    }
  }

  private lateinit var playPauseButton: Button
  private lateinit var permissionButton: Button
  private lateinit var titleText: TextView
  private lateinit var artistText: TextView
  private lateinit var detailRootLayout: LinearLayout
  private lateinit var audioManager: AudioManager

  private val audioCallback = @RequiresApi(Build.VERSION_CODES.O)
  object : AudioManager.AudioPlaybackCallback() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
      super.onPlaybackConfigChanged(configs)

      Log.d("PWD", "size : ${configs?.size}")
      configs?.forEach {
        Log.d("PWD", " details : ${it.audioAttributes} ")
      }

      val size = configs?.filter { it.audioAttributes.contentType == CONTENT_TYPE_MUSIC }?.size ?: 0
      isPlay = size > 0

    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

    Log.d(TAG, "is music Active " + audioManager.isMusicActive)
    isPlay = audioManager.isMusicActive

    registerActionButtons()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      audioManager.registerAudioPlaybackCallback(
        audioCallback,
        Handler(Looper.getMainLooper())
      )
    }

    MusicController.trackLiveData.observe(
      this,
      { track ->
        if (
          isPlay.not() ||
          isNotificationListenerEnabled().not()
        ) return@observe

        titleText.text = track.name
        artistText.text = track.artistName
      }
    )
  }

  override fun onResume() {
    super.onResume()

    if (isNotificationListenerEnabled()) {
      permissionButton.visibility = View.GONE
      detailRootLayout.visibility = View.VISIBLE
    } else {
      permissionButton.visibility = View.VISIBLE
      detailRootLayout.visibility = View.GONE
    }
  }

  private fun isNotificationListenerEnabled(): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(applicationContext)
      .contains(applicationContext.packageName)
  }

  override fun onDestroy() {
    super.onDestroy()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      audioManager.unregisterAudioPlaybackCallback(audioCallback)
    }
  }

  private fun registerActionButtons() {
    findViewById<Button>(R.id.previousButton).setOnClickListener {
      if (isPlay.not()) return@setOnClickListener
      val eventTime = SystemClock.uptimeMillis()
      val downEvent =
        KeyEvent(
          eventTime,
          eventTime,
          KeyEvent.ACTION_DOWN,
          KeyEvent.KEYCODE_MEDIA_PREVIOUS,
          0
        )
      audioManager.dispatchMediaKeyEvent(downEvent)
    }

    permissionButton = findViewById(R.id.permissionButton)

    permissionButton.setOnClickListener {
      startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    detailRootLayout = findViewById(R.id.detailRoot)
    artistText = findViewById(R.id.artistText)
    titleText = findViewById(R.id.titleText)

    val playPauseText = if (audioManager.isMusicActive) "Pause" else "play"
    playPauseButton = findViewById(R.id.playPauseButton)
    playPauseButton.apply {
      text = playPauseText
      setOnClickListener {
        val eventTime = SystemClock.uptimeMillis()
        if (isPlay) {
          val upEvent =
            KeyEvent(
              eventTime,
              eventTime,
              KeyEvent.ACTION_DOWN,
              KeyEvent.KEYCODE_MEDIA_PAUSE,
              0
            )
          audioManager.dispatchMediaKeyEvent(upEvent)
        } else {
          val upEvent =
            KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0)
          audioManager.dispatchMediaKeyEvent(upEvent)
        }
      }
    }
    findViewById<Button>(R.id.nextButton).setOnClickListener {
      if (isPlay.not()) return@setOnClickListener

      val eventTime = SystemClock.uptimeMillis()
      val downEvent =
        KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0)
      audioManager.dispatchMediaKeyEvent(downEvent)
    }
  }
}