package mogot

import mogot.math.Vector3f
import mogot.math.Vector3fm

val Node.isDirectLight
    get() = (type and DIRECT_LIGHT_TYPE) != 0
val Node.isPointLight
    get() = (type and POINT_LIGHT_TYPE) != 0

abstract class Light : Spatial() {
    abstract var diffuse: Vector3fm
    abstract var specular: Float
}

open class PointLight : Light() {
    override var diffuse: Vector3fm = Vector3f(1f, 1f, 1f)
    override var specular: Float = 1f
    override val type: Int
        get() = POINT_LIGHT_TYPE
}

class DirectLight: Spatial() {
    var direction: Vector3fm = Vector3f(1f,1f,1f)
    override val type: Int
        get() = DIRECT_LIGHT_TYPE
}