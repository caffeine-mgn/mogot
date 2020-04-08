package game.game

import mogot.*
import mogot.gl.GLView
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.rendering.*
import pw.binom.MockFileSystem
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin
import mogot.rendering.*
import pw.binom.sceneEditor.*

/*class FullScreenSprite(engine: Engine) {
    var material: Material? = null
    private val rect = Rect2D(engine.gl, Vector2f(16f, 16f))
    private val projection = Matrix4f().ortho2D(0f, 16f, 16f, 0f)

    fun draw(renderContext: RenderContext) {
        val mat = material ?: return
        mat.use(MATRIX4_ONE, this.projection, renderContext)
        rect.draw()
        mat.unuse()
    }
}*/

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

class FFF : Behaviour() {
    var r = 0f
    override fun onUpdate(delta: Float) {
        val node = node as? Spatial ?: return
        r += delta
        node.position.x = sin(r) * 3f
        node.position.z = cos(r) * 3f
        super.onUpdate(delta)
    }
}

class DDD : GLView(EditorDisplay(), MockFileSystem()) {

    private var closed = false
    private var inited = false
    override var root: Node? = Node()
    var cam = Camera()

    override var camera: Camera? = cam

    override fun init() {
        backgroundColor.set(0.5f, 0.5f, 0.5f, 1f)
        super.init()
        /*val gg = Grid3D(engine.gl)
        gg.material.value = Default3DMaterial(engine.gl).instance(mogot.math.Vector4f(0.0f,0.0f,0.0f,1.0f))
        gg.parent = root*/
        inited = true
        cam.parent = root
        cam.position.set(5f, 5f, 5f)
        //cam.lookTo(Vector3f(6f, 6f, 6f))
        cam.behaviour = FpsCamB(engine)
        val box = CSGBox(engine)
        box.material.value = SimpleMaterial(engine.gl)
        box.parent = root
        cam.lookTo(Vector3f(0f, 0f, 0f))
        cam.enabled = true
        (display as EditorDisplay).setCamera(camera)
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
        val gTop = GuideLine(GuideLine.Place.TOP)
        val gLeft = GuideLine(GuideLine.Place.LEFT)
        f.contentPane.add(gTop, BorderLayout.NORTH)
        f.contentPane.add(gLeft, BorderLayout.WEST)
        val p = JPanel().also { it.background = Color.GRAY }
        var mx = 0
        var my = 0

        p.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {
                mx = e.x
                my = e.y
            }

            override fun mouseDragged(e: MouseEvent) {
                val dx = e.x - mx
                val dy = e.y - my
                mx = e.x
                my = e.y
                gTop.position += dx / gTop.scale
                gLeft.position += dy / gLeft.scale
            }
        })
        p.addMouseWheelListener {
//            gTop.scale -= it.wheelRotation / 5f
//            gLeft.scale -= it.wheelRotation / 5f
            println("g.scale=${gTop.scale}")
        }
        f.contentPane.add(p, BorderLayout.CENTER)

        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        f.size = Dimension(800, 600)
        f.add(view)
//        view.startRender()
        f.isVisible = true
        view.startRender()
    }
}