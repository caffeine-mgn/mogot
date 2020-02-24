package mogot.physics.d2.shapes

import mogot.Engine
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.mulXY
import mogot.physics.box2d.collision.shapes.PolygonShape
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.collision.shapes.getPoints
import mogot.physics.box2d.collision.shapes.setPoints
import mogot.physics.d2.PhysicsBody2D

class PolygonShape2D(engine: Engine) : Shape2D(engine) {

    override fun makeShape(): Shape {
        val s = PolygonShape()
//        s.setAsBox(0.5f, 0.5f)
        val points = vertex.map {
            it.mulXY(transform, Vector2f())
        }
        s.setPoints(points)
        return s
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
        val fixture = fixture
        if (fixture != null) {
            val points = vertex.map {
                it.mulXY(transform, Vector2f())
            }
            (fixture.getShape() as PolygonShape).setPoints(points)
        }
    }
}