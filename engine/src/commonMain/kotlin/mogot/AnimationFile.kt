package mogot

import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.math.Vector4f
import pw.binom.io.*
import kotlin.math.ceil
import kotlin.math.floor

private const val START_OBJECT = 1.toByte()
private const val END_OBJECT = 2.toByte()
private const val PROPERTY = 3.toByte()
private const val FRAME = 4.toByte()

//enum class PropertyType {
//    VEC3,
//    VEC2,
//    FLOAT,
//    STRING,
//    INT
//}

private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
    when {
        fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }
}

fun <T> List<T>.binarySearch2(fromIndex: Int = 0, toIndex: Int = this.size, comparator: (T) -> Int): Int {
    val list = this
    rangeCheck(list.size, fromIndex, toIndex)

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high).ushr(1) // safe from overflows
        val midVal = list.get(mid)

        val cmp = comparator(midVal)

        if (cmp < 0)
            low = mid + 1
        else if (cmp > 0)
            high = mid - 1
        else
            return mid // key found
    }
    return -(low + 1)  // key not found
}

class AnimationFile(val frameInSecond: Int, val frameCount: Int, val objects: MutableList<Object>) : ResourceImpl() {

    init {
        require(frameCount > 0) { "Frame Count must be more then 0" }
        require(frameInSecond > 0) { "Frame in Second must be more then 0" }
    }

    class Object(val path: String) {
        val properties = HashSet<Property<Any>>()
    }

    class Frame<T : Any>(val time: Int, val value: T) {
        override fun toString(): String {
            return "Frame(time=$time, value=$value)"
        }
    }

    class Property<T : Any>(val file: AnimationFile, val type: Field.Type, val name: String) {
        val frames = ArrayList<Frame<T>>()

        fun lerp(currentFrame: Float, revers: Boolean, lerpFunc: (T, T, Float) -> Unit) {
            val current = getCurrentFrame(floor(currentFrame).toInt(), revers)
            var next = getNextFrame(ceil(currentFrame).toInt(), revers)
            if (next != null) {
                println("lerp ${current.time} -> ${next.time}")
                lerpFunc(current.value, next.value, currentFrame / (next.time - current.time))
            } else {
                next = getCurrentFrame(0, revers)
                println("lerp ${current.time} -> ${next!!.time}")
                val timeLerp = file.frameCount.toFloat() - current.time + next!!.time
                val currentLerp = file.frameCount.toFloat() - currentFrame + next!!.time
                lerpFunc(current.value, next!!.value, currentLerp / timeLerp)
            }
        }

        fun currentFrameIndex(time: Int, revers: Boolean): Int {
            require(frames.size > 0)
            val v = frames.binarySearch2 { time - it.time }
            return v
//            var l = findLowerFrame { it.time - time }
//            when {
//                l < 0 -> l++
//                l >= frames.size -> return -1//l = frames.lastIndex
//            }
//            return l
        }

        fun getCurrentFrame(time: Int, revers: Boolean): Frame<T> {
            val l = currentFrameIndex(time, revers)
            require(l >= 0)
            return frames[l]
//            if (frames.isEmpty())
//                return null
//            var l = findLowerFrame { it.time - time }
//            when {
//                l < 0 -> l++
//                l >= frames.size -> return null
//            }
//            return frames[l]
        }

        fun getNextFrame(time: Int, revers: Boolean): Frame<T>? {
            var l = currentFrameIndex(time, revers)
            if (l < 0)
                return null
            l++
            if (l >= frames.size)
                return null
            val frame = frames[l]
            if (frame.time < time)
                return null
            return frame
        }

        private fun findLowerFrame(comparison: (Frame<T>) -> Int): Int {
            var low = 0
            var high = frames.lastIndex
            while (low <= high) {
                val mid = (low + high).ushr(1) // safe from overflows
                val midVal = frames[mid]
                val cmp = comparison(midVal)

                when {
//                    cmp < 0 && mid + 1 >= high -> return low
                    cmp < 0 -> low = mid + 1
                    cmp > 0 -> high = mid - 1
                    else -> return mid// key found
                }
            }
            return low  // key not found
        }
    }


    companion object {
        internal suspend fun load(engine: Engine, path: String): AnimationFile {
            val file = engine.resources.fileSystem.get(Unit, "$path.bin")?.read()
                    ?: throw FileSystem.FileNotFoundException(path)
            return file.use { stream ->
                val magic = ByteArray(ANIMATION_MAGIC_BYTES.size)
                stream.readFully(magic)
                ANIMATION_MAGIC_BYTES.forEachIndexed { index, byte ->
                    if (magic[index] != byte)
                        throw IllegalArgumentException("Can't load image from $path. This is not an Animation")
                }

                val frameInSecond = stream.readInt()
                val frameCount = stream.readInt()
                val objects = ArrayList<Object>()
                val file = AnimationFile(frameInSecond = frameInSecond, frameCount = frameCount, objects = objects)
                var lastObject: Object? = null
                var lastProperty: Property<Any>? = null
                var lastTime = -1
                loop@ while (true) {
                    when (stream.read()) {
                        START_OBJECT -> {
                            val nodePath = stream.readUTF8String()
                            val obj = Object(nodePath)
                            lastObject = obj
                            objects += obj
                        }
                        PROPERTY -> {
                            stream.readUTF8String()
                            val propertyName = stream.readUTF8String()
                            val type = Field.Type.values()[stream.readInt()]
                            val property = Property<Any>(file, type, propertyName)
                            lastProperty = property
                            lastTime = -1
                            lastObject!!.properties += property
                        }
                        FRAME -> {
                            val time = stream.readInt()
                            check(time >= 0) { "Invalid frame time in file \"$path\"" }
                            check(time > lastTime) { "Invalid Frame Order in file \"$path\"" }
                            lastTime = time
                            val value = when (lastProperty!!.type) {
                                Field.Type.INT -> stream.readInt()
                                Field.Type.FLOAT -> stream.readFloat()
                                Field.Type.STRING -> stream.readUTF8String()
                                Field.Type.VEC2 -> Vector2f(stream.readFloat(), stream.readFloat())
                                Field.Type.VEC3 -> Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat())
                                Field.Type.BOOL -> stream.read() > 1
                                Field.Type.VEC4 -> Vector4f(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat())
                            }
                            lastProperty.frames += Frame(time, value)
                        }
                        END_OBJECT -> break@loop
                    }
                }
                file
            }
        }
    }
}

suspend fun Resources.loadAnimation(path: String) = AnimationFile.load(engine, path)

fun <T> FileSystem<T>.getRelative(file: String, currentDir: String): String {

    fun String.parent(): String {
        val p = this.lastIndexOf('/')
        if (p == -1)
            throw FileSystem.FileNotFoundException("")
        return substring(0, p)
    }

    var node = currentDir
    file.splitToSequence('/').forEach {
        if (it == ".")
            return@forEach
        if (it == "..") {
            node = node.parent()
            return@forEach
        }
        node = "$node/$it"
    }
    return node
}