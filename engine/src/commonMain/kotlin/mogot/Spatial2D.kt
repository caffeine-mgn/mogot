package mogot

import mogot.math.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class Spatial2D(val engine: Engine) : Node() {
    private val p = Vector2fProperty()
    private val s = Vector2fProperty(1f, 1f)
    open val position: Vector2fm
        get() = p
    open val scale: Vector2fm
        get() = s
    open var rotation: Float = 0f
        set(value) {
            if (!rotationChanged && field != value)
                rotationChanged = true
            field = value
        }
    private var rotationChanged = false
    protected var _matrix = Matrix4f()
    private val _transform = Matrix4f()

    val transform: Matrix4fc
        get() {
            if (rotationChanged || p.resetChangeFlag() || s.resetChangeFlag()) {
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
                if (it.isSpatial2D())
                    return it
                true
            }
            return null
        }

    fun globalToLocal(point: Vector2fc, dest: Vector2fm = Vector2f()): Vector2fm {
        val mat = engine.mathPool.mat4f.poll()
        globalToLocalMatrix(mat)
        point.mulXY(mat, dest)
        engine.mathPool.mat4f.push(mat)
        return dest
    }

    fun localToGlobal(point: Vector2fc, dest: Vector2fm = Vector2f()): Vector2fm {
        val mat = engine.mathPool.mat4f.poll()
        localToGlobalMatrix(mat)
        point.mulXY(mat, dest)
        engine.mathPool.mat4f.push(mat)
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

    fun setGlobalTransform(matrix: Matrix4fc) {
        val m = parentSpatial2D?.globalToLocalMatrix(Matrix4f()) ?: Matrix4f()
        val tmp = engine.mathPool.vec3f.poll()
        m.mul(matrix, m)
        m.getTranslation(tmp)
        position.set(tmp.x, tmp.y)
        m.getScale(tmp)
        scale.set(tmp.x, tmp.y)
        val q = engine.mathPool.quatf.poll()
        q.setFromUnnormalized(m)
        q.getEulerAnglesXYZ(tmp)
        rotation = tmp.z
        engine.mathPool.quatf.push(q)
        engine.mathPool.vec3f.push(tmp)
    }

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
        this._matrix.translate(position.x, position.y, 0f)
        this._matrix.rotateZ(rotation)
        this._matrix.scale(scale.x, scale.y, 1f)

        return this._matrix
    }
}


private const val spatial2dType = SPATIAL2D_TYPE or VISUAL_INSTANCE2D_TYPE


@UseExperimental(ExperimentalContracts::class)
fun Node.isSpatial2D(): Boolean {
    contract {
        returns(true) implies (this@isSpatial2D is Spatial2D)
    }
    return (type and spatial2dType) != 0
}

fun <T : Node> Sequence<T>.onlySpatial2D(): Sequence<Spatial2D> =
        filter { it.isSpatial2D() }.map { it as Spatial2D }