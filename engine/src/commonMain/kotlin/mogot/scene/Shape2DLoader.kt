package mogot.scene

import mogot.physics.d2.shapes.Shape2D

object Shape2DLoader {
    fun load(node: Shape2D, data: Map<String, String>) {
        node.sensor = data["sensor"] == "1"
        node.density = data["density"]?.toFloatOrNull() ?: 1f
        node.friction = data["friction"]?.toFloatOrNull() ?: 0.5f
        node.restitution = data["restitution"]?.toFloatOrNull() ?: 0.2f
    }
}