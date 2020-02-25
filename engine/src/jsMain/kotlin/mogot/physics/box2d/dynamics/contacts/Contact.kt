package mogot.physics.box2d.dynamics.contacts

import mogot.physics.box2d.dynamics.Fixture

actual external abstract class Contact {
    actual open fun getFixtureA(): Fixture?
    actual open fun getFixtureB(): Fixture?
    actual open fun getTangentSpeed(): Float
    actual open fun isTouching(): Boolean
}