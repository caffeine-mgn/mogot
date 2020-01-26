package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.Sprite

object SpriteLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Sprite"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val out = Sprite(engine)
        Spatial2DLoader.load(engine, out, loaderContext, props)
        MaterialNodeLoader.load(engine, out, props)
        out.size.set(
                props["size.x"]?.toFloatOrNull()?:0f,
                props["size.y"]?.toFloatOrNull()?:0f
        )
        return out
    }
}