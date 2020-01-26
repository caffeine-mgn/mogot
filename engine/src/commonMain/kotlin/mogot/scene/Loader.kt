package mogot.scene

import mogot.Engine

class LoaderContext(val engine: Engine, loaders: Collection<SceneLoader.NodeLoader>, val behavioursLoader: BehavioursLoader) {
    private val nodes = loaders.asSequence().map { it.nodeClass to it }.toMap()
    fun nodeLoader(clazz: String): SceneLoader.NodeLoader =
            nodes[clazz] ?: throw RuntimeException("Can't find loader for \"$clazz\"")

    fun readBehaviour(data: Map<String, String>) =
            behavioursLoader.readBehaviour(engine, data)
}