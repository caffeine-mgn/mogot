package mogot.scene

import mogot.*
import pw.binom.io.FileSystem
import pw.binom.io.IOException
import pw.binom.io.use

object InjectSceneLoader : SceneLoader.NodeLoader {
    const val FILE_PROPERTY = "sceneFile"
    override val nodeClass: String
        get() = "Scene"

    override suspend fun load(engine: Engine, loaderContext: LoaderContext, props: Map<String, String>): Node {
        val file = props[FILE_PROPERTY] ?: TODO("Property \"$FILE_PROPERTY\" not found")
        val fileName = "$file.bin"
        val sceneFile = engine.resources.fileSystem.get(Unit, fileName)
                ?: throw FileSystem.FileNotFoundException(fileName)
        val fileStream = sceneFile.read() ?: throw IOException("Can't open file \"$fileName\"")
        val node = fileStream.use {
            SceneLoader.loadScene(engine, it, loaderContext)
        }

        if (node.isSpatial) {
            node as Spatial
            SpatialLoader.load(engine, node, loaderContext, props)
        }

        if (node.isSpatial2D) {
            node as Spatial2D
            Spatial2DLoader.load(engine, node, loaderContext, props)
        }
        return node
    }

}