package mogot

import mogot.annotation.ComponentNode
import mogot.math.Vector3f
import mogot.math.Vector3fc

abstract class PointLight : Spatial() {
    abstract var diffuse: Vector3fc
    abstract var specular: Float
}

@ComponentNode(displayName = "OmniLight", description = "Point Light")
class OmniLight : PointLight() {
    override var diffuse: Vector3fc = Vector3f(1f, 1f, 1f)
    override var specular: Float = 1f
}