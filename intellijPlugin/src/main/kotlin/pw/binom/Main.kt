package pw.binom

import mogot.Engine
import mogot.Material
import mogot.Rect2D
import mogot.RenderContext
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector2f
import pw.binom.sceneEditor.SceneEditorView
import java.awt.Dimension
import javax.swing.JFrame

class FullScreenSprite(engine: Engine) {
    var material: Material? = null
    private val rect = Rect2D(engine.gl, Vector2f(16f, 16f))
    private val projection = Matrix4f().ortho2D(0f, 16f, 16f, 0f)

    fun draw(renderContext: RenderContext) {
        val mat = material ?: return
        mat.use(MATRIX4_ONE, this.projection, renderContext)
        rect.draw()
        mat.unuse()
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        //val view3d = FbxViewer(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\main\\resources\\untitled.fbx").inputStream().readAllBytes())
//        val view3d = SceneEditorView()
        val f = JFrame()
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.size = Dimension(800, 600)
//        f.add(view3d)
        f.isVisible = true
//        view3d.startRender()
    }
}