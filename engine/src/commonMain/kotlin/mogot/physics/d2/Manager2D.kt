package mogot.physics.d2

import mogot.math.Vector2f
import mogot.math.Vector2fm
import mogot.physics.box2d.callbacks.ContactImpulse
import mogot.physics.box2d.callbacks.ContactListener
import mogot.physics.box2d.collision.Manifold
import mogot.physics.box2d.common.Vec2
import mogot.physics.box2d.dynamics.World
import mogot.physics.box2d.dynamics.contacts.Contact
import mogot.physics.d2.shapes.Shape2D
import pw.binom.io.Closeable

class Manager2D : Closeable {

    private inner class MyContactListener : ContactListener {
        override fun beginContact(contact: Contact) {
            val shape1 = contact.getFixtureA()!!.getUserData() as Shape2D
            val shape2 = contact.getFixtureB()!!.getUserData() as Shape2D
            shape1.behaviour?.let {
                this@Manager2D.contact._shapeA = shape1
                this@Manager2D.contact._shapeB = shape2
                it.onCollisionEnter2D(this@Manager2D.contact)
            }
            shape2.behaviour?.let {
                this@Manager2D.contact._shapeA = shape2
                this@Manager2D.contact._shapeB = shape1
                it.onCollisionEnter2D(this@Manager2D.contact)
            }
        }

        override fun endContact(contact: Contact) {
            val shape1 = contact.getFixtureA()!!.getUserData() as Shape2D
            val shape2 = contact.getFixtureB()!!.getUserData() as Shape2D
            shape1.behaviour?.let {
                this@Manager2D.contact._shapeA = shape1
                this@Manager2D.contact._shapeB = shape2
                it.onCollisionLeave2D(this@Manager2D.contact)
            }
            shape2.behaviour?.let {
                this@Manager2D.contact._shapeA = shape2
                this@Manager2D.contact._shapeB = shape1
                it.onCollisionLeave2D(this@Manager2D.contact)
            }
        }

        override fun preSolve(contact: Contact, oldManifold: Manifold) {
        }

        override fun postSolve(contact: Contact, impulse: ContactImpulse) {
        }
    }

    private class MyContact2D : Contact2D {
        var _shapeA: Shape2D? = null
        var _shapeB: Shape2D? = null
        override val shapeA: Shape2D
            get() = _shapeA!!
        override val shapeB: Shape2D
            get() = _shapeB!!
    }

    private val contact = MyContact2D()

    internal var world = World(Vec2(0f, 0f))
    private val contactListener = MyContactListener()

    init {
        world.setContactListener(contactListener)
    }

    val gravity: Vector2fm = Vector2fUpdater { x, y ->
        world.setGravity(Vec2(x, y))
    }

    fun step(dt: Float) {
        world.step(dt, 10, 8)
    }

    override fun close() {

    }
}

private class Vector2fUpdater(val updater: (Float, Float) -> Unit) : Vector2f() {
    override fun set(x: Float, y: Float): Vector2fm {
        if (super.x != x || super.y != y)
            updater(x, y)
        return super.set(x, y)
    }

    override var x: Float
        get() = super.x
        set(value) {
            if (super.x != value) {
                updater(value, super.y)
            }
            super.x = value
        }
    override var y: Float
        get() = super.y
        set(value) {
            if (super.y != value) {
                updater(super.x, value)
            }
            super.y = value
        }
}