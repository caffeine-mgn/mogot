package pw.binom

import java.awt.Dimension
import java.io.File
import javax.swing.JFrame

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val view3d = FbxViewer(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\main\\resources\\untitled.fbx").inputStream().readAllBytes())
        val f = JFrame()
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.size = Dimension(800, 600)
        f.add(view3d)
        f.isVisible = true
    }
}