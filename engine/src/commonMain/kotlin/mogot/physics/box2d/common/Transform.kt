package mogot.physics.box2d.common

import kotlin.jvm.JvmField

expect class Transform{
    @JvmField
    val q:Rot

    @JvmField
    val p:Vec2
}