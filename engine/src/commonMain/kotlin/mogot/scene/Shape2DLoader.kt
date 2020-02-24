package mogot.scene

import mogot.physics.d2.shapes.Shape2D

object Shape2DLoader {
    fun load(node: Shape2D, data: Map<String, String>) {
        node.sensor = data["sensor"] == "1"
    }
}