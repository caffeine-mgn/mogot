package pw.binom.sceneEditor.nodeLoader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Node
import mogot.OmniLight
import pw.binom.sceneEditor.SceneEditorView

private val JsonNode.obj: ObjectNode
    get() = this as ObjectNode

private val JsonNode.array: ArrayNode
    get() = this as ArrayNode

private val JsonNode.string: String
    get() = this.textValue()!!

object OmniLightLoader : NodeLoader {
    override fun isCanLoad(classname: String): Boolean = classname == "mogot.OmniLight"
    override fun isCanSave(node: Node): Boolean = node is OmniLight

    override fun load(view: SceneEditorView, node: ObjectNode): Node {
        val n = OmniLight()
        node["properties"]?.obj?.fields()?.forEach {
            when (it.key) {
                "position.x" -> n.position.x = it.value.floatValue()
                "position.y" -> n.position.y = it.value.floatValue()
                "position.z" -> n.position.z = it.value.floatValue()
            }
        }
        return n
    }

    override fun save(view: SceneEditorView, node: Node): ObjectNode {
        node as OmniLight
        val r = JsonNodeFactory.instance.objectNode()
        val properties = JsonNodeFactory.instance.objectNode()
        r["properties"] = properties
        properties.set("position.x", JsonNodeFactory.instance.numberNode(node.position.x))
        properties.set("position.y", JsonNodeFactory.instance.numberNode(node.position.y))
        properties.set("position.z", JsonNodeFactory.instance.numberNode(node.position.z))
        return r
    }

}