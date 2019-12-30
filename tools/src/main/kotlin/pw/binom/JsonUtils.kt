package pw.binom

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

val JsonNode.obj: ObjectNode
    get() = this as ObjectNode

val JsonNode.array: ArrayNode
    get() = this as ArrayNode

val JsonNode.string: String
    get() = this.textValue()!!

fun ObjectNode.toMap() = this.fields().asSequence().associate {
    it.key to it.value.string
}

fun Map<String, String>.toObject(): ObjectNode {
    val obj = JsonNodeFactory.instance.objectNode()
    forEach { t, u ->
        obj.set<JsonNode>(t, JsonNodeFactory.instance.textNode(u))
    }
    return obj
}