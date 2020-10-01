package com.sergeenko.alexey.globars.dataClasses

import java.io.Serializable
import java.util.*

data class TrackingSessionsResult(val success: Boolean, val data: List<Data>): Serializable

data class Data(
    var id: String? = null,
    var userId: String? = null,
    var units: List<Unit>? = null,
    var groups: List<Any>? = null,
    var geozones: List<Any>? = null,
    var drivers: List<Any>? = null,
    var trailers: List<Any>? = null,
    var trackingSettings: Any? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null
)