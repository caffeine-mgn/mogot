package mogot

import mogot.math.Vector3f
import mogot.math.*
import mogot.math.Vector3fm

val Node.isDirectLight
    get() = (type and DIRECT_LIGHT_TYPE) != 0
val Node.isPointLight
    get() = (type and POINT_LIGHT_TYPE) != 0

object LightSpecularField : AbstractField<Light, Float>() {
    override val name: String
        get() = "specular"
    override val type: Field.Type
        get() = Field.Type.FLOAT

    override suspend fun setValue(engine: Engine, node: Light, value: Float) {
        node.specular = value
    }

    override fun currentValue(node: Light): Float = node.specular
}

object LightDiffuseField : AbstractField<Light, Vector3fc>() {
    override val name: String
        get() = "diffuse"
    override val type: Field.Type
        get() = Field.Type.VEC3

    override suspend fun setValue(engine: Engine, node: Light, value: Vector3fc) {
        node.diffuse.set(value)
    }

    override fun currentValue(node: Light): Vector3fc = node.diffuse

}

abstract class Light : Spatial() {
    abstract var diffuse: Vector3fm
    abstract var specular: Float

    override fun getField(name: String): Field? =
            when (name) {
                LightSpecularField.name -> LightSpecularField
                LightDiffuseField.name -> LightDiffuseField
                else -> super.getField(name)
            }
}

open class PointLight(engine: Engine) : Light() {
    override var diffuse: Vector3fm = Vector3f(1f, 1f, 1f)
    override var specular: Float = 1f
    override val type: Int
        get() = POINT_LIGHT_TYPE
}

class DirectLight : Spatial() {
    var direction: Vector3fm = Vector3f(1f, 1f, 1f)
    override val type: Int
        get() = DIRECT_LIGHT_TYPE
}