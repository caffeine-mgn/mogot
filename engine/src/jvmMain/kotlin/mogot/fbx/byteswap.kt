package mogot.fbx

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.Inflater

fun LongArray.byteswap() = LongArray(size) { this[it].byteswap() }
fun FloatArray.byteswap() = FloatArray(size) { this[it].byteswap() }
fun IntArray.byteswap() = IntArray(size) { this[it].byteswap() }
fun DoubleArray.byteswap() = DoubleArray(size) { this[it].byteswap() }

fun Long.byteswap(): Long {
    val ch1 = (this ushr 56) and 0xFF
    val ch2 = (this ushr 48) and 0xFF
    val ch3 = (this ushr 40) and 0xFF
    val ch4 = (this ushr 32) and 0xFF
    val ch5 = (this ushr 24) and 0xFF
    val ch6 = (this ushr 16) and 0xFF
    val ch7 = (this ushr 8) and 0xFF
    val ch8 = (this ushr 0) and 0xFF

    return ((ch8 shl 56) or
            ((ch7 and 0xFF) shl 48) or
            ((ch6 and 0xFF) shl 40) or
            ((ch5 and 0xFF) shl 32) or
            ((ch4 and 0xFF) shl 24) or
            ((ch3 and 0xFF) shl 16) or
            ((ch2 and 0xFF) shl 8) or
            ((ch1 and 0xFF) shl 0))
}

fun Int.byteswap(): Int {
    val ch1 = (this ushr 24) and 0xFF
    val ch2 = (this ushr 16) and 0xFF
    val ch3 = (this ushr 8) and 0xFF
    val ch4 = (this ushr 0) and 0xFF
    return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
}

fun Short.byteswap(): Short {
    val ch1 = (this.toInt() ushr 8) and 0xFF
    val ch2 = (this.toInt() ushr 0) and 0xFF
    return ((ch2 shl 8) + (ch1 shl 0)).toShort()
}

fun Double.byteswap(): Double {
    val value = java.lang.Double.doubleToLongBits(this).byteswap()
    return java.lang.Double.longBitsToDouble(value)
}

fun Float.byteswap(): Float {
    val value = java.lang.Float.floatToIntBits(this).byteswap()
    return java.lang.Float.intBitsToFloat(value)
}

fun ByteArray.decompressZip(): ByteArray {
    val out = ByteArrayOutputStream()
    val decompresser = Inflater()
    decompresser.setInput(this)
    val buf = ByteArray(512)
    while (!decompresser.finished()) {
        val length = decompresser.inflate(buf)
        out.write(buf, 0, length)
    }
    decompresser.end()
    val result = out.toByteArray()
    out.close()
    return result
}

fun ByteArray.drawHex(length: Int): ByteArray {
    var c = 0

    println("HEX Data [${size}]:")
    asSequence().map {
        it.toUByte().toString(16).toUpperCase().let { if (it.length != 2) "0$it" else it }
    }.forEach {
        if (c >= length) {
            println()
            c = 0
        }
        if (c != 0)
            print(" ")
        print(it)
        c++
    }
    println()
    return this
}

fun String.hexToByteArray(): ByteArray {
    val values = split(' ')
    return ByteArray(values.size) { values[it].toInt(16).toByte() }
}

fun Int.toBytes(): ByteArray {
    val s = ByteArrayOutputStream()
    return DataOutputStream(s).use {
        it.writeInt(this)
        s.toByteArray()
    }
}

fun ByteArray.toInt() = DataInputStream(ByteArrayInputStream(this)).use {
    it.readInt()
}

fun Double.toBytes(): ByteArray {
    val s = ByteArrayOutputStream()
    return DataOutputStream(s).use {
        it.writeDouble(this)
        s.toByteArray()
    }
}

fun ByteArray.toDouble() = DataInputStream(ByteArrayInputStream(this)).use {
    it.readDouble()
}