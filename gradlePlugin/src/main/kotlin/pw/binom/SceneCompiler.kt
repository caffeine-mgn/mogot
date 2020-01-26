package pw.binom

import mogot.scene.SceneLoader
import pw.binom.io.*
import pw.binom.scene.Scene
import java.io.File

object SceneCompiler {
    fun compile(input: File, output: File, behavioursGenerator: BehavioursGenerator?) {
//        if (!DesktopAssertTask.checkChanges(input, output)) {
//            println("$input: UP-TO-DATE")
//            return
//        }
        val stream = input.inputStream().use { input ->
            Scene.load(input)
        }
        val out = output.outputStream().wrap()
        out.write(SceneLoader.SCENE_MAGIC_BYTES)
        val type: Byte = if (stream.type == Scene.Type.D3)
            1
        else
            0
        out.write(type)
        out.use { output ->
            writeChilds(input.path, output, stream.childs, behavioursGenerator)
            output.flush()
        }
        println("$input: compiled")
    }

    private fun writeChilds(fileName: String, output: OutputStream, childs: List<Scene.Node>, behavioursGenerator: BehavioursGenerator?) {
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
                output.writeUTF8String(t)
                output.writeUTF8String(u)
            }
            writeChilds(fileName, output, it.childs, behavioursGenerator)
        }
    }


}