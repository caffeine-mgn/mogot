package mogot.math

import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer

fun FloatDataBuffer.builder() = FloatDataBufferBuilder(this)

class FloatDataBufferBuilder(val builder: FloatDataBuffer) {
    var cursor = 0
    fun push(x: Float, y: Float, z: Float) {
        builder[cursor++] = x
        builder[cursor++] = y
        builder[cursor++] = z
    }

    fun push(vector: Vector3fc) = push(vector.x, vector.y, vector.z)

    fun push(x: Float, y: Float) {
        builder[cursor++] = x
        builder[cursor++] = y
    }
}

fun FloatDataBuffer.each3(func: (Float, Float, Float) -> Unit) {
    if (size % 3 == 0)
        throw IllegalArgumentException("Can't read each 3 values: Invalid size: $size")
    val it = iterator()
    while (it.hasNext()) {
        val v1 = it.next()
        val v2 = it.next()
        val v3 = it.next()
        func(v1, v2, v3)
    }
}

fun FloatDataBuffer.each2(func: (Float, Float) -> Unit) {
    if (size % 2 == 0)
        throw IllegalArgumentException("Can't read each 2 values: Invalid size: $size")
    val it = iterator()
    while (it.hasNext()) {
        val v1 = it.next()
        val v2 = it.next()
        func(v1, v2)
    }
}

fun IntDataBuffer.each3(func: (Int, Int, Int) -> Unit) {
    if (size % 3 == 0)
        throw kotlin.IllegalArgumentException("Can't read each 3 values: Invalid size: $size")
    val it = iterator()
    while (it.hasNext()) {
        val v1 = it.next()
        val v2 = it.next()
        val v3 = it.next()
        func(v1, v2, v3)
    }
}

fun IntDataBuffer.each2(func: (Int, Int) -> Unit) {
    if (size % 2 == 0)
        throw kotlin.IllegalArgumentException("Can't read each 2 values: Invalid size: $size")
    val it = iterator()
    while (it.hasNext()) {
        val v1 = it.next()
        val v2 = it.next()
        func(v1, v2)
    }
}