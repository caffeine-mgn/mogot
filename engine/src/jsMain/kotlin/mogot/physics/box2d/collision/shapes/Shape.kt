package mogot.physics.box2d.collision.shapes

@JsModule("planck")
@JsNonModule
actual external abstract class Shape {
    actual fun getRadius(): Float
    actual fun setRadius(radius: Float)
}