package mogot.physics.box2d.dynamics.contacts

import mogot.physics.box2d.dynamics.Fixture

expect abstract class Contact {
    open fun getFixtureA(): Fixture?
    open fun getFixtureB(): Fixture?
    open fun getTangentSpeed(): Float
    open fun isTouching(): Boolean
}