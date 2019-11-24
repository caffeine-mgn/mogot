package mogot.math

actual interface Vector4fc {
    @JsName("getX")
    actual fun x(): Float

    @JsName("getY")
    actual fun y(): Float

    @JsName("getZ")
    actual fun z(): Float

    @JsName("getW")
    actual fun w(): Float
}

actual class Vector4f actual constructor(actual var x: Float, actual var y: Float, actual var z: Float, actual var w: Float) : Vector4fc {
    actual constructor() : this(0f, 0f, 0f, 0f)

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    override fun x(): Float = x


    override fun y(): Float = y


    override fun z(): Float = z
    override fun w(): Float = w
}