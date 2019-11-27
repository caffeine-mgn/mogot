package mogot

import org.joml.*

//private val matrixBuffer = BufferUtils.createFloatBuffer(16)
private val matrixArray = FloatArray(16)
private val matrixArray2 = DoubleArray(16)

//fun glLoadMatrixf(matrix: Matrix4fc) {
//    GL45.glLoadMatrixf(matrix.get(matrixBuffer))
//}

fun Matrix4fc.toArray(): FloatArray {
    get(matrixArray)
    return matrixArray
}

fun Matrix4fc.toDoubleArray(): DoubleArray {
    toArray().forEachIndexed { index, fl ->
        matrixArray2[index] = fl.toDouble()
    }
    return matrixArray2
}

val VECTOR_UP: Vector3fc = Vector3f(0f, 1f, 0f)
val VECTOR_LEFT: Vector3fc = Vector3f(-1f, 1f, 0f)

val Quaternionfc.right: Vector3f
    get() = positiveX(Vector3f())

val Quaternionfc.left: Vector3f
    get() = right.negate()

val Quaternionfc.up: Vector3f
    get() = positiveY(Vector3f())

val Quaternionfc.forward: Vector3f
    get() = positiveZ(Vector3f()).negate()

fun Vector3fc.mul(value: Float) = Vector3f(x() * value, y() * value, z() * value)

val TEMP_MATRIX = Matrix4f()
fun Matrix4dc.toArray(): DoubleArray {
    val out = DoubleArray(16)
    out[0] = m00()
    out[1] = m01()
    out[2] = m02()
    out[3] = m03()
    out[4] = m10()
    out[5] = m11()
    out[6] = m12()
    out[7] = m13()
    out[8] = m20()
    out[9] = m21()
    out[10] = m22()
    out[11] = m23()
    out[12] = m30()
    out[13] = m31()
    out[14] = m32()
    out[15] = m33()
    return out
}

fun <T> Iterator<T>.last(): T? {
    if (!hasNext())
        return null
    var element = next()
    while (hasNext()) {
        element = next()
    }
    return element
}

operator fun Vector3fc.minus(value: Vector3fc) = this.sub(value, Vector3f())
operator fun Vector3fc.times(scale: Float) = this.mul(scale, Vector3f())
operator fun Vector3fc.unaryMinus() = negate(Vector3f())
operator fun Vector3fc.plus(other: Vector3f?) = add(other, Vector3f())

fun Matrix4fc.rotateAngle(rotation: Vector3fc, dest: Matrix4f): Matrix4f {
    if (dest !== this)
        dest.set(this)
    dest.rotate(rotation.x(), 1f, 0f, 0f)
    dest.rotate(rotation.y(), 0f, 1f, 0f)
    dest.rotate(rotation.z(), 0f, 0f, 1f)
    return dest
}

fun Matrix4f.rotateAngle(rotation: Vector3fc) = rotateAngle(rotation, this)

fun Vector3f.mul(matrix: Matrix4fc): Vector3f {
    mul(matrix, this)
    return this
}

fun Vector3f.div(matrix: Matrix4fc): Vector3f {
    div(matrix, this)
    return this
}

fun Vector3fc.div(matrix: Matrix4fc, dest: Vector3f): Vector3f {
    val rx = matrix.m00() / x() + matrix.m10() / y() + matrix.m20() / z() + matrix.m30()
    val ry = matrix.m01() / x() + matrix.m11() / y() + matrix.m21() / z() + matrix.m31()
    val rz = matrix.m02() / x() + matrix.m12() / y() + matrix.m22() / z() + matrix.m32()
    dest.x = rx
    dest.y = ry
    dest.z = rz
    return dest
}

fun Vector3fc.mul(matrix: Matrix4fc, dest: Vector3f): Vector3f {
    val rx = matrix.m00() * x() + matrix.m10() * y() + matrix.m20() * z() + matrix.m30()
    val ry = matrix.m01() * x() + matrix.m11() * y() + matrix.m21() * z() + matrix.m31()
    val rz = matrix.m02() * x() + matrix.m12() * y() + matrix.m22() * z() + matrix.m32()
    dest.x = rx
    dest.y = ry
    dest.z = rz
    return dest
}

operator fun Vector3f.timesAssign(matrix: Matrix4fc) {
    mul(matrix, this)
}

//operator fun Vector3fc.times(matrix: Matrix4fc) = mul(matrix, Vector3f())
operator fun Vector3fc.times(matrix: Matrix4fc) = this.mul(matrix, Vector3f())

operator fun Float.times(vector: Vector2fc) = vector * this

operator fun Vector2fc.times(value: Float) = Vector2f(x() * value, y() * value)

fun calcBox(from: Vector3f, to: Vector3f) {
    calcBox(from, to, from, to)
}

fun calcBox(v1: Vector3fc, v2: Vector3fc, from: Vector3f, to: Vector3f) {
    val fx = Math.min(v1.x(), v2.x())
    val fy = Math.min(v1.y(), v2.y())
    val fz = Math.min(v1.z(), v2.z())

    val tx = Math.max(v1.x(), v2.x())
    val ty = Math.max(v1.y(), v2.y())
    val tz = Math.max(v1.z(), v2.z())

    from.set(fx, fy, fz)
    to.set(tx, ty, tz)
}

fun Vector3fc.inBox(from: Vector3fc, to: Vector3fc) =
        x() >= from.x() && x() <= to.x()
                && y() >= from.y() && y() <= to.y()
                && z() >= from.z() && z() <= to.z()

operator fun Vector2fc.minus(other: Vector2fc) = sub(other, Vector2f())
operator fun Vector2fc.plus(other: Vector2fc) = add(other, Vector2f())

fun Vector2fc.normalized() = normalize(Vector2f())
fun Vector3fc.normalized() = normalize(Vector3f())
fun Vector2fc.add(mul: Vector2fc) = add(mul, Vector2f())

fun Vector2fc.isNaN() = x().isNaN() || y().isNaN()
fun Vector3fc.isNaN() = x().isNaN() || y().isNaN() || z().isNaN()

fun Vector2fc.normal(dest: Vector2f): Vector2f {
    dest.x = -y()
    dest.y = x()
    return dest
}

fun Vector2fc.normal() = normal(Vector2f())

fun Vector3fc.asString() = "{${x()}; ${y()}; ${z()}}"
fun Vector3fc.toDegrees() = Vector3f(
        Math.toDegrees(x().toDouble()).toFloat(),
        Math.toDegrees(y().toDouble()).toFloat(),
        Math.toDegrees(z().toDouble()).toFloat()
)

operator fun Vector3fc.times(scale: Vector3fc) = this.mul(scale, Vector3f())


operator fun Matrix4fc.unaryMinus(): Matrix4f = invert(Matrix4f())
operator fun Matrix4fc.times(vector3f: Vector3f): Vector3f = vector3f.mul(get3x3(), Vector3f())
operator fun Matrix4fc.times(viewMatrix: Matrix4fc): Matrix4f = mul(viewMatrix, Matrix4f())
fun Matrix4fc.get3x3() = get3x3(Matrix3f())

val Quaternionfc.yaw: Float
    get() = Math.atan2(2.0 * (y() * z() + w() * x()), (w() * w() - x() * x() - y() * y() + z() * z()).toDouble()).toFloat()

val Quaternionfc.pitch: Float
    get() = Math.asin(-2.0 * (x() * z() - w() * y())).toFloat()

val Quaternionfc.roll: Float
    get() = Math.atan2(2.0 * (x() * y() + w() * z()), (w() * w() + x() * x() - y() * y() - z() * z()).toDouble()).toFloat()

// yaw (Z), pitch (Y), roll (X)
fun Quaternionfc.setRotation(yaw: Float, pitch: Float, roll: Float, dest: Quaternionf): Quaternionf {
    // Abbreviations for the various angular functions
    val cy = Math.cos(yaw * 0.5)
    val sy = Math.sin(yaw * 0.5)
    val cp = Math.cos(pitch * 0.5)
    val sp = Math.sin(pitch * 0.5)
    val cr = Math.cos(roll * 0.5)
    val sr = Math.sin(roll * 0.5)

    dest.w = (cy * cp * cr + sy * sp * sr).toFloat()
    dest.x = (cy * cp * sr - sy * sp * cr).toFloat()
    dest.y = (sy * cp * sr + cy * sp * cr).toFloat()
    dest.z = (sy * cp * cr - cy * sp * sr).toFloat()
    return dest
}

fun Quaternionf.setRotation(yaw: Float, pitch: Float, roll: Float) = setRotation(yaw = yaw, pitch = pitch, roll = roll, dest = this)

fun <T : Vector4fc> Array<T>.toFloatArray(): FloatArray {
    val out = FloatArray(size * 4)
    forEachIndexed { index, vec ->
        out[index * 4 + 0] = vec.x()
        out[index * 4 + 1] = vec.y()
        out[index * 4 + 2] = vec.z()
        out[index * 4 + 3] = vec.w()
    }
    return out
}