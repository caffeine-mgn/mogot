package mogot

import mogot.math.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class Spatial : Node() {

    override val type: Int
        get() = SPATIAL_TYPE

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
        localToGlobalMatrix(dest)
        dest.invert(dest)
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

    open val position: Vector3fm = Vector3fWithChangeCounter(0f, 0f, 0f)
    open val quaternion: Quaternionfm = QuaternionfProperty()
    open val scale: Vector3fm = Vector3fWithChangeCounter(1f, 1f, 1f)
    private val _transform = Matrix4f()

    fun setGlobalTransform(matrix: Matrix4fc) {
        val m = parentSpatial?.globalToLocalMatrix(Matrix4f()) ?: Matrix4f()
        m.mul(matrix, m)
        m.getTranslation(position)
        m.getScale(scale)
        quaternion.setFromUnnormalized(m)
    }

    val parentSpatial: Spatial?
        get() {
            val parent = parent ?: return null
            parent.currentToRoot {
                if (it.isSpatial())
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
                                position,
                                quaternion,
                                scale
                        )
                updateMatrix = false
            }
            return _transform
        }

    protected var _matrix = Matrix4f()

    open val matrix: Matrix4fc
        get() = _matrix

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
                //.translationRotateScale(position,quaternion,scale)
                .translate(position)
                .rotate(quaternion)
                .scale(scale)
        //this._matrix.translationRotateScale(position, quaternion, scale)
        return this._matrix
    }

    fun lookTo(position: Vector3fc, up: Vector3fc = Vector3fc.UP) {
        quaternion.identity()
        val localPosition = globalToLocal(position, Vector3f())
        quaternion.lookAt(localPosition, up)
    }
}

private const val spatial3dType = SPATIAL_TYPE or VISUAL_INSTANCE3D_TYPE

@UseExperimental(ExperimentalContracts::class)
fun Node.isSpatial(): Boolean {
    contract {
        returns(true) implies (this@isSpatial is Spatial)
    }
    return (type and spatial3dType) != 0
}

fun <T : Node> Sequence<T>.onlySpatial(): Sequence<Spatial> =
        filter { it.isSpatial() }.map { it as Spatial }