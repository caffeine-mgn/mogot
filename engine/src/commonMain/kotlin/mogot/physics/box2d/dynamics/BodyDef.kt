package mogot.physics.box2d.dynamics

import mogot.physics.box2d.common.Vec2

expect class BodyDef {
    constructor()

    var position: Vec2
    var angle: Float
    var fixedRotation:Boolean
}

expect var BodyDef.type: BodyType