package mogot.physics.box2d.collision.shapes

import mogot.math.Vector2fc
import mogot.physics.box2d.mogotFormat
import mogot.physics.box2d.toBox2d

actual typealias PolygonShape = org.jbox2d.collision.shapes.PolygonShape

actual fun PolygonShape.getPoints(): List<Vector2fc> =
        this.vertices.map { it.mogotFormat }

actual fun PolygonShape.setPoints(list: List<Vector2fc>) {
    this.set(Array(list.size) { list[it].toBox2d }, list.size)
}