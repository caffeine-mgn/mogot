package mogot.physics.box2d.dynamics

import mogot.physics.box2d.callbacks.ContactListener
import mogot.physics.box2d.common.Vec2

expect class World {
    constructor(gravity: Vec2)

    fun createBody(bodyDef: BodyDef): Body
    /**
     * Take a time step. This performs collision detection, integration, and constraint solution.
     *
     * @param timeStep the amount of time to simulate, this should not vary.
     * @param velocityIterations for the velocity constraint solver.
     * @param positionIterations for the position constraint solver.
     */
    fun step(dt: Float, velocityIterations: Int, positionIterations: Int)

    /**
     * destroy a rigid body given a definition. No reference to the definition is retained. This
     * function is locked during callbacks.
     *
     * @warning This automatically deletes all associated shapes and joints.
     * @warning This function is locked during callbacks.
     * @param body
     */
    fun destroyBody(body: Body)

    fun setGravity(gravity: Vec2)
    fun getGravity(): Vec2

    fun setContactListener(listener: ContactListener?)
}