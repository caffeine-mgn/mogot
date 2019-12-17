package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

val JsonNode.obj: ObjectNode
    get() = this as ObjectNode

val JsonNode.array: ArrayNode
    get() = this as ArrayNode

val JsonNode.string: String
    get() = this.textValue()!!