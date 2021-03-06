package pw.binom.fbx.file

import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass


fun DataInputStream.readShortL(): Short {
    val ch1 = read()
    val ch2 = read()
    if (ch1 or ch2 < 0)
        throw EOFException()
    return ((ch2 shl 8) + (ch1 shl 0)).toShort()
}


fun DataInputStream.readLongL() = readLong().byteswap()
fun DataInputStream.readDoubleL() = java.lang.Double.longBitsToDouble(readLongL())
fun DataInputStream.readFloatL() = java.lang.Float.intBitsToFloat(readIntL())

fun DataInputStream.readUIntL() = readIntL().toUInt()
fun DataInputStream.readULongL() = readLongL().toULong()
fun DataInputStream.readIntL(): Int {
    return readInt().byteswap()
}

private fun ByteArray.zeroOnly(): Boolean {
    forEach { if (it != 0.toByte()) return false }
    return true
}

fun InputStream.readArray(size: Int): ByteArray {
    val out = ByteArray(size)
    read(out)
    return out
}

fun DataInputStream.readUString1(): String {
    val size = readByte().toUByte()
    val d = readArray(size.toInt())
    return d.asString()
}

fun DataInputStream.readUString4(): String {
    val size = readUIntL()
    val d = readArray(size.toInt())
    return d.asString()
}


fun ByteArray.asString() = String(this)

private val _HEAD_MAGIC = "Kaydara FBX Binary  ${0.toChar()}${26.toChar()}${0.toChar()}"

object FbxReader {

    enum class Version {
        V7_5,
        V7_4
    }

    fun read(stream: InputStream, visiter: FbxVisiter) {
        visiter.start()
        val cursorStream = CursorDataInputStream(stream)
        val data = DataInputStream(cursorStream)


        if (String(data.readArray(23)) != _HEAD_MAGIC)
            throw IOException("Invalid file format")
        val version = data.readUIntL()
        visiter.version(version)
        val ver = when (version) {
            7400u -> Version.V7_4
            7500u -> Version.V7_5
            else -> throw NotImplementedError("Not supported fbx version $version")
        }
        println("Version: $version")


        while (true) {
            if (!readElement(ver, data, visiter) { cursorStream.fill })
                break
        }

        visiter.end()
    }

    private fun readArray(data: DataInputStream, type: KClass<*>, array_byteswap: Boolean): Any {
        val length = data.readUIntL().toInt()
        val encoding = data.readUIntL()
        val comp_len = data.readUIntL().toInt()
//

        fun readArray(data: DataInputStream): Any {
            return when (type) {
                Double::class -> if (array_byteswap) DoubleArray(length) { data.readDoubleL() }.reversedArray() else DoubleArray(length) { data.readDoubleL() }
                Int::class -> if (array_byteswap) IntArray(length) { data.readIntL() } else IntArray(length) { data.readIntL() }
                Float::class -> {
                    if (!array_byteswap)
                        FloatArray(length) { data.readFloatL() }
                    else
                        FloatArray(length) { data.readFloat() }
                }
                Long::class -> LongArray(length) { data.readLong() }.let { if (array_byteswap) it.byteswap() else it }
                else -> TODO("TypeClass: ${type.java.name}")
            }
        }

        return if (encoding == 0u) {
            readArray(data)
        } else if (encoding == 1u) {
            val mem = data.readArray(comp_len.toInt()).decompressZip()
            DataInputStream(ByteArrayInputStream(mem)).use { stream ->
                readArray(stream)
            }
        } else TODO()
    }

    fun read_data_dict(char: Char, data: DataInputStream) =
            when (char) {
                'Y' -> data.readShortL()//: lambda read: unpack(b'<h', read(2))[0],  # 16 bit int
                'C' -> data.readByte() == 1.toByte()//: lambda read: unpack(b'?', read(1))[0],   # 1 bit bool (yes/no)
                'I' -> data.readIntL()//: lambda read: unpack(b'<i', read(4))[0],  # 32 bit int
                'F' -> data.readFloat().byteswap()//: lambda read: unpack(b'<f', read(4))[0],  # 32 bit float
                'D' -> data.readDouble().byteswap()//: lambda read: unpack(b'<d', read(8))[0],  # 64 bit float
                'L' -> data.readLong().byteswap()//: lambda read: unpack(b'<tempQ', read(8))[0],  # 64 bit int
                'R' -> data.readArray(data.readUIntL().toInt())//: lambda read: read(read_uint(read)),      # binary data
                'S' -> data.readUString4()//: lambda read: read(read_uint(read)),      # string data
                'f' -> readArray(data, Float::class, false)//,  # array (float)
                'i' -> readArray(data, Int::class, true)//,   # array (int)
                'd' -> readArray(data, Double::class, false)//,  # array (double)
                'l' -> readArray(data, Long::class, true)//,   # array (long)
                'b' -> readArray(data, Boolean::class, false)//,  # array (bool)
                'c' -> readArray(data, UByte::class, false)//,  # array (ubyte)
                else -> TODO("char=$char  code=${char.toInt()}")
            }

    private fun readElement(version: Version, data: DataInputStream, visiter: ElementContener?, fill: () -> Long): Boolean {
        fun readProp(): ULong =
                when (version) {
                    Version.V7_4 -> data.readUIntL().toULong()
                    Version.V7_5 -> data.readULongL()
                }

        val end_offset = readProp()
        if (end_offset == 0uL) {
            return false
        }

        val prop_count = readProp()
        val prop_length = readProp()
        val block_sentinel_length = when (version) {
            Version.V7_4 -> 4 * 3 + 1
            Version.V7_5 -> 8 * 3 + 1
        }

        val elem_id = data.readUString1()
        val elVisit = visiter?.element(elem_id)

        println("Element: id=$elem_id   prop_count=$prop_count  prop_length=$prop_length")

        (0uL until prop_count).forEach { i ->
            if (fill() > end_offset.toLong())
                throw IOException("Out of data on ${fill()}")
            println("$i/${prop_count - 1u}")
            val type = data.readByte()
            if (type == 0.toByte())
                return@forEach
            val property = read_data_dict(type.toChar(), data)
            elVisit?.property(property)
        }

        if (fill() < end_offset.toLong()) {
            while (fill() < (end_offset - block_sentinel_length.toUInt()).toLong()) {
                readElement(version, data, elVisit, fill)
            }

            val dd = data.readArray(block_sentinel_length)
            if (!dd.zeroOnly())
                throw IOException("failed to read nested block sentinel, expected all bytes to be 0 in $elem_id on ${fill()} end=${end_offset}")
            else
                println("end of node $elem_id")
        }

        if (fill() != end_offset.toLong()) {
            throw IOException("scope length not reached, something is wrong (${(end_offset.toLong() - fill())})")
        }

        elVisit?.elementEnd()
        return true
    }


    fun unpack(packet: CharArray, raw: ByteArray): Array<String> {
        val result = arrayOfNulls<String>(packet.size)

        var pos = 0
        var Strindex = 0

        for (x in packet.indices) {

            val type = packet[x]
            if (type == 'x') {
                pos += 1
                continue
            } else if (type == 'c') {
                val c = (raw[pos].toInt() and 0xFF).toChar()
                result[Strindex] = Character.toString(c)
                Strindex += 1
                pos += 1
            } else if (type == 'h') {
                val bb = ByteBuffer.allocate(2)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                bb.put(raw[pos])
                bb.put(raw[pos + 1])
                val shortVal = bb.getShort(0)
                result[Strindex] = java.lang.Short.toString(shortVal)
                pos += 2
                Strindex += 1
            } else if (type == 's') {
                var s = ""

                while (raw[pos] != 0x00.toByte()) {
                    val c = (raw[pos].toInt() and 0xFF).toChar()
                    s += Character.toString(c)
                    pos += 1
                }
                result[Strindex] = s
                Strindex += 1
                pos += 1
            } else if (type == 'b') {
                val p = raw[pos]
                result[Strindex] = Integer.toString(p.toInt())
                Strindex += 1
                pos += 1
            }
        }
        return result as Array<String>
    }
}