package mogot.physics.d2.shapes

import mogot.Engine
import mogot.Node
import mogot.VisualInstance2D
import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.dynamics.Fixture
import mogot.physics.box2d.dynamics.FixtureDef
import mogot.physics.d2.PhysicsBody2D

abstract class Shape2D(engine: Engine) : VisualInstance2D(engine) {
    var density: Float = 1f
        set(value) {
            field = value
            fixture?.setDensity(value)
            body?.boxBody?.resetMassData()
            println("3 ${id} -> Mass: ${body?.boxBody?.getMass()}  $density")
        }
    var friction: Float = 0.5f
        set(value) {
            field = value
            fixture?.setFriction(value)
        }

    var restitution: Float = 0.2f
        set(value) {
            field = value
            fixture?.setRestitution(value)
        }


    protected var fixture: Fixture? = null

    var sensor = false
        set(value) {
            field = value
            fixture?.setSensor(value)
        }

    val body
        get() = parent as? PhysicsBody2D

    protected abstract fun makeShape(): Shape

    protected open fun removeFromBody(body: PhysicsBody2D) {
        fixture?.setUserData(null)
        fixture?.let { body.boxBody.destroyFixture(it) }
        fixture = null
    }

    private fun addToBody(body: PhysicsBody2D) {
        val def = FixtureDef()
        def.shape = makeShape()
        def.density = density
        def.friction = friction
        def.restitution = restitution
        def.isSensor = sensor
        def.userData = this
        fixture = body.boxBody.createFixture(def)
        body.boxBody.resetMassData()
        println("1 ${id} -> Mass: ${body.boxBody.getMass()}  $density")
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