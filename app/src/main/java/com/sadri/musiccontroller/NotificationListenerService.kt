package com.sadri.musiccontroller

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.util.Log


class NotificationListenerService : android.service.notification.NotificationListenerService() {

  private var mediaController: MediaController? = null

  private lateinit var mediaSessionManager: MediaSessionManager

  private val mediaControllerCallback: MediaController.Callback =
    object : MediaController.Callback() {
      override fun onPlaybackStateChanged(playbackState: PlaybackState?) {
        when (playbackState?.state) {
          PlaybackState.STATE_PLAYING -> {
            mediaController?.metadata?.let { metadata ->
              try {
                MusicController.onPublish(
                  Track(
                    metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                  )
                )
              } catch (e: RuntimeException) {
                Log.e(TAG, "An error occurred reading the media metadata: $e")
              }
            }
          }
        }
      }

      override fun onMetadataChanged(metadata: MediaMetadata?) {
        metadata?.let { metadata ->
          try {
            MusicController.onPublish(
              Track(
                metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
                metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
              )
            )
          } catch (e: RuntimeException) {
            Log.e(TAG, "An error occurred reading the media metadata: $e")
          }
        }
      }
    }

  private val activeSessionsChangedListener = MediaSessionManager.OnActiveSessionsChangedListener {
    registerActiveMediaControllerCallback(mediaSessionManager)
  }

  override fun onCreate() {
    super.onCreate()

    mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    registerActiveMediaControllerCallback(mediaSessionManager)

    addSessionStateChangeListener()
  }

  override fun onListenerConnected() {
    super.onListenerConnected()

    addSessionStateChangeListener()
  }

  override fun onDestroy() {
    unregisterCallback(mediaController)

    super.onDestroy()
  }

  private fun addSessionStateChangeListener() {
    try {
      mediaSessionManager.addOnActiveSessionsChangedListener(
        activeSessionsChangedListener,
        ComponentName(this, NotificationListenerService::class.java)
      )
      Log.i(TAG, "Successfully added session change listener")
    } catch (e: SecurityException) {
      Log.e(TAG, "Failed to add session change listener")
    }
  }

  private fun getActiveMediaController(mediaSessionManager: MediaSessionManager): MediaController? {
    return try {
      val mediaControllers = mediaSessionManager.getActiveSessions(
        ComponentName(
          this,
          NotificationListenerService::class.java
        )
      )
      mediaControllers.firstOrNull()
    } catch (e: SecurityException) {
      null
    }
  }

  private fun registerActiveMediaControllerCallback(mediaSessionManager: MediaSessionManager) {
    getActiveMediaController(mediaSessionManager)?.let { mediaController ->
      registerCallback(mediaController)

      mediaController.metadata?.let { metadata ->
        mediaControllerCallback.onMetadataChanged(metadata)
      }
    }
  }

  private fun registerCallback(mediaController: MediaController) {

    unregisterCallback(this.mediaController)

    Log.i(TAG, "Registering callback for ${mediaController.packageName}")

    this.mediaController = mediaController
    mediaControllerCallback.let { mediaControllerCallback ->
      mediaController.registerCallback(mediaControllerCallback)
    }
  }

  private fun unregisterCallback(mediaController: MediaController?) {

    Log.i(TAG, "Unregistering callback for ${mediaController?.packageName}")

    mediaControllerCallback.let { mediaControllerCallback ->
      mediaController?.unregisterCallback(mediaControllerCallback)
    }
  }
}