package com.sadri.musiccontroller

open class Track(val name: String, val artistName: String) {

  companion object {

    fun build(name: String?, artistName: String?, albumName: String?): Track? {
      if (name != null && albumName != null && artistName != null) {
        return Track(name, artistName)
      }
      return null
    }
  }

  override fun toString(): String {
    return "Track(name='$name', artistName='$artistName')"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Track

    if (name != other.name) return false
    if (artistName != other.artistName) return false
    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + artistName.hashCode()
    return result
  }
}