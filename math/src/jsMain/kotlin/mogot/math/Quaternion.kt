package mogot.math

/*
actual interface Quaternionfc {
    @JsName("getX")
    actual fun x(): Float

    @JsName("getY")
    actual fun y(): Float

    @JsName("getZ")
    actual fun z(): Float

    @JsName("getW")
    actual fun w(): Float
}

actual class Quaternionf : Quaternionfc {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var w: Float = 1f

    actual constructor() {

    }

    actual fun identity(): Quaternionf {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
        return this
    }

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun lookAlong(dir: Vector3fc, up: Vector3fc, dest: Quaternionf): Quaternionf {
        // Normalize direction
        val invDirLength = (1.0 / kotlin.math.sqrt(dir.x() * dir.x() + dir.y() * dir.y() + dir.z() * dir.z())).toFloat()
        val dirnX = -dir.x() * invDirLength;
        val dirnY = -dir.y() * invDirLength;
        val dirnZ = -dir.z() * invDirLength;
        // left = up x dir
        var leftX = up.y() * dirnZ - up.z() * dirnY;
        var leftY = up.z() * dirnX - up.x() * dirnZ;
        var leftZ = up.x() * dirnY - up.y() * dirnX;
        // normalize left
        val invLeftLength = (1.0 / kotlin.math.sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ)).toFloat()
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        val upnX = dirnY * leftZ - dirnZ * leftY
        val upnY = dirnZ * leftX - dirnX * leftZ
        val upnZ = dirnX * leftY - dirnY * leftX

        */
/* Convert orthonormal basis vectors to quaternion *//*

        var x: Float
        var y: Float
        var z: Float
        var w: Float
        var t: Double
        var tr = leftX + upnY + dirnZ;
        if (tr >= 0.0) {
            t = kotlin.math.sqrt(tr + 1.0);
            w = (t * 0.5).toFloat()
            t = 0.5 / t;
            x = ((dirnY - upnZ) * t).toFloat()
            y = ((leftZ - dirnX) * t).toFloat()
            z = ((upnX - leftY) * t).toFloat()
        } else {
            if (leftX > upnY && leftX > dirnZ) {
                t = kotlin.math.sqrt(1.0 + leftX - upnY - dirnZ);
                x = (t * 0.5).toFloat()
                t = 0.5 / t;
                y = ((leftY + upnX) * t).toFloat()
                z = ((dirnX + leftZ) * t).toFloat()
                w = ((dirnY - upnZ) * t).toFloat()
            } else if (upnY > dirnZ) {
                t = kotlin.math.sqrt(1.0 + upnY - leftX - dirnZ);
                y = (t * 0.5).toFloat()
                t = 0.5 / t;
                x = ((leftY + upnX) * t).toFloat()
                z = ((upnZ + dirnY) * t).toFloat()
                w = ((leftZ - dirnX) * t).toFloat()
            } else {
                t = kotlin.math.sqrt(1.0 + dirnZ - leftX - upnY);
                z = (t * 0.5).toFloat()
                t = 0.5 / t;
                x = ((dirnX + leftZ) * t).toFloat()
                y = ((upnZ + dirnY) * t).toFloat()
                w = ((upnX - leftY) * t).toFloat()
            }
        }
        */
/* Multiply *//*

        dest.set(this.w * x + this.x * w + this.y * z - this.z * y,
                this.w * y - this.x * z + this.y * w + this.z * x,
                this.w * z + this.x * y - this.y * x + this.z * w,
                this.w * w - this.x * x - this.y * y - this.z * z);
        return dest
    }

    actual fun lookAlong(dir: Vector3fc, up: Vector3fc): Quaternionf = lookAlong(dir, up, this)
    override fun x(): Float = x

    override fun y(): Float = y

    override fun z(): Float = z

    override fun w(): Float = w
}*/
