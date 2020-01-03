package mogot

import mogot.math.Quaternionfc
import mogot.math.Vector3f
import mogot.math.*
import mogot.math.Vector3fm


class AnimationBehaviour : Behaviour() {
    private interface Key {
        val time: Float
    }

    override fun checkNode(node: Node?) {
        super.checkNode(node)
        node as Spatial?
    }

    override val node: Spatial
        get() = super.node as Spatial

    private class Vec3Key(val vec: Vector3fc, override val time: Float) : Key
    private class QuaKey(val qua: Quaternionfc, override val time: Float) : Key

    private val positions = ArrayList<Vec3Key>()
    private val scale = ArrayList<Vec3Key>()
    private val rotation = ArrayList<QuaKey>()
    var duration: Float = 0f

    fun addPosition(time: Float, position: Vector3fc) {
        this.positions.lastOrNull()?.also {
            if (it.time > time)
                throw IllegalArgumentException()
        }
        this.positions += Vec3Key(vec = position, time = time)
    }

    fun addScale(time: Float, scale: Vector3fc) {
        this.scale.lastOrNull()?.also {
            if (it.time > time)
                throw IllegalArgumentException()
        }
        this.scale += Vec3Key(vec = scale, time = time)
    }

    fun addRotation(time: Float, rotation: Quaternionfc) {
        this.rotation.lastOrNull()?.also {
            if (it.time > time)
                throw IllegalArgumentException()
        }
        this.rotation += QuaKey(qua = rotation, time = time)
    }

    var time = 0f

    override fun onUpdate(dt: Float) {
        time += dt
        if (time > duration)
            time = 0f


        if (rotation.isNotEmpty())
            rotation.getKey(time).let {
                val prevToNextDelta = rotation[it.second].time - rotation[it.first].time
                val prevToCurrentDelta = time - rotation[it.first].time
                val lerpAmount = if (prevToNextDelta > 0) prevToCurrentDelta / prevToNextDelta else prevToCurrentDelta
                rotation[it.first].qua.slerp(rotation[it.second].qua, lerpAmount, node.quaternion)
            }

        if (positions.isNotEmpty()) {
//            var vv = Vector3f(node.position)
            calcPos(time, node.position)
//            println("len=${(vv - node.position).length()}")
        }
//            positions.getKey(time).let {
//                val prevToNextDelta = positions[it.second].time - positions[it.first].time
//                val prevToCurrentDelta = time - positions[it.first].time
//                val lerpAmount = if (prevToNextDelta > 0) prevToCurrentDelta / prevToNextDelta else prevToCurrentDelta
//                positions[it.first].vec.lerp(positions[it.second].vec, lerpAmount, node.position)
//                if (node.position.isNaN()) {
//                    println("pos=${node.position} f=${positions[it.first].vec} s=${positions[it.second].vec} delta=$prevToCurrentDelta prevToNextDelta=$prevToNextDelta")
//                }
//            }
    }


    private fun calcPos(time: Float, dest: Vector3fm) {
        if (positions.isEmpty())
            return

        if (time <= positions[0].time) {
            dest.set(positions[0].vec)
            return
        }
        if (time >= positions[positions.lastIndex].time) {
            dest.set(positions[positions.lastIndex].vec)
            return
        }

        val lastIndex = positions.indexOfFirst { it.time >= time }

        if (lastIndex == -1) {
            dest.set(positions[0].vec)
            return
        }

        if (positions[lastIndex].time == time) {
            dest.set(positions[lastIndex].vec)
            return
        }


        val firstIndex = maxOf(lastIndex - 1, 0)
        val diff = positions[lastIndex].time - positions[firstIndex].time
        val delta = positions[lastIndex].time - time
        val cof = delta / diff
        positions[lastIndex].vec.lerp(positions[firstIndex].vec, cof, dest)
    }

    private fun <T : Key> List<T>.getKey(time: Float): Pair<Int, Int> {
        if (size == 0) {
            return 0 to 0
        }

        // If the time is outside the range,
        // we just return the closest value. (No extrapolation)
        if (time <= get(0).time) {
            return 0 to 0
        } else if (time >= this[size - 1].time) {
            return size - 1 to size - 1
        }


        var startFrame = 0
        var endFrame = 1
        val lastFrame = size - 1

        var i = 0
        while (i < lastFrame && this[i].time < time) {
            startFrame = i
            endFrame = i + 1
            ++i
        }

        if (this[endFrame].time == time) {
            return endFrame to endFrame
        }
        return startFrame to endFrame
    }
}