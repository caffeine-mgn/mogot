package mogot.scene

import mogot.*
import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.math.Vector4f
import pw.binom.io.*

object SceneLoader {
    val SCENE_MAGIC_BYTES = byteArrayOf(44, 83, 56, 33)
    suspend fun loadScene(engine: Engine, stream: AsyncInputStream, loaderContext: LoaderContext): Node {
        val magic = ByteArray(SCENE_MAGIC_BYTES.size)
        stream.readFully(magic)
        SCENE_MAGIC_BYTES.forEachIndexed { index, byte ->
            if (magic[index] != byte)
                throw IllegalArgumentException("Can't load scene from. Input data is not Scene")
        }
        val d3 = stream.read() > 0
        val root = if (d3)
            Spatial()
        else
            Spatial2D(engine)
        loadChilds(root, engine, stream, loaderContext)
        return root
    }

    private suspend fun loadNode(stream: AsyncInputStream, loader: Loader) {

    }

    private suspend fun loadChilds(root: Node, engine: Engine, stream: AsyncInputStream, loaderContext: LoaderContext) {
        val childCount = stream.readInt()
        (0 until childCount).forEach {
            val clazz = stream.readUTF8String()
            val propCount = stream.readInt()
            val props = (0 until propCount).associate {
                stream.readUTF8String() to stream.readUTF8String()
            }
            val loader = loaderContext.nodeLoader(clazz)
            val node = loader.load(engine, loaderContext, props)
            node.parent = root
            loadChilds(node, engine, stream, loaderContext)
        }
    }

    interface NodeLoader {
        val nodeClass: String
        suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node
    }
}

suspend fun Resources.loadScene(path: String, loaders: List<SceneLoader.NodeLoader>, behavioursLoader: BehavioursLoader): Node {
    val stream = fileSystem.get(Unit, "$path.bin")?.read() ?: throw FileSystem.FileNotFoundException(path)
    return stream.use {
        SceneLoader.loadScene(engine, it, LoaderContext(engine, loaders, behavioursLoader))
    }
}