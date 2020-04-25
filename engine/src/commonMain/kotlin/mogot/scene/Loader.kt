package mogot.scene

import mogot.Engine
import mogot.Field
import mogot.Node
import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.math.Vector4f
import pw.binom.io.*

class LoaderContext(val engine: Engine, loaders: Collection<SceneLoader.NodeLoader>, val behavioursLoader: BehavioursLoader) {
    private val nodes = loaders.asSequence().map { it.nodeClass to it }.toMap()
    fun nodeLoader(clazz: String): SceneLoader.NodeLoader =
            nodes[clazz] ?: throw RuntimeException("Can't find loader for \"$clazz\"")

    fun readBehaviour(data: Map<String, String>) =
            behavioursLoader.readBehaviour(engine, data)
}

private val SCENE_MAGIC_BYTES = byteArrayOf(44, 83, 56, 33)

abstract class Loader {

    protected abstract fun newInstance(className: String, engine: Engine): Node

    suspend fun loadScene(stream: AsyncInputStream, engine: Engine) {
        val magic = ByteArray(SceneLoader.SCENE_MAGIC_BYTES.size)
        stream.readFully(magic)
        SCENE_MAGIC_BYTES.forEachIndexed { index, byte ->
            if (magic[index] != byte)
                throw IllegalArgumentException("Can't load scene from. Input data is not Scene")
        }
        val node = Node()
        val childCount = stream.readInt()
        repeat(childCount) {
            loadNode(stream, engine).parent = node
        }
    }

    private suspend fun loadNode(stream: AsyncInputStream, engine: Engine): Node {
        val node = newInstance(stream.readUTF8String(), engine)
        val propCount = stream.readInt()
        repeat(propCount) {
            val fieldName = stream.readUTF8String()
            val fieldValue = loadProperty(stream)
            val field = node.getField(fieldName)
            field?.setAsync(fieldValue)
        }
        val childCount = stream.readInt()
        repeat(childCount) {
            loadNode(stream, engine).parent = node
        }
        return node
    }

    private suspend fun loadProperty(stream: AsyncInputStream): Any =
            when (Field.Type.findByType(stream.read())) {
                Field.Type.INT -> stream.readInt()
                Field.Type.BOOL -> stream.read() == 10.toByte()
                Field.Type.STRING -> stream.readUTF8String()
                Field.Type.FILE -> stream.readUTF8String()
                Field.Type.VEC2 -> Vector2f(stream.readFloat(), stream.readFloat())
                Field.Type.FLOAT -> stream.readFloat()
                Field.Type.VEC3 -> Vector3f(stream.readFloat(), stream.readFloat(), stream.readFloat())
                Field.Type.VEC4 -> Vector4f(stream.readFloat(), stream.readFloat(), stream.readFloat(), stream.readFloat())
            }
}