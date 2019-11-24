package mogot.math

actual interface Vector3fc {
    @JsName("getX")
    actual fun x(): Float

    @JsName("getY")
    actual fun y(): Float

    @JsName("getZ")
    actual fun z(): Float
}

actual class Vector3f actual constructor(actual var x: Float, actual var y: Float, actual var z: Float) : Vector3fc {
    actual constructor() : this(0f, 0f, 0f)


    override fun x(): Float = x


    override fun y(): Float = y


    override fun z(): Float = z
}