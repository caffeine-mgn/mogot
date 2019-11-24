package mogot

import mogot.math.*

open class Spatial : Node() {
    val position = Vector3f()
    val quaternion = Quaternionf()
    val scale = Vector3f(1f, 1f, 1f)

    protected var _matrix = Matrix4f()

    val matrix: Matrix4fc
        get() = _matrix

    override fun apply(matrix: Matrix4fc): Matrix4fc {
        this._matrix.set(matrix)
        this._matrix.translate(position)
        this._matrix.rotate(quaternion)
        this._matrix.scale(scale)

        return this._matrix
    }

    fun lookTo(position: Vector3f) {
        quaternion.identity()
        quaternion.lookAlong(position - this.position, Vec3f.UP)
    }
}
