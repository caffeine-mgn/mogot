package mogot

import mogot.material.ExternalMaterialGLSL
import mogot.material.MaterialInstance
import mogot.material.loadMaterial
import mogot.math.Matrix4fc
import mogot.rendering.Display

object MaterialField : AbstractField<Node, String>() {
    override val name: String
        get() = "material"
    override val type: Field.Type
        get() = Field.Type.FILE

    override suspend fun setValue(engine: Engine, node: Node, value: String) {
        node as MaterialNode
        if (value.isEmpty())
            node.material.value = null
        else
            node.material.value = engine.resources.loadMaterial(value).instance()
    }

    override suspend fun setSubFields(engine: Engine, node: Node, data: Map<String, Any>) {
        node as MaterialNode
        val material = node.material.value ?: return
        material as MaterialInstance
        data.forEach {
            val value = when (it.value) {
                is String -> engine.resources.createTexture2D(it.value as String)
                else -> null
            }
            if (value != null)
                material.set(it.key, value)
        }
    }

    override fun currentValue(node: Node): String {
        return ""
    }

}

open class CSGBox(val engine: Engine) : CSGPrimitive(), MaterialNode by MaterialNodeImpl() {
    var width: Float = 1f
    var height: Float = 1f
    var depth: Float = 1f
    private var geomNode3D2 by ResourceHolder<Geometry>()

    override fun getField(name: String): Field? =
            when (name) {
                MaterialField.name -> MaterialField
                else -> super.getField(name)
            }

    private fun rebuild() {
        geomNode3D2 = Geoms.buildCube3(width = width / 2f, height = height / 2f, depth = depth / 2f, gl = engine.gl)
    }

    override fun onStart() {
        super.onStart()
        rebuild()
    }

    override fun render(model: Matrix4fc, modelView:Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (geomNode3D2 == null)
            rebuild()
        super.render(model, modelView, projection, context)
        val material = material
        val geom = geomNode3D2
        material.value?.use(model, modelView, projection, context)
        geom!!.draw()
        material.value?.unuse()
    }

    override fun close() {
        geomNode3D2 = null
        material.dispose()
        super.close()
    }
}