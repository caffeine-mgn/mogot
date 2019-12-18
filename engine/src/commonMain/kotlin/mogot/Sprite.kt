package mogot

import mogot.gl.GL
import mogot.math.Matrix4fc
import mogot.math.Vector2f

class Sprite(val gl: GL) : VisualInstance2D() {
    val size = Vector2f()
    private var geom = Rect2D(gl, size)
    var material: Material? = null
    private val oldSize = Vector2f(size)
    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (size != oldSize) {
            geom.setSize(size)
            oldSize.set(size)
        }
        super.render(model, projection, renderContext)
        val mat = material ?: return

        mat.use(model, projection, renderContext)
        geom.draw()
        mat.unuse()
    }
}