package com.zidoo.clingapi.data

import kotlinx.serialization.Serializable

@Serializable
data class QPlayTrackMetaData(
    var TracksMetaData: List<TrackData>
)