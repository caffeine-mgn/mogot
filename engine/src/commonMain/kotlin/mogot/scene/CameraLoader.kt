package mogot.scene

import mogot.Camera
import mogot.Engine
import mogot.Node

object CameraLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Camera"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = Camera(engine)
        SpatialLoader.load(engine, node, loaderContext, props)
        props["near"]?.toFloatOrNull()?.let { node.near = it }
        props["far"]?.toFloatOrNull()?.let { node.far = it }
        props["fieldOfView"]?.toFloatOrNull()?.let { node.fieldOfView = it }
        //TODO("Add posteffects loading")
        return node
    }

}