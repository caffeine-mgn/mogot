package mogot.physics.d2

import mogot.Engine
import mogot.Spatial2D
import mogot.math.Vector2fProperty
import mogot.math.Vector2fm
import mogot.math.angle
import mogot.physics.box2d.common.Vec2
import mogot.physics.box2d.dynamics.*


class PhysicsBody2D(engine: Engine) : Spatial2D(engine) {
    val boxBody: Body

    var bodyType: BodyType
        get() {
            return boxBody.getType()
        }
        set(value) {
            boxBody.setType(value)
        }
    private val pos = Vec2()

    init {
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.STATIC
        boxBody = engine.physicsManager2D.world.createBody(bodyDef)
    }

    override val position: Vector2fm = object : Vector2fm {
        override fun set(x: Float, y: Float): Vector2fm {
            pos.x = x
            pos.y = y
            boxBody.setTransform(pos, rotation)
            return this
        }

        override var x: Float
            get() = boxBody.getTransform().p.x
            set(value) {
                set(value, y)
            }
        override var y: Float
            get() = boxBody.getTransform().p.y
            set(value) {
                set(x, value)
            }

    }

    private val tx = boxBody.getTransform()
    override fun update(delta: Float) {
        super.position.set(tx.p.x, tx.p.y)
        super.rotation = tx.q.getAngle()
        super.update(delta)
    }

    override fun close() {
        engine.physicsManager2D.world.destroyBody(boxBody)
        super.close()
    }
}