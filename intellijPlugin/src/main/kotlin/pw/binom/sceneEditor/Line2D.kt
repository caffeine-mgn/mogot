package pw.binom.sceneEditor

import mogot.gl.GL
import mogot.*
import mogot.math.Matrix4fc
import mogot.math.Vector2f
import pw.binom.FloatDataBuffer
import pw.binom.floatDataOf
import pw.binom.intDataOf

class Line2D(val gl: GL) : VisualInstance2D() {
    val size = Vector2f()
    private var geom by ResourceHolder<Geom2D>()
    var material by ResourceHolder<Material>()
    val lineTo = Vector2fProperty()
    private val data = FloatDataBuffer.alloc(4)

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!visible)
            return

        data[0] = 0f
        data[1] = 0f
        data[2] = lineTo.x
        data[3] = lineTo.y

        val mat = material ?: return
        if (geom == null) {
            lineTo.resetChangeFlag()
            geom = Geom2D(gl, intDataOf(0, 1), data, null, null)
            geom!!.mode = Geometry.RenderMode.LINES
        }
        if (lineTo.resetChangeFlag()) {
            geom!!.vertexBuffer.uploadArray(data)
        }

        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
    }

    override fun close() {
        super.close()
        data.close()
        geom = null
        material = null
    }
}