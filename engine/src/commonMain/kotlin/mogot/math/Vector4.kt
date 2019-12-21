package mogot.math

import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

interface Vector4fc {
    val x: Float

    val y: Float

    val z: Float
    val w: Float
}

open class Vector4f(override var x: Float = 0f, override var y: Float = 0f, override var z: Float = 0f, override var w: Float = 0f) : Vector4fc {
    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }
}
/*
expect interface Vector4fc {
    @JsName("getX")
    fun x(): Float

    @JsName("getY")
    fun y(): Float

    @JsName("getZ")
    fun z(): Float

    @JsName("getW")
    fun w(): Float
}

expect class Vector4f : Vector4fc {
    constructor()
    constructor(x: Float, y: Float, z: Float, w: Float)

    @JvmField
    var x:Float

    @JvmField
    var y:Float

    @JvmField
    var z:Float

    @JvmField
    var w:Float
}
 */