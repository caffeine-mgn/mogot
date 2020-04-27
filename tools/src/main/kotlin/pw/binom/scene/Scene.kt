package pw.binom.scene

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import pw.binom.*
import java.io.InputStream
import java.io.OutputStream

class Scene(val childs: List<Node>, val type: Type) {
    enum class Type {
        D2,
        D3
    }

    class Node(val id: String?, val className: String, val properties: Map<String, String>, val childs: List<Node>)

    fun save(stream: OutputStream) {
        val root = JsonNodeFactory.instance.objectNode()
        if (childs.isNotEmpty()) {
            val array = JsonNodeFactory.instance.arrayNode()
            array.addAll(childs.map { it.toObject() })
            root.set<JsonNode>("scene", array)
            root.set<JsonNode>("type", JsonNodeFactory.instance.textNode(when (type) {
                Type.D2 -> "2d"
                Type.D3 -> "3d"
            }))
        }
        val mapper = ObjectMapper()
        mapper.writeValue(stream, root)
    }

    private fun Node.toObject(): ObjectNode {
        val root = JsonNodeFactory.instance.objectNode()
        root.set<JsonNode>("class", JsonNodeFactory.instance.textNode(className))
        if (id != null) {
            root.set<JsonNode>("id", JsonNodeFactory.instance.textNode(id))
        }
        if (properties.isNotEmpty()) {
            root.set<JsonNode>("properties", properties.toObject())
        }

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
            val type = when (val type = root.obj["type"]?.textValue()?.trim()) {
                "3d", null -> Type.D3
                "2d" -> Type.D2
                else -> throw IllegalArgumentException("Unknown scene type \"$type\"")
            }
            return Scene(root.obj["scene"]?.map {
                readNode(it.obj)
            } ?: emptyList(), type)
        }

        private fun readNode(node: ObjectNode): Node {
            val className = node["class"].string
            val properties = node["properties"]?.obj?.toMap() ?: emptyMap()
            val childs = node["childs"]?.array?.map {
                readNode(it.obj)
            } ?: emptyList()

            return Node(className = className, properties = properties, childs = childs, id = node["id"]?.string)
        }
    }
}