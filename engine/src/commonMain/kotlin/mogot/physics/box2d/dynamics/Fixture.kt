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
    fun isSensor(): Boolean
    fun setSensor(sensor: Boolean)
    /**
     * Get the coefficient of restitution.
     *
     * @return
     */
    fun getRestitution(): Float

    /**
     * Set the coefficient of restitution. This will _not_ change the restitution of existing
     * contacts.
     *
     * @param restitution
     */
    fun setRestitution(restitution: Float)
}