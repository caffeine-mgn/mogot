package mogot.scene

import mogot.Engine
import mogot.Node
import mogot.Resources
import mogot.Spatial
import pw.binom.io.*

object SceneLoader {
    val SCENE_MAGIC_BYTES = byteArrayOf(44, 83, 56, 33)
    suspend fun loadScene(engine: Engine, stream: AsyncInputStream, loaders: List<NodeLoader>, behavioursLoader: BehavioursLoader): Node {
        val magic = ByteArray(SCENE_MAGIC_BYTES.size)
        stream.readFully(magic)
        SCENE_MAGIC_BYTES.forEachIndexed { index, byte ->
            if (magic[index] != byte)
                throw IllegalArgumentException("Can't load scene from. Input data is not Scene")
        }
        val root = Spatial()
        val loadersMap = loaders.associate { it.nodeClass to it }

        loadChilds(root, engine, stream, loadersMap, behavioursLoader)
        return root
    }

    private suspend fun loadChilds(root: Node, engine: Engine, stream: AsyncInputStream, loaders: Map<String, NodeLoader>, behavioursLoader: BehavioursLoader) {
        val childCount = stream.readInt()
        (0 until childCount).forEach {
            val clazz = stream.readUTF8String()
            val propCount = stream.readInt()
            val props = (0 until propCount).associate {
                stream.readUTF8String() to stream.readUTF8String()
            }
            val loader = loaders[clazz] ?: throw IllegalArgumentException("Can't find Scene Loader for class $clazz")
            val node = loader.load(engine, behavioursLoader, props)
            node.parent = root
            loadChilds(node, engine, stream, loaders, behavioursLoader)
        }
    }

    interface NodeLoader {
        val nodeClass: String
        suspend fun load(engine: Engine, behavioursLoader: BehavioursLoader, props: Map<String, String>): Node
    }
}

suspend fun Resources.loadScene(path: String, loaders: List<SceneLoader.NodeLoader>, behavioursLoader: BehavioursLoader): Node {
    val stream = fileSystem.get(Unit, "$path.bin")?.read() ?: throw FileSystem.FileNotFoundException(path)
    return stream.use {
        SceneLoader.loadScene(engine, it, loaders, behavioursLoader)
    }
}