package mogot

import mogot.math.*
import kotlin.test.Test


class Camera2DTest {

    @Test
    fun worldToScreenCenter() {
        val cam = Camera2D(mockEngine(800, 600))
        cam.resize()
        cam.worldToScreen(Vector2f(0f, 0f)).also {
            it.x.eq(400)
            it.y.eq(300)
        }
        cam.zoom = 2f
        cam.worldToScreen(Vector2f(0f, 0f)).also {
            it.x.eq(400)
            it.y.eq(300)
        }
    }

    @Test
    fun worldToScreen() {
        val cam = Camera2D(mockEngine(800, 600))
        cam.resize()
        cam.worldToScreen(Vector2f(-400f, -300f)).also {
            it.x.eq(0)
            it.y.eq(0)
        }
        cam.zoom = 2f
        cam.worldToScreen(Vector2f(-200f, -150f)).also {
            it.x.eq(0)
            it.y.eq(0)
        }
        cam.position.set(10f, 10f)
        cam.worldToScreen(Vector2f(-200f, -150f)).also {
            it.x.eq(10)
            it.y.eq(10)
        }
    }

    @Test
    fun screenToWorld() {
        val cam = Camera2D(mockEngine(800, 600))
        cam.resize()
        cam.screenToWorld(0, 0).also {
            it.x.eq(-400f)
            it.y.eq(-300f)
        }
        cam.screenToWorld(400, 300).also {
            it.x.eq(0f)
            it.y.eq(0f)
        }
        cam.zoom = 2f
        cam.screenToWorld(0, 0).also {
            it.x.eq(-200f)
            it.y.eq(-150f)
        }
        cam.position.set(10f, 10f)
        cam.screenToWorld(0, 0).also {
            it.x.eq(-200f + 10f)
            it.y.eq(-150f + 10f)
        }
    }
}