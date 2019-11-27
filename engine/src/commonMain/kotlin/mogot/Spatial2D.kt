package mogot

import mogot.math.Math
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector2f

open class Spatial2D:Node(){
    val position = Vector2f()
    val scale = Vector2f(1f,1f)
    var rotation: Float = 0f
    protected var _matrix = Matrix4f()

    val matrix: Matrix4fc
        get() = _matrix

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
        this._matrix.rotateZ(rotation / 180f * Math.PI.toFloat())
        this._matrix.translate(position.x, position.y, 0f)
        this._matrix.scale(scale.x, scale.y, 1f)

        return this._matrix
    }
}