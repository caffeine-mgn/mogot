package mogot.physics.d2.shapes

import mogot.Engine
import mogot.math.Vector2f
import mogot.math.Vector2fm
import mogot.physics.box2d.collision.shapes.CircleShape
import mogot.physics.box2d.collision.shapes.Shape

class CircleShape2D(engine: Engine) : Shape2D(engine) {
    var radius = 50f
        set(value) {
            field = value
            shape?.setRadius(value)
        }

    private val shape
        get() = fixture?.getShape() as CircleShape?

    override val position: Vector2fm = object : Vector2f(0f, 0f) {
        override var x: Float
            get() = shape?.m_p?.x ?: super.x
            set(value) {
                super.x = value
                shape?.m_p?.x = value
            }
        override var y: Float
            get() = shape?.m_p?.y ?: super.y
            set(value) {
                super.y = value
                shape?.m_p?.y = value
            }

        override fun set(x: Float, y: Float): Vector2fm {
            super.set(x, y)
            shape?.m_p?.x = x
            shape?.m_p?.y = y
            return this
        }
    }

    override fun makeShape(): Shape {
        val s = CircleShape()
        s.m_p.x = position.x
        s.m_p.y = position.y
        s.setRadius(radius)
        return s
    }
}