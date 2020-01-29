package mogot.physics.d2.shapes

import mogot.*
import mogot.math.*
import mogot.physics.box2d.collision.shapes.PolygonShape
import mogot.physics.box2d.collision.shapes.Shape

class BoxShape2D(engine: Engine) : Shape2D(engine), MaterialNode by MaterialNodeImpl() {
    private var rect by ResourceHolder<Rect2D>()
    val size = Vector2fProperty()

    override fun makeShape(): Shape {
        val s = PolygonShape()
        s.setAsBox(size.x * 0.5f, size.y * 0.5f)
        return s
    }

    private val shape
        get() = fixture?.getShape() as PolygonShape?

    override fun close() {
        material.dispose()
        rect = null
        super.close()
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (rect == null)
            rect = Rect2D(engine.gl, null)
        if (size.resetChangeFlag()) {
            rect!!.size.set(size)
            shape?.setAsBox(size.x * 0.5f, size.y * 0.5f)
            println("Reset size to ${size.x} x ${size.y}")
        }
        val mat = material.value ?: return
        mat.use(model, projection, renderContext)
        rect!!.draw()
        mat.unuse()
    }
}