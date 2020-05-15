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
            val file = if (value == "") null else view.editor1.findFileByRelativePath(value as String)
            materialNode.material.value = if (file == null) {
                view.default3DMaterial.instance(Vector4f(1f, 1f, 1f, 1f))
            } else {
                view.engine.resources.loadMaterial(view.project, file)
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