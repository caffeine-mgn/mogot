package mogot.math

import kotlin.js.JsName
import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

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