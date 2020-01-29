package mogot.physics.d2.shapes

import mogot.*
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.dynamics.Fixture
import mogot.physics.d2.PhysicsBody2D

abstract class Shape2D(engine: Engine) : VisualInstance2D(engine) {
    var density: Float = 0f
        set(value) {
            field = value
            fixture?.setDensity(value)
        }
    var friction: Float = 0.2f
        set(value) {
            field = value
            fixture?.setFriction(value)
        }
    protected var fixture: Fixture? = null
    val body
        get() = parent as? PhysicsBody2D

    protected abstract fun makeShape():Shape

    private fun removeFromBody(body: PhysicsBody2D) {
        fixture?.setUserData(null)
        fixture?.let { body.boxBody.destroyFixture(it) }
        fixture = null
        println("remove from body")
    }

    private fun addToBody(body: PhysicsBody2D) {
        fixture = body.boxBody.createFixture(makeShape(), density)
        fixture!!.setDensity(density)
        fixture!!.setFriction(friction)
        fixture!!.setUserData(this)
        println("add to body ${body.bodyType}")
    }

    override var parent: Node?
        get() = super.parent
        set(value) {
            if (super.parent == value)
                return
            (super.parent as? PhysicsBody2D)?.let { removeFromBody(it) }
            super.parent = value
            (value as? PhysicsBody2D)?.let { addToBody(it) }
        }

    override fun close() {
        super.close()
    }
}