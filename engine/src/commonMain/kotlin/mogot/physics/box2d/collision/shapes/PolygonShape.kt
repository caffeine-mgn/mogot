package mogot.physics.box2d.collision.shapes

import mogot.math.Vector2fc
import mogot.physics.box2d.common.Vec2

expect class PolygonShape : Shape {
    fun setAsBox(hx: Float, hy: Float)
    fun setAsBox(hx: Float, hy: Float, center: Vec2, angle: Float)

    constructor()
}

expect fun PolygonShape.getPoints(): List<Vector2fc>

expect fun PolygonShape.setPoints(list: List<Vector2fc>)