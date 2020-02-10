package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape

actual external class Fixture {
    actual fun setDensity(density: Float)
    actual fun getDensity(): Float
    actual fun getFriction(): Float
    actual fun setFriction(friction: Float)
    actual fun getUserData(): Any?
    actual fun setUserData(data: Any?)
    actual fun getShape(): Shape
}