package mogot

import mogot.math.*

open class Spatial2D : Node() {
    val position = Vector2fProperty()
    val scale = Vector2fProperty(1f, 1f)
    var rotation: Float = 0f
        set(value) {
            if (!rotationChanged && field != value)
                rotationChanged = true
            field = value
        }
    private var rotationChanged = false
    protected var _matrix = Matrix4f()
    private val _transform = Matrix4f()
    private val tmpMatrix = Matrix4f()

    val transform: Matrix4fc
        get() {
            if (rotationChanged || position.resetChangeFlag() || scale.resetChangeFlag()) {
                _transform.identity().translate(position.x, position.y, 0f)
                        .rotateZ(rotation)
                        .scale(scale.x, scale.y, 1f)
                rotationChanged = false
            }
            return _transform
        }

    override val type: Int
        get() = SPATIAL2D_TYPE

    val matrix: Matrix4fc
        get() = _matrix

    val parentSpatial2D: Spatial2D?
        get() {
            val parent = parent ?: return null
            parent.currentToRoot {
                if (it.isSpatial2D)
                    return it as Spatial2D
                true
            }
            return null
        }

    fun globalToLocal(point: Vector3fc, dest: Vector3fm): Vector3fm {
        globalToLocalMatrix(tmpMatrix)
        point.mul(tmpMatrix, dest)
        return dest
    }

    fun localToGlobalMatrix(dest: Matrix4f): Matrix4f {
        val parent = parentSpatial2D?.transform
        if (parent == null)
            dest.set(transform)
        else {
            dest.set(parent)
            dest.mul(transform, dest)
        }
        return dest
    }

    fun globalToLocalMatrix(dest: Matrix4f): Matrix4f {
        localToGlobalMatrix(dest)
        dest.invert(dest)
        return dest
    }

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
        this._matrix.translate(position.x, position.y, 0f)
        this._matrix.rotateZ(rotation)
        this._matrix.scale(scale.x, scale.y, 1f)

        return this._matrix
    }
}

val Node.isSpatial2D
    get() = (type and SPATIAL2D_TYPE) != 0

fun <T : Node> Sequence<T>.onlySpatial2D(): Sequence<Spatial2D> =
        filter { it.isSpatial2D }.map { it as Spatial2D }