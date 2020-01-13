package pw.binom.scene

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import pw.binom.*
import java.io.InputStream
import java.io.OutputStream

class Scene(val childs: List<Node>) {
    class Node(val className: String, val properties: Map<String, String>, val childs: List<Node>)

    fun save(stream: OutputStream) {
        val root = JsonNodeFactory.instance.objectNode()
        if (childs.isNotEmpty()) {
            val array = JsonNodeFactory.instance.arrayNode()
            array.addAll(childs.map { it.toObject() })
            root.set<JsonNode>("scene", array)
        }
        val mapper = ObjectMapper()
        mapper.writeValue(stream, root)
    }

    private fun Node.toObject(): ObjectNode {
        val root = JsonNodeFactory.instance.objectNode()
        root.set<JsonNode>("class", JsonNodeFactory.instance.textNode(className))
        if (properties.isNotEmpty())
            root.set<JsonNode>("properties", properties.toObject())

        if (childs.isNotEmpty()) {
            val array = JsonNodeFactory.instance.arrayNode()
            array.addAll(childs.map { it.toObject() })
            root.set<JsonNode>("childs", array)
        }
        return root
    }

    companion object {
        fun load(stream: InputStream): Scene {
            val mapper = ObjectMapper()
            val root = mapper.readTree(stream)
            return Scene(root.obj["scene"]?.map {
                readNode(it.obj)
            } ?: emptyList())
        }

        private fun readNode(node: ObjectNode): Node {
            val className = node["class"].string
            val properties = node["properties"]?.obj?.toMap() ?: emptyMap()
            val childs = node["childs"]?.array?.map {
                readNode(it.obj)
            } ?: emptyList()

            return Node(className, properties, childs)
        }
    }
}