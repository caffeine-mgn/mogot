package pw.binom.sceneEditor.nodeController

import mogot.MaterialNode
import mogot.Node
import mogot.VisualInstance
import mogot.VisualInstance2D
import mogot.math.*
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.MaterialFieldEditor
import pw.binom.sceneEditor.properties.TextureFieldEditor
import pw.binom.ui.AbstractEditor

class MaterialField(val view: SceneEditorView, override val node: Node) : NodeService.FieldString() {

    init {
        require(node is VisualInstance || node is VisualInstance2D)
    }

    override val subFieldsEventChange = mogot.EventDispatcher()

    private val materialNode = node as MaterialNode
    private val material: MaterialInstance?
        get() = materialNode.material.value as? MaterialInstance?

    private var originalValue: String? = null
    override val id: Int
        get() = MaterialField::class.java.hashCode()
    override val groupName: String
        get() = "Material"
    override var currentValue: Any
        get() = material?.root?.file?.let { view.editor1.getRelativePath(it) } ?: ""
        set(value) {
            if (value == currentValue)
                return
            println("Reset Material Files: $value")
            val file = if (value == "") null else view.editor1.findFileByRelativePath(value as String)
            materialNode.material.value = if (file == null) {
                view.default3DMaterial.instance(Vector4f(1f, 1f, 1f, 1f))
            } else {
                view.engine.resources.loadMaterial(file)
            }
            rebuildUniforms()
        }
    override val value: Any
        get() = originalValue ?: currentValue

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "material"
    override val displayName: String
        get() = "Material"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = currentValue as String
        currentValue = value
    }

    override fun resetValue() {
        if (originalValue != null) {
            currentValue = originalValue!!
            originalValue = null
        }
    }

    private val uniforms = ArrayList<NodeService.Field>()

    override fun getSubFields(): List<NodeService.Field> {
        if (!updated) {
            rebuildUniforms()
            updated = true
        }
        return uniforms
    }

    private fun rebuildUniforms() {
        uniforms.clear()
        material?.uniforms?.forEach {
            when (it.type) {
                MaterialInstance.Type.Texture -> uniforms.add(TextureUniformField(view, node, it))
            }
        }
        subFieldsEventChange.dispatch()
    }

    override fun makeEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>): AbstractEditor =
            MaterialFieldEditor(sceneEditor, fields)

    private var updated = false
}

class TextureUniformField(val view: SceneEditorView, override val node: Node, val uniform: MaterialInstance.Uniform) : NodeService.FieldString() {

    init {
        require(node is VisualInstance || node is VisualInstance2D)
        require(uniform.type == MaterialInstance.Type.Texture)
    }

    override val subFieldsEventChange = mogot.EventDispatcher()

    private var originalValue: String? = null
    override val id: Int
        get() = TextureUniformField::class.java.hashCode() + uniform.name.hashCode() + uniform.type.hashCode()
    override val groupName: String
        get() = ""

    override var currentValue: Any
        get() = (uniform.materialInstance.get(uniform) as ExternalTextureFS?)?.file?.let { view.editor1.getRelativePath(it) }
                ?: ""
        set(value) {
            value as String
            if (value.isEmpty()) {
                uniform.materialInstance.set(uniform, null)
            } else {
                view.editor1.findFileByRelativePath(value)
                        ?.let { view.engine.resources.loadTexture(it) }
                        ?.let {
                            uniform.materialInstance.set(uniform, it)
                        }
            }
        }
    override val value: Any
        get() = originalValue ?: currentValue

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = uniform.name
    override val displayName: String
        get() = uniform.title

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = currentValue as String
        currentValue = value
    }

    override fun resetValue() {
        if (originalValue != null) {
            currentValue = originalValue!!
            originalValue = null
        }
    }

    override fun makeEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>): AbstractEditor =
            TextureFieldEditor(sceneEditor, fields)
}

class TextureSpriteField(val view: SceneEditorView, override val node: EditableSprite) : NodeService.FieldString() {

    override val subFieldsEventChange = mogot.EventDispatcher()

    private var originalValue: String? = null
    override val id: Int
        get() = TextureSpriteField::class.java.hashCode()
    override val groupName: String
        get() = "Sprite"

    override var currentValue: Any
        get() = node.textureFile?.file?.let { view.editor1.getRelativePath(it) }
                ?: ""
        set(value) {
            value as String
            println("Set Texture \"$value\"")
            if (value.isEmpty()) {
                node.textureFile = null
            } else {
                view.editor1.findFileByRelativePath(value)
                        ?.let { view.engine.resources.loadTexture(it) }
                        ?.let {
                            node.textureFile = it
                        }
            }
        }
    override val value: Any
        get() = originalValue ?: currentValue

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "texture"
    override val displayName: String
        get() = "Texture"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = currentValue as String
        currentValue = value
    }

    override fun resetValue() {
        if (originalValue != null) {
            currentValue = originalValue!!
            originalValue = null
        }
    }

    override fun makeEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>): AbstractEditor =
            TextureFieldEditor(sceneEditor, fields)
}

object MaterialNodeUtils {

    fun clone(from: MaterialNode, to: MaterialNode) {
        to.material.value = when (val mat = from.material.value) {
            is MaterialInstance -> {
                val new = mat.root.instance()
                mat.uniforms.forEach {
                    new.set(it, mat.get(it))
                }
                new
            }
            else -> mat
        }
    }

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