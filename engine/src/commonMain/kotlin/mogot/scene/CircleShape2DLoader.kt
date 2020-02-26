package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.physics.d2.shapes.CircleShape2D

object CircleShape2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.physics.d2.shapes.CircleShape2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = CircleShape2D(engine)
        Spatial2DLoader.load(engine, node, loaderContext, props)
        Shape2DLoader.load(node, props)
        node.radius = props["radius"]?.toFloatOrNull() ?: 50f
        return node
    }

}