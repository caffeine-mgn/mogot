package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.OmniLight

object OmniLightNodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.OmniLight"

    override suspend fun load(engine: Engine, behavioursLoader: BehavioursLoader, props: Map<String, String>): Node {
        val node = OmniLight()
        SpatialLoader.load(engine, node, behavioursLoader, props)
        return node
    }
}