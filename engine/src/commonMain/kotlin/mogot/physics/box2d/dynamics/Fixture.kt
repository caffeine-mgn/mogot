package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape

expect class Fixture {
    fun setDensity(density: Float)
    fun getDensity(): Float
    fun getFriction(): Float
    fun setFriction(friction: Float)
    fun getUserData(): Any?
    fun setUserData(data: Any?)
    fun getShape(): Shape
}