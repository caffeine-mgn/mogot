package mogot

import mogot.gl.PostEffectPipeline
import mogot.math.*


class Camera : Spatial() {
    var postEffectPipeline: PostEffectPipeline? = null
        set(value) {
            field = value
            resize(width,height)
        }
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
        postEffectPipeline?.let {
            it.close()
            it.init(width,height)
        }
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

    fun begin(){
        postEffectPipeline?.begin()
    }

    fun end(renderContext: RenderContext){
        postEffectPipeline?.end(renderContext)
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
        val clipCoords = pos//TEMP_VEC_3F_2


        val proj = projectionMatrix
        clipCoords.z = pos.x * proj.m03 + pos.y * proj.m13 + pos.z * proj.m23 + proj.m33
        if (clipCoords.z < 0.1f)
            return false
        clipCoords.x = pos.x * proj.m00 + pos.y * proj.m10 + pos.z * proj.m20 + proj.m30
        clipCoords.y = pos.x * proj.m01 + pos.y * proj.m11 + pos.z * proj.m21 + proj.m31

        val ndc = TEMP_VEC_2F
        ndc.x = clipCoords.x / clipCoords.z
        ndc.y = clipCoords.y / clipCoords.z

        dest.x = ((width.toFloat() / 2 * ndc.x) + (ndc.x + width.toFloat() / 2)).toInt()
        dest.y = (-(height.toFloat() / 2 * ndc.y) + (ndc.y + height.toFloat() / 2)).toInt()
        return true
    }

    fun screenPointToRay(x: Int, y: Int, dest: MutableRay): MutableRay {
        val mat = Matrix4f()
        globalToLocalMatrix(mat)
        val matInvert = mat.invert(Matrix4f())
        /*
        val mx = x.toFloat() / width * 2f - 1f
        val my = y.toFloat() / height * 2f - 1f
        val r = projectionMatrix//.mul(mat, Matrix4f()).invert(Matrix4f())
        //projectionMatrix.mul(mat, Matrix4f())//.invert(Matrix4f())
        r.unprojectInv(
                (width - x).toFloat(),
                (height - y).toFloat(),
                near,
                intArrayOf(0, 0, width, height),
                dest.direction
        )
        dest.direction.z = -far
        //dest.direction.set(mx, my, 0f).mul(projectionMatrix.invert(Matrix4f()))
        dest.position.set(0f, 0f, -near).mul(matInvert)//.mul(projectionMatrix)
        dest.direction.mul(matInvert)
        dest.direction.normalize()

//        mat.mul(projectionMatrix).unprojectRay(
//                x.toFloat(),
//                y.toFloat(),
//                intArrayOf(0, 0, width, height),
//                dest.position,
//                dest.direction
//        )
        */
        matInvert.mul(projectionMatrix.invert(Matrix4f())).unprojectInvRay(
                x.toFloat(),
                (height - y).toFloat(),
                intArrayOf(0, 0, width, height),
                dest.position,
                dest.direction
        )
        dest.direction.normalize()
        return dest
//        val v = Vector3f()
//        v.z = -near
//        v.mul(matInvert)
//        mat.getTranslation(TEMP_VEC_3F_1)
//        dest.direction.mul(mat).sub(TEMP_VEC_3F_1)
//        dest.direction.normalize()
//        return dest
//        r.unprojectRay(
//                x.toFloat(),
//                y.toFloat(),
//                intArrayOf(0, 0, width, height),
//                dest.position,
//                dest.direction
//        )


//        (projectionMatrix * mat).invert()
//                .unprojectInvRay(
//                        x.toFloat(),
//                        (height - y).toFloat(),
//                        intArrayOf(0, 0, width, height),
//                        dest.position,
//                        dest.direction
//                )
//        dest.direction.normalize()
//        return dest
//
//        println("$mx, $my")
//        val pp = Vector4f(mx, my, 1f, 1f).mul(projectionMatrix)
//        //pp.mul(mat)
//        pp.w = 1.0f / pp.w
//        pp.x *= pp.w
//        pp.y *= pp.w
//        pp.z *= pp.w
//        val ppp = Vector3f(pp.x, pp.y, pp.z).normalize()
//        println("pp=$ppp")


//        globalTransfrorm(mat)

//        val matProjection = mat * projectionMatrix
//        val matInverse = matProjection.invert(Matrix4f())
//        val winZ = 1.0f
//        val _in = Vector4f(
//                x.toFloat() / (width * 0.5f) - 1f,
//                y.toFloat() / (height * 0.5f) - 1f,
//                1f,
//                1f)
//        val pos = _in.mul(matInverse, Vector4f())
//        println("pos=$pos")
//
//        pos.w = 1.0f / pos.w;
//        pos.x *= pos.w
//        pos.y *= pos.w
//        pos.z *= pos.w
//        println("pos=${pos.x} ${pos.y}, ${pos.z}")
//        println("matProjection=$matInverse")
//        println("mat=$mat")

//        matInverse.unprojectRay(
//                x.toFloat(),
//                (height - y).toFloat(),
//                intArrayOf(0, 0, width, height),
//                dest.position,
//                dest.direction
//        )


//        globalTransfrorm(mat)
//        val m = projectionMatrix.mul(mat, Matrix4f())

//        m.unprojectRay(x.toFloat(), (height - y).toFloat(), intArrayOf(0, 0, width,height), dest.position, dest.direction)

//        mat.unprojectRay(
//                x.toFloat(),
//                (y).toFloat(),
//                intArrayOf(0, 0, width, height),
//                dest.position,
//                dest.direction
//        )
//        dest.direction.normalize()
//        return dest
    }

    override fun close() {
        super.close()
        postEffectPipeline?.dec()
        postEffectPipeline = null
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