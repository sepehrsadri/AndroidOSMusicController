package com.sadri.musiccontroller

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object MusicController {
  private val _trackLiveData: MutableLiveData<Track> = MutableLiveData()
  val trackLiveData: LiveData<Track> get() = _trackLiveData

  fun onPublish(track: Track) {
    Log.d(TAG, "new track published $track")
    _trackLiveData.value = track
  }
}