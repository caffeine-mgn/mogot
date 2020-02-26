package mogot.physics.d2.shapes

import mogot.Engine
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector2fm
import mogot.math.mulXY
import mogot.physics.box2d.collision.shapes.PolygonShape
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.collision.shapes.getPoints
import mogot.physics.box2d.collision.shapes.setPoints
import mogot.physics.d2.PhysicsBody2D

class PolygonShape2D(engine: Engine) : Shape2D(engine) {

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

    override fun makeShape(): Shape {
        val s = PolygonShape()
//        s.setAsBox(0.5f, 0.5f)
        val points = vertex.map {
            it.mulXY(transform, Vector2f())
        }
        s.setPoints(points)
        return s
    }

    private val shape
        get() = fixture?.getShape() as PolygonShape?

    private fun updateShape() {
        val shape = shape
        if (shape != null) {
            val points = vertex.map {
                it.mulXY(transform, Vector2f())
            }
            shape.setPoints(points)
        }
    }

    override fun removeFromBody(body: PhysicsBody2D) {
        vertex = (fixture!!.getShape() as PolygonShape).getPoints()
        super.removeFromBody(body)
    }

    private var vertex: List<Vector2fc> = run {
        val size = 100f
        listOf(
                Vector2f(-size * 0.5f, -size * 0.5f),
                Vector2f(-size * 0.5f, size * 0.5f),
                Vector2f(size * 0.5f, size * 0.5f),
                Vector2f(size * 0.5f, -size * 0.5f)
        )
    }

    fun getVertex() = vertex

    fun setVertex(list: List<Vector2fc>) {
        vertex = list
        updateShape()
    }
}