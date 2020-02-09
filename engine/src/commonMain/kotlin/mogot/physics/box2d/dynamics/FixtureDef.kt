package mogot.physics.box2d.dynamics

import mogot.physics.box2d.collision.shapes.Shape

expect class FixtureDef{
    constructor()
    var shape:Shape
    var density:Float
    var friction:Float
}