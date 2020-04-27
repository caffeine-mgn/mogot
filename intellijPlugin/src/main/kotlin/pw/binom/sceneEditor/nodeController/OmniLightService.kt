package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import mogot.Node
import mogot.PointLight
import mogot.math.AABBm
import mogot.math.set
import pw.binom.SolidTextureMaterial
import pw.binom.io.Closeable
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.collections.set

class SpecularEditableField(override val node: mogot.Light) : NodeService.FieldFloat() {
    override val id: Int
        get() = SpecularEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Light"
    override var currentValue: Any
        get() = node.specular
        set(value) {
            node.specular = value as Float
        }

    private var originalValue: Float? = null

    override val value: Any
        get() = originalValue ?: node.specular

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "specular"
    override val displayName: String
        get() = "Specular"

    override fun setTempValue(value: Any) {
        TODO("Not yet implemented")
    }

    override fun resetValue() {
        TODO("Not yet implemented")
    }
}

object OmniNodeCreator : NodeCreator {
    override val name: String
        get() = "Omni Light"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))

    override fun create(view: SceneEditorView): Node {
        val node = PointLight()
        createStub(view, node)
        return node
    }
}

private class OmniManager(val engine: Engine) : Closeable {
    val lights = HashMap<PointLight, FlatScreenBehaviour>()

    lateinit var PointLightTexture: ExternalTexture

    init {
        engine.editor.renderThread {
            PointLightTexture = engine.resources.loadTextureResource("/light-icon.png")
            PointLightTexture.inc()
        }
    }

    override fun close() {
        PointLightTexture.dec()
    }
}

private val Engine.omniManager: OmniManager
    get() = manager("omniManager") { OmniManager(this) }

private fun createStub(view: SceneEditorView, light: PointLight) {
    view.renderThread {
        val s = SpriteFor3D(view)
        s.size.set(120f / 4f, 160f / 4f)
        s.internalMaterial = SolidTextureMaterial(view.engine).apply {
            diffuseColor.set(0f, 0f, 0f, 0f)
            tex = view.engine.omniManager.PointLightTexture.gl
        }
        val b = FlatScreenBehaviour(view.engine, view.editorCamera, light)
        s.behaviour = b
        s.parent = view.editorRoot
        view.engine.omniManager.lights[light] = b

        println("Create stub for $light $s and $b")
    }
}

class EditablePointLight : PointLight(), EditableNode {
    val specularEditableField = SpecularEditableField(this);
    val positionField = PositionField3D(this)
    val rotationField = RotateField3D(this)

    private val fields = listOf(positionField, rotationField, specularEditableField)
    override fun getEditableFields(): List<NodeService.Field> = fields
}

object OmniLightService : NodeService {

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false
    override val nodeClass: String
        get() = PointLight::class.java.name

    override fun newInstance(view: SceneEditorView): Node {
        val node = EditablePointLight()
        createStub(view, node)
        return node;
    }

    private val props = listOf(Transform3DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun isEditor(node: Node): Boolean = node is PointLight
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PointLight) return null
        val out = EditablePointLight()
        out.specular = node.specular
        out.diffuse.set(node.diffuse)
        SpatialService.cloneSpatial(node, out)
        return out
    }

    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is PointLight) return
        super.delete(view, node)
        view.engine.omniManager.lights.remove(node)?.node?.let {
            it.parent = null
            it.close()
        }
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        val sprite = view.engine.omniManager.lights[node]?.node ?: return
        val material = sprite.internalMaterial as SolidTextureMaterial
        if (selected)
            material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
        else
            material.diffuseColor.set(0f, 0f, 0f, 0f)
    }
}