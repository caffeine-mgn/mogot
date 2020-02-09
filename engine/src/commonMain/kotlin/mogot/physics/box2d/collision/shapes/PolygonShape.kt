package mogot.physics.box2d.collision.shapes

import mogot.math.Vector2fc

expect class PolygonShape : Shape {
    fun setAsBox(hx: Float, hy: Float)

    constructor()
}

expect fun PolygonShape.getPoints(): List<Vector2fc>

expect fun PolygonShape.setPoints(list: List<Vector2fc>)