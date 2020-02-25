package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.common.Transform
import mogot.physics.box2d.common.Vec2

actual external class Body {
    actual fun createFixture(shape: Shape?, density: Float): Fixture
    actual fun createFixture(def: FixtureDef): Fixture
    actual fun destroyFixture(fixture: Fixture)
    actual fun getTransform(): Transform
    actual fun setTransform(position: Vec2, angle: Float)
    actual fun isFixedRotation(): Boolean
    actual fun setFixedRotation(value: Boolean)
    actual fun setLinearVelocity(v: Vec2)
    actual fun setAngularVelocity(w: Float)
    actual fun getLinearVelocity(): Vec2
    actual fun getAngularVelocity(): Float
    actual fun resetMassData()
    actual fun getMass(): Float
    actual fun setAwake(flag: Boolean)
    actual fun isAwake(): Boolean
    actual fun getLinearDamping(): Float
    actual fun setLinearDamping(linearDamping: Float)
    actual fun getAngularDamping(): Float
    actual fun setAngularDamping(angularDamping: Float)
    actual fun getGravityScale(): Float
    actual fun setGravityScale(gravityScale: Float)
}

actual fun Body.setType(type: BodyType) {
    this.asDynamic().setType(type.js)
}

actual fun Body.getType(): BodyType = this.asDynamic().getType().mogot

actual enum class BodyType {
    STATIC, KINEMATIC, DYNAMIC
}

val BodyType.js
    get() = when (this) {
        BodyType.STATIC -> js("planck.Body.STATIC")
        BodyType.KINEMATIC -> js("planck.Body.KINEMATIC")
        BodyType.DYNAMIC -> js("planck.Body.DYNAMIC")
    }

internal fun toMogotBodyType(enum: Any): BodyType {
    if (enum === js("planck.Body.STATIC"))
        return BodyType.STATIC
    if (enum === js("planck.Body.KINEMATIC"))
        return BodyType.KINEMATIC
    if (enum === js("planck.Body.DYNAMIC"))
        return BodyType.DYNAMIC
    throw RuntimeException()
}