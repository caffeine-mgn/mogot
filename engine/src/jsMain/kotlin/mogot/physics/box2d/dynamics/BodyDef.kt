package mogot.physics.box2d.dynamics

import mogot.physics.box2d.common.Vec2

actual external class BodyDef {
    actual constructor()

    actual var position: Vec2
    actual var angle: Float
    actual var fixedRotation: Boolean
    actual var gravityScale: Float
}

actual var BodyDef.type: BodyType
    get() = toMogotBodyType(this.asDynamic().type)
    set(value) {
        this.asDynamic().type = value.js
    }