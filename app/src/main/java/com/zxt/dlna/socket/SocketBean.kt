package com.zidoo.clingapi.socket

import com.zidoo.clingapi.data.QPlayTrackMetaData
import com.zxt.dlna.socket.Actions

/**
 * Description:  <br>
 * @author: fy <br>
 * Date: 2024/4/2 <br>
 */
@Serializable
data class SocketBean(
    var type: String,
    var actions: Actions,
    var trackMetaData: QPlayTrackMetaData?,
    var json: String?,
    var position: Int,
    var duration: Int,
    var volume: Double

) {

    constructor(actions: Actions) : this(
        "", actions
    )

    constructor(type: String, actions: Actions, trackMetaData: QPlayTrackMetaData?) : this(
        type, actions, trackMetaData, null, 0, 0, 0.0
    )

    constructor(type: String, actions: Actions) : this(
        type, actions, null, null, 0, 0, 0.0
    )

    constructor(type: String, actions: Actions, volume: Double) : this(
        type, actions, null, null, 0, 0, volume
    )

    constructor(type: String, actions: Actions, position: Int, duration: Int) : this(
        type, actions, null, null, position, duration, 0.0
    )

}