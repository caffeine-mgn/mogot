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
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.get
import kotlin.collections.listOf
import kotlin.collections.set

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
        s.material.value = SolidTextureMaterial(view.engine).apply {
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

object OmniLightService : NodeService {

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false

    private val props = listOf(Transform3DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun isEditor(node: Node): Boolean = node is PointLight
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PointLight) return null
        val out = PointLight()
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
        val material = sprite.material.value as SolidTextureMaterial
        if (selected)
            material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
        else
            material.diffuseColor.set(0f, 0f, 0f, 0f)
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != PointLight::class.java.name)
            return null
        val node = PointLight()
        createStub(view, node)
        SpatialService.loadSpatial(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is PointLight) return null
        val out = HashMap<String, String>()
        SpatialService.saveSpatial(view.engine, node, out)
        out["specular"] = node.specular.toString()
        return out
    }
}