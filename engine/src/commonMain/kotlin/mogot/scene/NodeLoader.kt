package mogot.scene

import mogot.Engine
import mogot.Node

object NodeLoader : SceneLoader.NodeLoader {
    override val nodeClass: String
        get() = "mogot.Node"

    override suspend fun load(engine: Engine, behavioursLoader: BehavioursLoader, props: Map<String, String>): Node {
        val node = Node()
        loadNode(engine, node, behavioursLoader, props)
        return node
    }

    fun loadNode(engine: Engine, node: Node, behavioursLoader: BehavioursLoader, props: Map<String, String>) {
        props["id"]?.let { node.id = it }
        node.behaviour = behavioursLoader.readBehaviour(engine, props)
    }

}