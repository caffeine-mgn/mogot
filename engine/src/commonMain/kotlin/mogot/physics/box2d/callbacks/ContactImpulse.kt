package mogot.physics.box2d.callbacks

expect class ContactImpulse {
    var normalImpulses:FloatArray
    var tangentImpulses:FloatArray
    var count:Int
}
