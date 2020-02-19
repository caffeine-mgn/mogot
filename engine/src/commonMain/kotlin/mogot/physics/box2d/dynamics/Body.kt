package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.common.Transform
import mogot.physics.box2d.common.Vec2

expect class Body {
    fun createFixture(shape: Shape?, density: Float): Fixture
    fun createFixture(def: FixtureDef): Fixture
    fun destroyFixture(fixture: Fixture)
    fun getTransform(): Transform
    fun setTransform(position: Vec2, angle: Float)
    fun isFixedRotation(): Boolean
    fun setFixedRotation(value: Boolean)
    fun setLinearVelocity(v: Vec2)
    fun setAngularVelocity(w: Float)
    fun getLinearVelocity(): Vec2
    fun getAngularVelocity(): Float
}

expect fun Body.setType(type: BodyType)
expect fun Body.getType(): BodyType

expect enum class BodyType {
    STATIC, KINEMATIC, DYNAMIC
}
