package pw.binom.sceneEditor

import mogot.gl.GL
import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector2f

class Line2D(val gl: GL) : VisualInstance2D() {
    val size = Vector2f()
    private var geom by ResourceHolder<Geom2D>()
    var material by ResourceHolder<Material>()
    val lineTo = Vector2fProperty()

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!visible)
            return

        val mat = material ?: return
        if (geom == null) {
            lineTo.resetChangeFlag()
            geom = Geom2D(gl, intArrayOf(0, 1), floatArrayOf(0f, 0f, lineTo.x, lineTo.y), null, null)
            geom!!.mode = Geometry.RenderMode.LINES
        }
        if (lineTo.resetChangeFlag()) {
            geom!!.vertexBuffer.uploadArray(floatArrayOf(0f, 0f, lineTo.x, lineTo.y))
        }

        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
        println("render! $position -> $lineTo")
    }

    override fun close() {
        geom = null
        material = null
        super.close()
    }
}