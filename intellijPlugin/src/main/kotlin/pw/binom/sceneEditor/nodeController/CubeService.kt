package pw.binom.sceneEditor.nodeController

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.CSGBox
import mogot.Node
import pw.binom.sceneEditor.ExternalMaterial
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.obj
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.PositionPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

object CubeServiceFactory : NodeServiceFactory {
    override fun create(view: SceneEditorView): NodeService =
            CubeService(view)
}

class CubeService(private val view: SceneEditorView) : NodeService {
    private val props = listOf(PositionPropertyFactory, MaterialPropertyFactory)
    override fun getProperties(node: Node): List<PropertyFactory> = props

    private val cubeIcon = ImageIcon(this::class.java.classLoader.getResource("/cube-icon-16.png"))
    override val createItems: List<NodeService.CreateItem> = listOf(object : NodeService.CreateItem {
        override val name: String
            get() = "CSV Box"
        override val icon: Icon?
            get() = cubeIcon

        override fun create(): Node {
            val node = CSGBox(view.engine)
            node.material = view.default3DMaterial
            view.link(node, this@CubeService)
            return node
        }
    })

    override fun load(clazz: String, json: ObjectNode): Node? {
        if (clazz != CSGBox::class.java.name)
            return null
        println("Load $clazz...")
        val n = CSGBox(view.engine)
        view.link(n, this)
        n.material = view.default3DMaterial
        json["properties"]?.obj?.fields()?.forEach {
            when (it.key) {
                "position.x" -> n.position.x = it.value.floatValue()
                "position.y" -> n.position.y = it.value.floatValue()
                "position.z" -> n.position.z = it.value.floatValue()
            }
        }
        println("Loaded!")
        return n
    }

    override fun save(node: Node): ObjectNode? {
        val node = (node as? CSGBox) ?: return null
        val r = JsonNodeFactory.instance.objectNode()
        val properties = JsonNodeFactory.instance.objectNode()
        r["properties"] = properties
        properties.set("position.x", JsonNodeFactory.instance.numberNode(node.position.x))
        properties.set("position.y", JsonNodeFactory.instance.numberNode(node.position.y))
        properties.set("position.z", JsonNodeFactory.instance.numberNode(node.position.z))
        return r
    }

    override fun close() {
    }

}