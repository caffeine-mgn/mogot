package pw.binom

import mogot.Field
import mogot.math.*
import mogot.scene.SceneLoader
import pw.binom.io.*
import pw.binom.scene.Scene
import java.io.File

object SceneCompiler {
    fun compile(assetsPath: File, input: File, output: File, nodes: MutableSet<String>) {
        try {
            val stream = input.inputStream().use { input ->
                Scene.load(input)
            }
            output.parentFile.mkdirs()
            val out = output.outputStream().wrap()
            out.write(SceneLoader.SCENE_MAGIC_BYTES)
            out.writeInt(stream.childs.size)
            stream.childs.forEach {
                writeNode(it, nodes, out)
            }
        } catch (e: Throwable) {
            throw RuntimeException("Can't compile ${input.absolutePath}", e)
        }
        println("$input: compiled")
    }

    private fun writeNode(node: Scene.Node, nodes: MutableSet<String>, stream: OutputStream) {
        nodes.add(node.className)
        stream.writeUTF8String(node.className)
        if (node.id != null) {
            stream.write(10)
            stream.writeUTF8String(node.id!!)
        } else {
            stream.write(0)
        }
        val count = node.properties.asSequence()
                .filter { '>' !in it.key }
                .filter { it.value.isNotEmpty() }
                .count()
        stream.writeInt(count)
        node.properties.asSequence()
                .filter { '>' !in it.key }
                .forEach {
                    if (it.value.isEmpty())
                        return@forEach
                    val type = Field.Type.findByPrefix(it.value)
                    stream.writeUTF8String(it.key)
                    saveProperty(type, Field.Type.fromText(it.value), stream)
                    val prefix = "${it.key}>"
                    val subFields = node.properties.asSequence()
                            .filter { v -> v.key.startsWith(prefix) }
                            .map { it.key.removePrefix(prefix) to it.value }
                            .toMap()
                    stream.writeInt(subFields.size)
                    subFields.forEach {
                        val subType = Field.Type.findByPrefix(it.value)
                        stream.writeUTF8String(it.key)
                        saveProperty(subType, Field.Type.fromText(it.value), stream)
                    }
                }
        stream.writeInt(node.childs.size)
        node.childs.forEach {
            writeNode(it, nodes, stream)
        }
    }

    private fun saveProperty(type: Field.Type, value: Any, stream: OutputStream) {
        stream.write(type.type)
        val r = when (type) {
            Field.Type.INT -> stream.writeInt(value as Int)
            Field.Type.BOOL -> stream.write(if (value as Boolean) 10.toByte() else 0.toByte())
            Field.Type.FILE, Field.Type.STRING -> stream.writeUTF8String(value as String)
            Field.Type.VEC2 -> {
                value as Vector2fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
            }
            Field.Type.FLOAT -> stream.writeFloat(value as Float)
            Field.Type.VEC3 -> {
                value as Vector3fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
            }
            Field.Type.VEC4 -> {
                value as Vector4fc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
                stream.writeFloat(value.w)
            }
            Field.Type.QUATERNION -> {
                value as Quaternionfc
                stream.writeFloat(value.x)
                stream.writeFloat(value.y)
                stream.writeFloat(value.z)
                stream.writeFloat(value.w)
            }
        }
    }
    /*
    private fun writeChilds(assetsPath: File, sceneFile: File, sceneResultFile: File, fileName: String, output: OutputStream, childs: List<Scene.Node>, behavioursGenerator: BehavioursGenerator?) {
        output.writeInt(childs.size)
        childs.forEach {
            output.writeUTF8String(it.className)
            output.writeInt(it.properties.size)
            if (behavioursGenerator != null) {
                val behaviourClass = it.properties["behaviour.class"]
                if (behaviourClass != null) {
                    val gen = behavioursGenerator.getClass(behaviourClass)
                    it.properties.asSequence()
                            .filter { it.key.startsWith("behaviour.property.") }
                            .forEach {
                                gen.addProperty(fileName, it.key.removePrefix("behaviour.property."), it.value)
                            }
                }
            }
            it.properties.forEach { t, u ->
                val key = if (u.startsWith("FILE ")) {
                    val file = sceneFile.parentFile.getRelative(u.removePrefix("FILE "))
                            ?: throw FileNotFoundException("\"$u\" by relative path \"${sceneFile.absolutePath}\"")
                    file.path.removePrefix(assetsPath.path)
                    //File(sceneResultFile.path.removePrefix(assetsPath)).parentFile


                } else {
                    u
                }

                output.writeUTF8String(t)
                output.writeUTF8String(key)
            }
            writeChilds(assetsPath, sceneFile, sceneResultFile, fileName, output, it.childs, behavioursGenerator)
        }
    }
    */
}

private fun File.getRelative(file: String): File? {
    var currentFile = this
    file.splitToSequence('/', '\\').forEach {
        if (it == ".")
            return@forEach
        if (it == "..") {
            currentFile = currentFile.parentFile ?: return null
            return@forEach
        }
        currentFile = File(currentFile, it)
        if (!currentFile.exists())
            return null
    }
    return currentFile
}