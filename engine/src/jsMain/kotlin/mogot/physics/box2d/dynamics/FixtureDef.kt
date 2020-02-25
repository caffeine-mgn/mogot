package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape

actual external class FixtureDef{
    actual constructor()
    actual var shape: Shape
    actual var density:Float
    actual var friction:Float
    actual var isSensor: Boolean
    actual var restitution: Float
    actual var userData: Any?
}