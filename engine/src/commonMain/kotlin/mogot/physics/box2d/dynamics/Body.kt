package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape
import mogot.physics.box2d.common.Transform
import mogot.physics.box2d.common.Vec2
import kotlin.jvm.JvmName

expect class Body {
    fun createFixture(shape: Shape?, density: Float): Fixture
    fun createFixture(def: FixtureDef): Fixture
    fun destroyFixture(fixture: Fixture)
    fun getTransform(): Transform
    fun setTransform(position: Vec2, angle: Float)
    fun isFixedRotation():Boolean
    fun setFixedRotation(value:Boolean)
}

expect fun Body.setType(type: BodyType)
expect fun Body.getType(): BodyType

expect enum class BodyType {
    STATIC, KINEMATIC, DYNAMIC
}
