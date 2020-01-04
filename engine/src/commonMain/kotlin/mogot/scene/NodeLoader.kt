package mogot.scene

import mogot.Engine
import mogot.Node

object NodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Node"

    override suspend fun load(engine: Engine, props: Map<String, String>): Node {
        val node = Node()
        loadNode(node, props)
        return node
    }

    fun loadNode(node: Node, props: Map<String, String>) {
        props["id"]?.let { node.id = it }
    }

}