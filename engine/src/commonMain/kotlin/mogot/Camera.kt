package mogot

import mogot.math.*


class Camera : Spatial() {
    var enabled = false
    val projectionMatrix = Matrix4f()

    var width = 0
        private set
    var height = 0
        private set

    var near = 0.3f
        set(value) {
            field = value
            resize(width, height)
        }

    var fieldOfView = 60f
        set(value) {
            field = value
            resize(width, height)
        }

    var far = 1000f
        set(value) {
            field = value
            resize(width, height)
        }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.identity().setPerspective(
                Math.toRadians(fieldOfView.toDouble()).toFloat(),
                width.toFloat() / height.toFloat(),
                near,
                far, false)
    }

    fun applyMatrix(viewMatrix4f: Matrix4f) {
        /*
        parent?.currentToRoot {
            if (it.isSpatial) {
                it as Spatial
                viewMatrix4f.set(it.apply(viewMatrix4f))
            }
            true
        }
        */
        globalToLocalMatrix(viewMatrix4f)
        return
        this.asUpSequence().mapNotNull { it as? Spatial }.forEach {
            viewMatrix4f.set(it.apply(viewMatrix4f))
        }

        viewMatrix4f.rotateAffine(quaternion)
        viewMatrix4f.translate(-position)
    }


    fun worldToScreenPoint(position: Vector3fc): Vector2i? {
        val out = Vector2i()
        if (!worldToScreenPoint(position, out))
            return null
        return out
    }

    /**
     * Project [position] to screen of this camera. Result will push to [dest]
     *
     * @param position global position in world
     * @param dest result place holder
     */
    fun worldToScreenPoint(position: Vector3fc, dest: Vector2i): Boolean {
        val pos = TEMP_VEC_3F_1.set(position)
        globalToLocal(pos, pos)

        val proj = projectionMatrix
        pos.z = pos.x * proj.m03 + pos.y * proj.m13 + pos.z * proj.m23 + proj.m33
        if (pos.z < 0.1f)
            return false
        pos.x = pos.x * proj.m00 + pos.y * proj.m10 + pos.z * proj.m20 + proj.m30
        pos.y = pos.x * proj.m01 + pos.y * proj.m11 + pos.z * proj.m21 + proj.m31

        val ndc = TEMP_VEC_2F
        ndc.x = pos.x / pos.z
        ndc.y = pos.y / pos.z

        dest.x = ((width * 0.5f * ndc.x) + (ndc.x + width * 0.5f)).toInt()
        dest.y = (-(height * 0.5f * ndc.y) + (ndc.y + height * 0.5f)).toInt()
        return true
    }

    fun screenPointToRay(x: Int, y: Int, dest: MutableRay): MutableRay {
        val mat = Matrix4f()
        globalToLocalMatrix(mat)
        val matInvert = mat.invert(Matrix4f())
        matInvert.mul(projectionMatrix.invert(Matrix4f())).unprojectInvRay(
                x.toFloat(),
                (height - y).toFloat(),
                intArrayOf(0, 0, width, height),
                dest.position,
                dest.direction
        )
        dest.direction.normalize()
        return dest
    }
}

val TEMP_VEC_2F = Vector2f()
val TEMP_VEC_3F_1 = Vector3f()
val TEMP_VEC_3F_2 = Vector3f()

fun Vector4fc.toVector3f(dest: Vector3f): Vector3f {
    val w = 1.0f / w
    dest.set(x * w, y * w, z * w)
    return dest
}

fun Vector4fc.toVector3f(): Vector3f = toVector3f(Vector3f())