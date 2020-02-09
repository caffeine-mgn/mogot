package mogot.physics.d2.shapes

import mogot.Engine
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.physics.box2d.collision.shapes.PolygonShape
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.collision.shapes.getPoints
import mogot.physics.box2d.collision.shapes.setPoints
import mogot.physics.d2.PhysicsBody2D

class PolygonShape2D(engine: Engine) : Shape2D(engine) {

    override fun makeShape(): Shape {
        val s = PolygonShape()
        s.setAsBox(0.5f, 0.5f)
        s.setPoints(vertex!!)
        return s
    }

    override fun removeFromBody(body: PhysicsBody2D) {
        vertex = (fixture!!.getShape() as PolygonShape).getPoints()
        super.removeFromBody(body)
    }

    private var vertex: List<Vector2fc>? = listOf(
            Vector2f(-0.5f, -0.5f),
            Vector2f(-0.5f, 0.5f),
            Vector2f(0.5f, 0.5f),
            Vector2f(0.5f, -0.5f)
    )

    fun getVertex() = vertex ?: (fixture!!.getShape() as PolygonShape).getPoints()

    fun setVertex(list: List<Vector2fc>) {
        val fixture = fixture
        if (fixture != null) {
            (fixture.getShape() as PolygonShape).setPoints(list)
        } else {
            vertex = list
        }
    }
}