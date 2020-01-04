package pw.binom

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import pw.binom.material.ImageCompiler
import pw.binom.material.MaterialCompiler
import java.io.File

open class DesktopAssertTask : DefaultTask() {

    @get:InputDirectory
    var assertPath: File? = null

    @OutputDirectory
    var outputPath: File? = null

    companion object {
        fun checkChanges(src: File, dest: File): Boolean {
            if (!dest.exists() || !dest.isFile)
                return true
            return src.lastModified() > dest.lastModified()
        }
    }

    private fun processDirectory(file: File, outputDir: File) {
        when (file.extension.toLowerCase()) {
            "png" -> ImageCompiler.compile(file, File(outputDir, file.name + ".bin"))
            "mat" -> MaterialCompiler.compile(file, File(outputDir, file.name + ".bin"))
            "scene" -> SceneCompiler.compile(file, File(outputDir, file.name + ".bin"))
        }

//        inputDir.listFiles()?.asSequence()?.filter { it.isFile }?.forEach {
//            println("FILE ${it.absolutePath} -> ${File(outputDir, it.name)}")
//        }
    }

    @TaskAction
    fun execute() {
        check(assertPath != null) { "assertPath not set" }
        check(outputPath != null) { "outputPath not set" }

        val assertPath = assertPath!!
        val outputPath = outputPath!!

        assertPath.walkTopDown().onEnter {
            true
        }.forEach {
            val file = if (it.isAbsolute) it else it.absoluteFile
            val outDir = File(outputPath, file.path.removePrefix(assertPath.path)).parentFile
            processDirectory(file, outDir)
        }
    }
}