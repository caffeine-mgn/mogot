package game.game

import mogot.*
import mogot.gl.GLView
import mogot.math.*
import mogot.math.Math.sin
import mogot.math.Math.toRadians
import pw.binom.MockFileSystem
import pw.binom.sceneEditor.Default3DMaterial
import pw.binom.sceneEditor.FrustumNode
import pw.binom.sceneEditor.Grid
import pw.binom.sceneEditor.RotateAllAxes
import pw.binom.sceneEditor.properties.BehaviourProperty
import java.awt.Dimension
import javax.swing.JFrame
import kotlin.math.sin
import kotlin.math.cos

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

class DDD : GLView(MockFileSystem()) {

    private var closed = false
    private var inited = false
    override val root: Node = Node()
    var cam = Camera()

    override var camera: Camera? = cam
    private val cam2 = Camera()
    private lateinit var box: CSGBox
    lateinit var rotateAllAxes: RotateAllAxes

    override fun init() {
        backgroundColor.set(0.5f, 0.5f, 0.5f, 1f)
        super.init()
        val mat = Default3DMaterial(engine)
        inited = true
        val grid = Grid(engine)
        grid.parent = root
        grid.material.value = mat

        cam.parent = root
        cam.position.set(5f, 5f, 5f)
        //cam.lookTo(Vector3f(6f, 6f, 6f))
        cam.behaviour = FpsCamB(engine)
        cam2.parent = root
        cam2.resize(200, 200)

        cam2.far = 30f

        cam2.quaternion.identity()
        cam2.position.set(5f, 2f, 0f)
        //RotationVector(cam2.quaternion).y = toRadians(45.0).toFloat()
        cam2.lookTo(Vector3f(0f, 0f, 0f))
        println(cam2.globalToLocalMatrix(Matrix4f()))

        FrustumNode(engine, cam2).also {
            it.parent = cam2
            it.material.value = mat
        }

        run {
            box = CSGBox(engine)
            box.width = 0.1f
            box.height = 0.1f
            box.depth = 0.1f
            box.parent = cam2
            box.position.z = 2f
            box.material.value = mat
        }

        box = CSGBox(engine)
        box.parent = root
        box.material.value = mat
        box.behaviour = FFF()
//        rotateAllAxes = RotateAllAxes(
//                engine, root, cam, listOf(grid)
//        )
    }

    override fun render() {
        //rotateAllAxes.render(0f)
        cam2.lookTo(box.position)
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