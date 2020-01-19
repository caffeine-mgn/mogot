package mogot

import mogot.math.Matrix4f
import mogot.math.Vector2fm
import mogot.math.Vector2ic
import mogot.math.Vector2im

class Camera2D(engine: Engine): Spatial2D(engine) {

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

    fun screenToWorld(point: Vector2ic, dest: Vector2fm) =
            screenToWorld(point.x, point.y, dest)

    fun screenToWorld(pointX: Int, pointY: Int, dest: Vector2fm): Vector2fm {
        dest.x = pointX.toFloat() + position.x
        dest.y = pointY.toFloat() + position.y
        return dest
    }

}