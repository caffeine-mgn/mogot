package mogot.scene

import mogot.CSGBox
import mogot.Engine
import mogot.Node

object CSGBoxLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.CSGBox"

    override suspend fun load(engine: Engine,loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = CSGBox(engine)
        SpatialLoader.load(engine, node, loaderContext, props)
        MaterialNodeLoader.load(engine, node, props)
        return node
    }

}