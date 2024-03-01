package org.fourthline.cling.support.qplay

data class TracksMetaData(
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
)