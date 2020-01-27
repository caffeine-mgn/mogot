package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.Spatial2D

object Spatial2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Spatial2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val out = Spatial2D(engine)
        load(engine, out, loaderContext, props)
        return out
    }

    fun load(engine: Engine, spatial: Spatial2D, loaderContext: LoaderContext, data: Map<String, String>) {
        NodeLoader.loadNode(engine, spatial, loaderContext, data)
        spatial.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f
        )
        spatial.scale.set(
                data["scale.x"]?.toFloat() ?: 1f,
                data["scale.y"]?.toFloat() ?: 1f
        )
        spatial.rotation = data["rotation"]?.toFloat() ?: 0f
    }
}