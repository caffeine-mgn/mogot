package mogot.scene

import mogot.Engine
import mogot.MaterialNode
import mogot.Node
import mogot.Spatial
import mogot.material.loadMaterial
import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.math.Vector4f

object MaterialNodeLoader {
    suspend fun load(engine: Engine, node: MaterialNode, props: Map<String, String>) {
        val materialFile = props["material.file"] ?: return
        val material = engine.resources.loadMaterial(materialFile).instance()
        props.forEach {
            if (!it.key.startsWith("material.props.")) return@forEach
            val uniformName = it.key.removePrefix("material.props.")
            val valueStr = it.value
            val value: Any? = when {
                valueStr.startsWith("VEC2 ") -> {
                    val items = valueStr.removePrefix("VEC2 ").split(';')
                    Vector2f(
                            items[0].toFloatOrNull() ?: 0f,
                            items[1].toFloatOrNull() ?: 0f
                    )
                }
                valueStr.startsWith("VEC3 ") -> {
                    val items = valueStr.removePrefix("VEC3 ").split(';')
                    Vector3f(
                            items[0].toFloatOrNull() ?: 0f,
                            items[1].toFloatOrNull() ?: 0f,
                            items[2].toFloatOrNull() ?: 0f
                    )
                }
                valueStr.startsWith("VEC4 ") -> {
                    val items = valueStr.removePrefix("VEC4 ").split(';')
                    Vector4f(
                            items[0].toFloatOrNull() ?: 0f,
                            items[1].toFloatOrNull() ?: 0f,
                            items[2].toFloatOrNull() ?: 0f,
                            items[3].toFloatOrNull() ?: 0f
                    )
                }
                valueStr.startsWith("FLOAT ") -> {
                    valueStr.removePrefix("FLOAT ").toFloatOrNull() ?: 0f
                }
                valueStr.startsWith("INT ") -> {
                    valueStr.removePrefix("INT ").toIntOrNull() ?: 0f
                }
                valueStr.startsWith("DOUBLE ") -> {
                    valueStr.removePrefix("DOUBLE ").toIntOrNull() ?: 0f
                }
                valueStr.startsWith("BOOLEAN ") -> {
                    valueStr.removePrefix("BOOLEAN ") == "true"
                }
                valueStr.startsWith("TEX ") -> {
                    val path = valueStr.removePrefix("TEX ")
                    engine.resources.createTexture2D(path)
                }
                else -> throw IllegalArgumentException("Can't parse material property $valueStr")
            }
            value ?: return@forEach
            material.set(uniformName, value)
        }
        node.material.value = material
    }
}