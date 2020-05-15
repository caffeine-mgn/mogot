package mogot.physics.d2.shapes

import mogot.*
import mogot.math.*
import mogot.physics.box2d.collision.shapes.PolygonShape
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.common.Vec2
import mogot.rendering.Display

class BoxShape2D(engine: Engine) : Shape2D(engine), MaterialNode by MaterialNodeImpl() {

    override val position: Vector2fm = object : Vector2f(0f, 0f) {
        override var x: Float
            get() = super.x
            set(value) {
                super.x = value
                updateShape()
            }
        override var y: Float
            get() = super.y
            set(value) {
                super.y = value
                updateShape()
            }

        override fun set(x: Float, y: Float): Vector2fm {
            super.set(x, y)
            updateShape()
            return this
        }
    }

    override var rotation: Float
        get() = super.rotation
        set(value) {
            super.rotation = value
            updateShape()
        }

    private fun updateShape() {
        shape?.setAsBox(size.x * 0.5f, size.y * 0.5f, Vec2(position.x, position.y), rotation)
    }

    private var rect by ResourceHolder<Rect2D>()
    val size = Vector2fProperty()

    override fun makeShape(): Shape {
        val s = PolygonShape()
        s.setAsBox(size.x * 0.5f, size.y * 0.5f, Vec2(position.x, position.y), rotation)
        return s
    }

    private val shape
        get() = fixture?.getShape() as PolygonShape?

    override fun close() {
        material.dispose()
        rect = null
        super.close()
    }

    override fun render(model: Matrix4fc, modelView:Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (rect == null)
            rect = Rect2D(engine.gl, null)
        if (size.resetChangeFlag()) {
            rect!!.size.set(size)
            shape?.setAsBox(size.x * 0.5f, size.y * 0.5f, Vec2(position.x, position.y), rotation)
            println("Reset size to ${size.x} x ${size.y}")
        }
        val mat = material.value ?: return
        mat.use(model, modelView, projection, context)
        rect!!.draw()
        mat.unuse()
    }
}