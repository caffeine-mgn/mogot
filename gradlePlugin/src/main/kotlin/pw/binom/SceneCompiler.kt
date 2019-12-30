package pw.binom

import mogot.scene.SceneLoader
import pw.binom.io.*
import pw.binom.scene.Scene
import java.io.File

object SceneCompiler {
    fun compile(input: File, output: File) {
        if (!DesktopAssertTask.checkChanges(input, output)) {
            println("$input: UP-TO-DATE")
            return
        }
        val stream = input.inputStream().use { input ->
            Scene.load(input)
        }
        val out = output.outputStream().wrap()
        out.write(SceneLoader.SCENE_MAGIC_BYTES)
        out.use { output ->
            writeChilds(output, stream.childs)
            output.flush()
        }
        println("$input: compiled")
    }

    private fun writeChilds(output: OutputStream, childs: List<Scene.Node>) {
        output.writeInt(childs.size)
        childs.forEach {
            output.writeUTF8String(it.className)
            output.writeInt(it.properties.size)
            it.properties.forEach { t, u ->
                output.writeUTF8String(t)
                output.writeUTF8String(u)
            }
            writeChilds(output, it.childs)
        }
    }


}