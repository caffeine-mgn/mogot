package mogot.scene

import mogot.AnimateNode
import mogot.Engine
import mogot.Node

object AnimateNodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.AnimateNode"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val out = AnimateNode(engine)
        props.asSequence().filter { it.key.startsWith("files.") }.forEach {
            out.animations += it.value
        }

        props["animationIndex"]?.toIntOrNull()?.let {
            if (it >= 0 && it < out.animations.size)
                out.setAnimation(it)
        }
        return out
    }

}