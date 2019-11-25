package mogot.gl

import mogot.Stage
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window

abstract class AbstractGLView : Stage {
    val canvas = document.createElement("canvas").unsafeCast<HTMLCanvasElement>()
    private val context = canvas.getContext("webgl2").unsafeCast<WebGL2RenderingContext>()
    override val gl: GL = GL(context)

    init {
        canvas.tabIndex = 1
    }

    private val drawFunc: (Double) -> Unit = {
        drawOp()
    }

    private var oldWidth = 0.0
    private var oldHeight = 0.0
    protected open fun setup(width: Int, height: Int) {

    }

    private fun drawOp() {
        val r = canvas.getBoundingClientRect()
        if (r.width != oldWidth || r.height != oldHeight) {
            oldWidth = r.width
            oldHeight = r.height
            setup(oldWidth.toInt(), oldHeight.toInt())
        }
        draw()
        window.requestAnimationFrame(drawFunc)
    }

    protected open fun init() {

    }

    protected open fun dispose() {

    }

    protected open fun draw() {
    }

    fun startDraw() {
        canvas.focus()
        init()
        drawOp()
    }
}