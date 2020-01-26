package mogot.scene

import mogot.Engine
import mogot.Node

object NodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Node"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val node = Node()
        loadNode(engine, node, loaderContext, props)
        return node
    }

    fun loadNode(engine: Engine, node: Node, loaderContext: LoaderContext, props: Map<String, String>) {
        props["id"]?.let { node.id = it }
        node.behaviour = loaderContext.readBehaviour(props)
    }

}