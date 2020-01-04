package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.OmniLight

object OmniLightNodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.OmniLight"

    override suspend fun load(engine: Engine, props: Map<String, String>): Node {
        val node = OmniLight()
        SpatialLoader.load(node, props)
        return node
    }
}