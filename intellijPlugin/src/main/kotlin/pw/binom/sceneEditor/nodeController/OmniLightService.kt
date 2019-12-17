package pw.binom.sceneEditor.nodeController

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Node
import mogot.OmniLight
import mogot.Sprite
import pw.binom.SolidTextureMaterial
import pw.binom.io.wrap
import pw.binom.sceneEditor.LightScreenPos
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.obj
import pw.binom.sceneEditor.properties.PositionPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

object OmniLightServiceFactory : NodeServiceFactory {
    override fun create(view: SceneEditorView): NodeService =
            OmniLightService(view)
}

class OmniLightService(private val view: SceneEditorView) : NodeService {
    private val lightIcon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))

    val omniLightTexture = this::class.java.getResourceAsStream("/light-icon.png").use {
        view.engine.resources.syncCreateTexture2D(it.wrap())
    }

    private val props = listOf(PositionPropertyFactory)
    override fun getProperties(node: Node): List<PropertyFactory> = props

    private val lights = HashMap<OmniLight, LightScreenPos>()
    override val createItems: List<NodeService.CreateItem> = listOf(object : NodeService.CreateItem {
        override val name: String
            get() = "Omni Light"
        override val icon: Icon?
            get() = lightIcon

        override fun create(): Node {
            val node = OmniLight()
            view.link(node, this@OmniLightService)
            createStub(node)
            return node
        }
    })

    override fun selected(node: Node) {
        super.selected(node)
        val sprite = lights[node]!!.node
        val material = sprite.material as SolidTextureMaterial
        material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
    }

    override fun unselected(node: Node) {
        val sprite = lights[node]!!.node
        val material = sprite.material as SolidTextureMaterial
        material.diffuseColor.set(0f, 0f, 0f, 0f)
        super.unselected(node)
    }

    private fun createStub(light: OmniLight) {
        view.renderThread {
            val s = Sprite(view.engine)
            s.size.set(120f / 4f, 160f / 4f)
            s.material = SolidTextureMaterial(view.engine.gl).apply {
                diffuseColor.set(0f, 0f, 0f, 0f)
                tex = omniLightTexture
            }
            val b = LightScreenPos(view.editorCamera, light)
            s.behaviour = b
            s.parent = view.editorRoot
            lights[light] = b

            println("Create stub for $light $s and $b")
        }
    }

    override fun load(clazz: String, json: ObjectNode): Node? {
        if (clazz != OmniLight::class.java.name)
            return null
        println("Load $clazz...")
        val n = OmniLight()
        createStub(n)
        view.link(n, this)
        json["properties"]?.obj?.fields()?.forEach {
            when (it.key) {
                "position.x" -> n.position.x = it.value.floatValue()
                "position.y" -> n.position.y = it.value.floatValue()
                "position.z" -> n.position.z = it.value.floatValue()
                "specular" -> n.specular = it.value.floatValue()
            }
        }
        println("Loaded!")
        return n
    }

    override fun save(node: Node): ObjectNode? {
        val node = (node as? OmniLight) ?: return null
        val r = JsonNodeFactory.instance.objectNode()
        val properties = JsonNodeFactory.instance.objectNode()
        r["properties"] = properties
        properties.set("position.x", JsonNodeFactory.instance.numberNode(node.position.x))
        properties.set("position.y", JsonNodeFactory.instance.numberNode(node.position.y))
        properties.set("position.z", JsonNodeFactory.instance.numberNode(node.position.z))
        properties.set("specular", JsonNodeFactory.instance.numberNode(node.specular))
        return r
    }

    override fun close() {
    }

}