package mogot

import mogot.math.*
import kotlin.math.roundToInt

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

    private inline val zoomScale
        get() = 1 / zoom

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.setOrtho2D(-width * 0.5f * zoomScale, width * 0.5f * zoomScale, height * 0.5f * zoomScale, -height * 0.5f * zoomScale)
    }

    fun worldToScreen(point: Vector2fc, dest: Vector2im = Vector2i()) =
            worldToScreen(point.x, point.y, dest)

    fun worldToScreen(pointX: Float, pointY: Float, dest: Vector2im = Vector2i()): Vector2im {
        val pos = Vector2f(
                pointX,
                pointY
        )
        globalToLocal(pos, pos)
        pos.x = pos.x * zoom + width * 0.5f
        pos.y = pos.y * zoom + height * 0.5f
        dest.set(
                pos.x.roundToInt(),
                pos.y.roundToInt()
        )
        return dest
    }

    fun screenToWorld(point: Vector2ic, dest: Vector2fm = Vector2f()) =
            screenToWorld(point.x, point.y, dest)

    fun screenToWorld(pointX: Int, pointY: Int, dest: Vector2fm = Vector2f()): Vector2fm {
        val pos = Vector2f((pointX - width * 0.5f) * zoomScale, (pointY - height * 0.5f) * zoomScale)
        localToGlobal(pos, dest)
//        dest.x = (pos.x * zoom - width * 0.5f * zoom)
//        dest.y = (pos.y * zoom - height * 0.5f * zoom)
//        dest.x = pointX.toFloat() - width * 0.5f + position.x
//        dest.y = pointY.toFloat() - height * 0.5f + position.y
        return dest
    }
}