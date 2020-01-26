package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.OmniLight

object OmniLightNodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.OmniLight"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = OmniLight()
        SpatialLoader.load(engine, node, loaderContext, props)
        return node
    }
}