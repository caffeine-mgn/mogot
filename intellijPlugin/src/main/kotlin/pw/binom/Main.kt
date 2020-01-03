package pw.binom

import com.jetbrains.rd.util.string.printToString
import mogot.*
import mogot.gl.GLView
import mogot.math.*
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import pw.binom.sceneEditor.Default3DMaterial
import pw.binom.sceneEditor.Grid
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.io.FileInputStream
import java.io.StringReader
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

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

/*
class DDShaderMaterial(gl: GL, vp: String, fp: String) : MaterialGLSL(gl) {
    override val shader: Shader = Shader(gl, vp, fp)

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)

    override fun close() {
        shader.close()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}
*/
class DDD : GLView(MockFileSystem()) {

    private var closed = false
    private var inited = false
    override val root: Node = Node()
    var cam = Camera()

    override var camera: Camera? = cam
    private lateinit var box:CSGBox

    override fun init() {
        backgroundColor.set(0.5f, 0.5f, 0.5f, 1f)
        super.init()
        val mat = Default3DMaterial(engine)
        inited = true
        val grid = Grid(engine)
        grid.parent = root
        grid.material.value = mat

        cam.parent = root
        box = CSGBox(engine)
        box.parent = root
        cam.position.set(3f, 3f, 3f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        box.material.value = mat
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        closed = true
        super.dispose()
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
//        val view3d = FbxViewer(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\main\\resources\\untitled.fbx").inputStream().readAllBytes())
        val view = DDD()
        val f = JFrame()

//        val view =FileInputStream(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\assets\\box.fbx")).use {
//            FbxViewer(it.readBytes())
//        }
//        f.contentPane.add(view.glcanvas)
        f.contentPane.add(view)

        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.size = Dimension(800, 600)
//        f.add(view3d)
//        view.startRender()
        f.isVisible = true
        view.startRender()
    }
}