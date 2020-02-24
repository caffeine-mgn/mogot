package mogot.physics.box2d.collision.shapes

import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.physics.box2d.common.Vec2

actual external class PolygonShape : Shape {
    actual fun setAsBox(hx: Float, hy: Float)

    actual constructor()

    actual fun setAsBox(hx: Float, hy: Float, center: Vec2, angle: Float)
}

actual fun PolygonShape.getPoints(): List<Vector2fc> =
        this.asDynamic().unsafeCast<Array<Vec2>>().map { Vector2f(it.x, it.y) }

actual fun PolygonShape.setPoints(list: List<Vector2fc>) {
    this.asDynamic()._set(Array(list.size) {
        val vec = Vec2()
        val item = list[it]
        vec.x = item.x
        vec.y = item.y
        vec
    }, list.size)
}