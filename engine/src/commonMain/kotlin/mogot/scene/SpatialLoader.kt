package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.Spatial
import mogot.math.RotationVector

object SpatialLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Spatial"

    override suspend fun load(engine: Engine, behavioursLoader: BehavioursLoader, props: Map<String, String>): Node {
        val out = Spatial()
        load(engine, out, behavioursLoader, props)
        return out
    }

    fun load(engine: Engine, spatial: Spatial, behavioursLoader: BehavioursLoader, data: Map<String, String>) {
        NodeLoader.loadNode(engine, spatial, behavioursLoader, data)
        spatial.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f,
                data["position.z"]?.toFloat() ?: 0f
        )
        spatial.scale.set(
                data["scale.x"]?.toFloat() ?: 1f,
                data["scale.y"]?.toFloat() ?: 1f,
                data["scale.z"]?.toFloat() ?: 1f
        )
        val rot = RotationVector(spatial.quaternion)
        rot.set(
                data["rotation.x"]?.toFloat() ?: 0f,
                data["rotation.y"]?.toFloat() ?: 0f,
                data["rotation.z"]?.toFloat() ?: 0f
        )
    }
}