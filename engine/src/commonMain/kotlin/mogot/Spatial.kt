package mogot

import mogot.math.*

val Node.isSpatial
    get() = (type and 0x1) > 0

open class Spatial : Node() {

    override val type: Int
        get() = 0x1

    private var updateMatrix = false
    private val tmpMatrix = Matrix4f()

    fun localToGlobal(point: Vector3fc, dest: Vector3fm): Vector3fm {
        localToGlobalMatrix(tmpMatrix)
        point.mul(tmpMatrix, dest)
        return dest
    }

    fun globalToLocal(point: Vector3fc, dest: Vector3fm): Vector3fm {
        globalToLocalMatrix(tmpMatrix)
        point.mul(tmpMatrix, dest)
        return dest
    }

    fun globalToLocalMatrix(dest: Matrix4f): Matrix4f {
        dest.identity()
        parent?.currentToRoot {
            if (it.isSpatial) {
                it as Spatial
                dest.set(it.apply(dest))
            }
            true
        }

//        dest.translationRotateScale(
//                        position.x, position.y, position.z,
//                        quaternion.x, quaternion.y, quaternion.z, quaternion.w,
//                        scale.x, scale.y, scale.z
//                )

        dest.scale(scale)
        dest.rotate(-quaternion)
        dest.translate(-position)
        return dest
    }

    fun localToGlobalMatrix(dest: Matrix4f): Matrix4f {
        val parent = parentSpatial?.transform
        if (parent == null)
            dest.set(transform)
        else {
            dest.set(parent)
            dest.mul(transform, dest)
        }
        return dest
    }

    private inner class Vector3fWithChangeCounter(x: Float, y: Float, z: Float) : Vector3f(x, y, z) {

        override var x: Float
            get() = super.x
            set(value) {
                super.x = value
                updateMatrix = true
            }

        override var y: Float
            get() = super.y
            set(value) {
                super.y = value
                updateMatrix = true
            }

        override var z: Float
            get() = super.z
            set(value) {
                super.z = value
                updateMatrix = true
            }
    }

    private inner class QuaternionfProperty : Quaternionf() {
        override var x: Float
            get() = super.x
            set(value) {
                super.x = value
                updateMatrix = true
            }

        override var y: Float
            get() = super.y
            set(value) {
                super.y = value
                updateMatrix = true
            }

        override var z: Float
            get() = super.z
            set(value) {
                super.z = value
                updateMatrix = true
            }

        override var w: Float
            get() = super.w
            set(value) {
                super.w = value
                updateMatrix = true
            }
    }

    val position: Vector3fm = Vector3fWithChangeCounter(0f, 0f, 0f)
    val quaternion: Quaternionfm = QuaternionfProperty()
    val scale: Vector3fm = Vector3fWithChangeCounter(1f, 1f, 1f)
    private val _transform = Matrix4f()

    val parentSpatial: Spatial?
        get() {
            val parent = parent ?: return null
            parent.currentToRoot {
                if (it.isSpatial)
                    return it as Spatial
                true
            }
            return null
        }

    val transform: Matrix4fc
        get() {
            if (updateMatrix) {
                _transform
                        .translationRotateScale(
                                position.x, position.y, position.z,
                                quaternion.x, quaternion.y, quaternion.z, quaternion.w,
                                scale.x, scale.y, scale.z
                        )
                updateMatrix = false
            }
            return _transform
        }

    protected var _matrix = Matrix4f()

    open val matrix: Matrix4fc
        get() = _matrix

//    fun globalMatrix(dest: Matrix4f) {
//        dest.identity()
//        apply2(dest)
//        asUpSequence().mapNotNull { it as? Spatial }.forEach {
//            dest.set(it.apply2(dest))
//        }
//    }

//    private fun apply2(matrix: Matrix4f): Matrix4fc {
//        matrix.translate(position)
//        matrix.rotate(quaternion)
//        matrix.scale(scale)
//        return matrix
//    }

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
        this._matrix.translate(position)
        this._matrix.rotate(quaternion)
        this._matrix.scale(scale)

        return this._matrix
    }

    fun lookTo(position: Vector3fc, up: Vector3fc = Vector3fc.UP) {
        quaternion.identity()
//        val v = localToGlobal(Vector3f(),Vector3f())
        val v = globalToLocal(position, Vector3f())

//        position.sub(v,v)
        //v.negate()
        //println("lookTo($v)")
//        val g = localToGlobalMatrix(Matrix4f())
//        val v = g.getTranslation(Vector3f())
//        quaternion.lookAlong(position - v, up)
        //v.negate()
        quaternion.lookAt(v,up)
        //quaternion.lookAlong(v, up)
    }
}
