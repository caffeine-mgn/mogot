package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.Spatial

object SpatialLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Spatial"

    override suspend fun load(engine: Engine, props: Map<String, String>): Node {
        val out = Spatial()
        load(out, props)
        return out
    }

    fun load(spatial: Spatial, data: Map<String, String>) {
        spatial.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f,
                data["position.z"]?.toFloat() ?: 0f
        )
    }
}