package pw.binom

import mogot.scene.SceneLoader
import pw.binom.io.*
import pw.binom.scene.Scene
import java.io.File
import java.io.FileNotFoundException

object SceneCompiler {
    fun compile(assetsPath: File, input: File, output: File, behavioursGenerator: BehavioursGenerator?) {
//        if (!DesktopAssertTask.checkChanges(input, output)) {
//            println("$input: UP-TO-DATE")
//            return
//        }
        val stream = input.inputStream().use { input ->
            Scene.load(input)
        }
        output.parentFile.mkdirs()
        val out = output.outputStream().wrap()
        out.write(SceneLoader.SCENE_MAGIC_BYTES)
        val type: Byte = if (stream.type == Scene.Type.D3)
            1
        else
            0
        out.write(type)
        out.use { output1 ->
            writeChilds(assetsPath,input, output, input.path, output1, stream.childs, behavioursGenerator)
            output1.flush()
        }
        println("$input: compiled")
    }

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
            writeChilds(assetsPath,sceneFile, sceneResultFile, fileName, output, it.childs, behavioursGenerator)
        }
    }
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