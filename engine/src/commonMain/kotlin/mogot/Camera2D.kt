package mogot

import mogot.math.*

class Camera2D(engine: Engine) : Spatial2D(engine) {

    val projectionMatrix = Matrix4f()

    var width = 0
        private set
    var height = 0
        private set

    var zoom = 1f
        set(value) {
            if (value <= 0)
                throw IllegalArgumentException("Zoom can't be less or equal 0")
            if (value.isNaN())
                throw IllegalArgumentException("Zoom can't be NaN")
            if (value.isInfinite())
                throw IllegalArgumentException("Zoom can't be Infinite")
            field = value
            resize(width, height)
        }

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.setOrtho2D(-width * 0.5f * zoom, width * 0.5f * zoom, height * 0.5f * zoom, -height * 0.5f * zoom)
    }

    fun worldToScreen(point: Vector2fc, dest: Vector2im = Vector2i()) =
            worldToScreen(point.x, point.y, dest)

    fun worldToScreen(pointX: Float, pointY: Float, dest: Vector2im = Vector2i()): Vector2im {
        dest.x = (pointX + width * 0.5f - position.x).toInt()
        dest.y = (pointY + height * 0.5f - position.y).toInt()
        return dest
    }

    fun screenToWorld(point: Vector2ic, dest: Vector2fm) =
            screenToWorld(point.x, point.y, dest)

    fun screenToWorld(pointX: Int, pointY: Int, dest: Vector2fm): Vector2fm {
        dest.x = pointX.toFloat() - width * 0.5f + position.x
        dest.y = pointY.toFloat() - height * 0.5f + position.y
        return dest
    }

}