package mogot.physics.box2d

import mogot.math.Vector2fc
import mogot.math.Vector2fm
import mogot.physics.box2d.dynamics.BodyType
import org.jbox2d.common.Vec2

val org.jbox2d.dynamics.BodyType.mogot
    get() = when (this) {
        org.jbox2d.dynamics.BodyType.DYNAMIC -> BodyType.DYNAMIC
        org.jbox2d.dynamics.BodyType.STATIC -> BodyType.STATIC
        org.jbox2d.dynamics.BodyType.KINEMATIC -> BodyType.KINEMATIC
    }

val BodyType.box2d
    get() = when (this) {
        BodyType.DYNAMIC -> org.jbox2d.dynamics.BodyType.DYNAMIC
        BodyType.STATIC -> org.jbox2d.dynamics.BodyType.STATIC
        BodyType.KINEMATIC -> org.jbox2d.dynamics.BodyType.KINEMATIC
    }

inline class Box2DVector2f(val box2d: Vec2) : Vector2fm {
    override var x: Float
        get() = box2d.x
        set(value) {
            box2d.x = value
        }
    override var y: Float
        get() = box2d.y
        set(value) {
            box2d.y = value
        }
}

inline val Vec2.mogotFormat
    get() = Box2DVector2f(this)

inline val Vector2fc.toBox2d
    get() = Vec2(x, y)