package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.math.Vector2f
import mogot.physics.d2.shapes.PolygonShape2D

object PolygonShape2DLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.physics.d2.shapes.PolygonShape2D"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = PolygonShape2D(engine)
        Spatial2DLoader.load(engine, node, loaderContext, props)
        props["vertex"]?.splitToSequence('|')
                ?.map {
                    val items = it.split('+')
                    Vector2f(
                            items.getOrNull(0)?.toFloat() ?: 0f,
                            items.getOrNull(1)?.toFloat() ?: 0f
                    )
                }
                ?.toList()
                ?.let { node.setVertex(it) }
        return node
    }

}