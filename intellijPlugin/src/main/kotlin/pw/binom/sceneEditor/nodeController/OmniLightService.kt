package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.math.AABBm
import pw.binom.SolidTextureMaterial
import pw.binom.io.Closeable
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

object OmniNodeCreator : NodeCreator {
    override val name: String
        get() = "Omni Light"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))

    override fun create(view: SceneEditorView): Node {
        val node = OmniLight()
        createStub(view, node)
        return node
    }
}

private class OmniManager(val engine: Engine) : Closeable {
    val lights = HashMap<OmniLight, FlatScreenBehaviour>()

    lateinit var omniLightTexture: ExternalTexture

    init {
        engine.editor.renderThread {
            omniLightTexture = engine.resources.loadTextureResource("/light-icon.png")
            omniLightTexture.inc()
        }
    }

    override fun close() {
        omniLightTexture.dec()
    }
}

private val Engine.omniManager: OmniManager
    get() = manager("omniManager") { OmniManager(this) }

private fun createStub(view: SceneEditorView, light: OmniLight) {
    view.renderThread {
        val s = Sprite(view.engine)
        s.size.set(120f / 4f, 160f / 4f)
        s.material.value = SolidTextureMaterial(view.engine).apply {
            diffuseColor.set(0f, 0f, 0f, 0f)
            tex = view.engine.omniManager.omniLightTexture.gl
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
    override fun isEditor(node: Node): Boolean = node is OmniLight
    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is OmniLight) return
        EmptyNodeService.nodeDeleted(view.engine, node)
        view.engine.omniManager.lights.remove(node)?.node?.let {
            it.parent = null
            it.close()
        }
    }

    override fun selected(view: SceneEditorView, node: Node) {
        val sprite = view.engine.omniManager.lights[node]?.node ?: return
        val material = sprite.material.value as SolidTextureMaterial
        material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
    }

    override fun unselected(view: SceneEditorView, node: Node) {
        val sprite = view.engine.omniManager.lights[node]?.node ?: return
        val material = sprite.material.value as SolidTextureMaterial
        material.diffuseColor.set(0f, 0f, 0f, 0f)
    }


    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != OmniLight::class.java.name)
            return null
        val node = OmniLight()
        createStub(view, node)
        SpatialService.loadSpatial(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is OmniLight) return null
        val out = HashMap<String, String>()
        SpatialService.saveSpatial(view.engine, node, out)
        out["specular"] = node.specular.toString()
        return out
    }
}