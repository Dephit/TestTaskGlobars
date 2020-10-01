package com.sergeenko.alexey.globars.dataClasses

import java.io.Serializable

data class TransportObjects(val success: Boolean, val data: List<ObjectData>) : Serializable

data class ObjectData(
    var id: String? = null,
    var name: String? = null,
    var checked: Boolean = false,
    var detail: Boolean = false,
    var position: Position? = null,
    var eye: Boolean = false,
    var color: String? = null,
    var icon: String? = null,
    var rotate: Boolean = false,
    var to: List<To>? = null
)

data class Position(
    var u: String? = null,
    var t: Long? = null,
    var lt: Double = 0.0,
    var ln: Double = 0.0,
    var s: Int = 0,
    var b: Int = 0,
    var i: Boolean = false,
    var m: Long? = null
)

data class To(
    var name: String? = null,
    var mi: Int = 0,
    var days: Int = 0
)