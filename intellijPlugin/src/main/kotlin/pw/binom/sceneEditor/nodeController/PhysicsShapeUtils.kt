package pw.binom.sceneEditor.nodeController

import pw.binom.sceneEditor.properties.ShapeEditorNode

object PhysicsShapeUtils {
    fun load(node: ShapeEditorNode, data: Map<String, String>) {
        node.sensor = data["sensor"] == "1"
        node.density = data["density"]?.toFloatOrNull() ?: 1f
        node.friction = data["friction"]?.toFloatOrNull() ?: 0.5f
        node.restitution = data["restitution"]?.toFloatOrNull() ?: 0.2f
    }

    fun save(node: ShapeEditorNode, data: MutableMap<String, String>) {
        data["sensor"] = if (node.sensor) "1" else "0"
        data["density"] = node.density.toString()
        data["friction"] = node.friction.toString()
        data["restitution"] = node.restitution.toString()
    }

    fun clone(from: ShapeEditorNode, to: ShapeEditorNode) {
        to.density = from.density
        to.friction = from.friction
        to.sensor = from.sensor
    }
}