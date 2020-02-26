package mogot.scene

import mogot.Camera2D
import mogot.Engine
import mogot.Node

object Camera2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Camera2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = Camera2D(engine)
        Spatial2DLoader.load(engine, node, loaderContext, props)
        return node
    }

}