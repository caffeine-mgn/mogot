package game

import mogot.*
import mogot.gl.GL
import mogot.gl.GLView
import mogot.gl.WebGL2RenderingContext
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window



class GameView : GLView() {
    override var camera: Camera? = null
    lateinit var game: TestGame
    override fun init() {
        backgroundColor.set(0f, 0.5f, 0.7f, 1f)
        super.init()
        game = TestGame(engine)
        camera = game.camera
    }
}

fun main() {
    val view = GameView()
    view.canvas.apply {
        width = 800
        height = 600
    }
    document.body!!.appendChild(view.canvas)
    view.startDraw()
}