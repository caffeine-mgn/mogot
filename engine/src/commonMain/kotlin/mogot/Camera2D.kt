package mogot

import mogot.math.Matrix4f

class Camera2D : Spatial2D() {

    val projectionMatrix = Matrix4f()

    var width = 0
        private set
    var height = 0
        private set

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.setOrtho2D(0f, width.toFloat(), height.toFloat(), 0f)
    }

}