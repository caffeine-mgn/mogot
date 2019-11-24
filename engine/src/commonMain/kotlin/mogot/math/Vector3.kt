@file:JvmName("Vec3KKT")
package mogot.math

import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

expect interface Vector3fc {
    @JsName("getX")
    fun x(): Float

    @JsName("getY")
    fun y(): Float

    @JsName("getZ")
    fun z(): Float
}

object Vec3f {
    val UP = Vector3f(0f, 1f, 0f)
}

//val Vector3fc.Companion.

expect class Vector3f : Vector3fc {
    constructor(x: Float, y: Float, z: Float)
    constructor()

    @JvmField
    var x:Float

    @JvmField
    var y:Float

    @JvmField
    var z:Float
}

operator fun Vector3fc.minus(other: Vector3f): Vector3f =
        Vector3f(x() - other.x(), y() - other.y(), z() - other.z())

operator fun Vector3fc.unaryMinus() = Vector3f(-x(), -y(), -z())