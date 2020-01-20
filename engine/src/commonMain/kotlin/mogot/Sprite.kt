package mogot

import mogot.math.Matrix4fc
import mogot.math.Vector2f
import mogot.math.set

open class Sprite(engine: Engine) : VisualInstance2D(engine), MaterialNode by MaterialNodeImpl() {
    val size = Vector2f()
    private var geom by ResourceHolder<Rect2D>()

    private val oldSize = Vector2f(size)
    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!visible)
            return
        if (geom == null) {
            geom = Rect2D(engine.gl, size)
        }

        if (size != oldSize) {
            geom!!.setSize(size)
            oldSize.set(size)
        }
        super.render(model, projection, renderContext)
        val mat = material.value ?: return

        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
    }

    override fun close() {
        material.dispose()
        geom = null
        super.close()
    }
}