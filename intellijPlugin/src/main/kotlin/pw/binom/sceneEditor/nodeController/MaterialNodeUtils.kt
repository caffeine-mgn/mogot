package pw.binom.sceneEditor.nodeController

import mogot.MaterialNode
import mogot.math.*
import pw.binom.sceneEditor.*

object MaterialNodeUtils {
    fun save(view: SceneEditorView, node: MaterialNode, props: MutableMap<String, String>) {
        val material = node.material.value as? MaterialInstance ?: return
        props["material.file"] = view.editor1.getRelativePath(material.root.file)
        material.uniforms.forEach {
            val value = material.get(it) ?: return@forEach
            val valueStr = when (value) {
                is ExternalTextureFS -> "TEX ${view.editor1.getRelativePath(value.file)}"
                is Vector2fc -> "VEC2 ${value.x};${value.y}"
                is Vector3fc -> "VEC3 ${value.x};${value.y};${value.z}"
                is Vector4fc -> "VEC4 ${value.x};${value.y};${value.z};${value.w}"
                is Int -> "INT $value"
                is Float -> "FLOAT $value"
                is Double -> "DOUBLE $value"
                is Boolean -> "BOOLEAN ${if (value) "true" else "false"}"
                else -> throw IllegalArgumentException("Can't save material property to file. Property type is ${value::class.java.name}")
            }
            props["material.props.${it.name}"] = valueStr
        }
    }

    fun load(view: SceneEditorView, node: MaterialNode, props: Map<String, String>) {
        val materialFile = props["material.file"]?.let { view.editor1.findFileByRelativePath(it) } ?: return
        val material = view.engine.resources.loadMaterial(materialFile)
        val properties = props.asSequence()
                .filter { it.key.startsWith("material.props.") }
                .map {
                    it.key.removePrefix("material.props.") to it.value
                }
                .map {
                    val value: Any? = when {
                        it.second.startsWith("VEC2 ") -> {
                            val items = it.second.removePrefix("VEC2 ").split(';')
                            Vector2f(
                                    items[0].toFloatOrNull() ?: 0f,
                                    items[1].toFloatOrNull() ?: 0f
                            )
                        }
                        it.second.startsWith("VEC3 ") -> {
                            val items = it.second.removePrefix("VEC3 ").split(';')
                            Vector3f(
                                    items[0].toFloatOrNull() ?: 0f,
                                    items[1].toFloatOrNull() ?: 0f,
                                    items[2].toFloatOrNull() ?: 0f
                            )
                        }
                        it.second.startsWith("VEC4 ") -> {
                            val items = it.second.removePrefix("VEC4 ").split(';')
                            Vector4f(
                                    items[0].toFloatOrNull() ?: 0f,
                                    items[1].toFloatOrNull() ?: 0f,
                                    items[2].toFloatOrNull() ?: 0f,
                                    items[3].toFloatOrNull() ?: 0f
                            )
                        }
                        it.second.startsWith("FLOAT ") -> {
                            it.second.removePrefix("FLOAT ").toFloatOrNull() ?: 0f
                        }
                        it.second.startsWith("INT ") -> {
                            it.second.removePrefix("INT ").toIntOrNull() ?: 0f
                        }
                        it.second.startsWith("DOUBLE ") -> {
                            it.second.removePrefix("DOUBLE ").toIntOrNull() ?: 0f
                        }
                        it.second.startsWith("BOOLEAN ") -> {
                            it.second.removePrefix("BOOLEAN ") == "true"
                        }
                        it.second.startsWith("TEX ") -> {
                            val path = it.second.removePrefix("TEX ")
                            val file = view.editor1.findFileByRelativePath(path)
                            file?.let { view.engine.resources.loadTexture(it) }
                        }
                        else -> throw IllegalArgumentException("Can't parse material property ${it.second}")
                    }
                    value ?: return@map null
                    it.first to value
                }
                .filterNotNull()
                .map { prop ->
                    val uniform = material.uniforms.find { it.name == prop.first } ?: return@map null
                    uniform to prop.second
                }
                .filterNotNull()

        properties.forEach {
            material.set(it.first, it.second)
        }
        node.material.value = material
    }
}