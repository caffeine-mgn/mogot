package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.physics.d2.shapes.BoxShape2D

object BoxShape2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.physics.d2.shapes.BoxShape2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = BoxShape2D(engine)
        Spatial2DLoader.load(engine, node, loaderContext, props)
        Shape2DLoader.load(node, props)
        node.size.set(
                props["size.x"]?.toFloatOrNull() ?: 0f,
                props["size.y"]?.toFloatOrNull() ?: 0f
        )
        return node
    }

}