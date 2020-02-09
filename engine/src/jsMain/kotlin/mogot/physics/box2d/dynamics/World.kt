package mogot.physics.box2d.dynamics

import mogot.physics.box2d.common.Vec2

actual external class World {
    actual constructor(gravity: Vec2)

    actual fun createBody(bodyDef: BodyDef): Body
    actual fun step(dt: Float, velocityIterations: Int, positionIterations: Int)
    actual fun destroyBody(body: Body)
    actual fun setGravity(gravity: Vec2)
    actual fun getGravity(): Vec2
}