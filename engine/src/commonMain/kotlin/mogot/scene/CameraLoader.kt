package mogot.scene

import mogot.Camera
import mogot.Engine
import mogot.Node

object CameraLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Camera"

    override suspend fun load(engine: Engine, props: Map<String, String>): Node {
        val node = Camera()
        SpatialLoader.load(node, props)
        props["near"]?.toFloatOrNull()?.let { node.near = it }
        props["far"]?.toFloatOrNull()?.let { node.far = it }
        props["fieldOfView"]?.toFloatOrNull()?.let { node.fieldOfView = it }
        return node
    }

}