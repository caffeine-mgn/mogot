package mogot

import mogot.math.Vector3f
import mogot.math.Vector3fm

abstract class Light : Spatial() {
    abstract var diffuse: Vector3fm
    abstract var specular: Float
}

class PointLight : Light() {
    override var diffuse: Vector3fm = Vector3f(1f, 1f, 1f)
    override var specular: Float = 1f
}