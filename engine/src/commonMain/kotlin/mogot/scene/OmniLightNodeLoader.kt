package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.PointLight

object PointLightNodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.PointLight"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = PointLight(engine)
        SpatialLoader.load(engine, node, loaderContext, props)
        return node
    }
}