package com.zidoo.clingapi.data

import kotlinx.serialization.Serializable

@Serializable
data class TrackData(
    var album: String,
    var albumArtURI: String,
    var creator: String,
    var duration: String,
    var lyric: String,
    var protocolInfo: String,
    var songID: String,
    var tag: String,
    var title: String,
    var trackURIs: List<String>
) {
    fun getTrackUri(): String {
        trackURIs?.forEach {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
    }
}