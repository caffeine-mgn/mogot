package mogot

import mogot.math.Math
import mogot.math.Matrix4f
import mogot.math.unaryMinus


class Camera : Spatial() {
    val projectionMatrix = Matrix4f()
    private var width = 0
    private var height = 0

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.identity().perspective(Math.toRadians(45.0).toFloat(), width.toFloat() / height.toFloat(), 0.1f, 1000f)
    }

    fun applyMatrix(viewMatrix4f: Matrix4f) {
        this.asUpSequence().mapNotNull { it as? Spatial }.forEach {
            viewMatrix4f.set(it.apply(viewMatrix4f))
        }
        viewMatrix4f.rotateAffine(quaternion)
        viewMatrix4f.translate(-position)
    }
}