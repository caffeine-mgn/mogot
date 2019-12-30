package pw.binom.fbx.file

class AnimationCurve(val el: FbxRoot.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is AnimationCurveNode) {
            when (param) {
                "d|X" -> obj.x = this
                "d|Y" -> obj.y = this
                "d|Z" -> obj.z = this
                else -> TODO()
            }
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Value(val time: Float, val value: Float)

    val times: LongArray = el.get("KeyTime").first().properties[0] as LongArray
    val values: FloatArray = el.get("KeyValueFloat").first().properties[0] as FloatArray

    init {
        if (times.size != values.size)
            throw IllegalArgumentException("KeyTime.size not match KeyValueFloat.Size")

        //Проверка на последовательность времени
        if (times.isNotEmpty()) {
            var t = times[0]
            for (i in 1 until times.size) {
                if (times[i] < t)
                    throw IllegalArgumentException()
                t = times[i]
            }
        }
    }

    fun get(time: Long): Float {
        if (times.isEmpty()) {
            throw IllegalArgumentException("Frame is empty")
        }

        if (time <= times[0]) {
            return values[0]
        }
        if (time >= times[times.lastIndex]) {
            return values[values.lastIndex]
        }

        val lastIndex = times.indexOfFirst {
            it >= time
        }

        if (times[lastIndex] == time)
            return values[lastIndex]


        val firstIndex = maxOf(lastIndex - 1, 0)

        val diff = times[lastIndex] - times[firstIndex]
        val delta = times[lastIndex] - time
        val cof = (delta.toDouble() / diff.toDouble()).toFloat()
        return values[firstIndex] + (values[lastIndex] - values[firstIndex]) * cof
    }
}