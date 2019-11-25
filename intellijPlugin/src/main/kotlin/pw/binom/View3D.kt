package pw.binom


import mogot.*
import mogot.gl.GLView
import mogot.math.Quaternionf
import mogot.math.Vector3f
import mogot.math.forward
import mogot.math.times
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener

abstract class View3D : GLView() {

    private var _x = 0
    private var _y = 0
    private var h = 0f
    private var v = 0f
    private var d = 6f

    override var camera: Camera? = Camera()

    init {
        val root = Spatial()
        camera!!.parent = root
    }

    protected fun resetCam() {
        val q = Quaternionf()
        q.rotateAxis(v, 1f, 0f, 0f)
        q.rotateAxis(h, 0f, 1f, 0f)
        camera!!.position.set(q.forward * d)
        camera!!.lookTo(Vector3f(0f, 0f, 0f))
    }

    init {
        addMouseWheelListener {
            d += it.wheelRotation / 2f
            if (d > 100f)
                d = 100f
            if (d < 1f)
                d = 1f
            resetCam()
            repaint()
        }
        addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent) {

            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent) {
                _x = e.x
                _y = e.y
            }
        })
        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent?) {
            }

            override fun mouseDragged(e: MouseEvent) {
                val dx = _x - e.x
                _x = e.x
                val dy = _y - e.y
                _y = e.y
                h -= dx / 100f
                v += dy / 100f
                if (v < -Math.PI / 2.0 + 0.001)
                    v = (-Math.PI / 2.0 + 0.001).toFloat()
                if (v > Math.PI / 2.0 - 0.001)
                    v = (Math.PI / 2.0 - 0.001).toFloat()
                resetCam()
                repaint()
            }

        })
    }
}

/*
abstract class View3D : Stage, GLJPanel(GLCapabilities(GLProfile.getDefault())) {
    override lateinit var gl: mogot.gl.GL
    private val camera = Camera()

    protected val root = Spatial()
    private var _x = 0
    private var _y = 0
    private var h = 0f
    private var v = 0f
    private var d = 6f

    protected fun resetCam() {
        val q = Quaternionf()
        q.rotateAxis(v, 1f, 0f, 0f)
        q.rotateAxis(h, 0f, 1f, 0f)
        camera.position.set(q.forward * d)
        camera.lookTo(Vector3f(0f, 0f, 0f))
    }

    protected abstract fun init(drawable: GLAutoDrawable)
    protected abstract fun dispose(drawable: GLAutoDrawable)

    init {
        addMouseWheelListener {
            d += it.wheelRotation / 2f
            if (d > 100f)
                d = 100f
            if (d < 1f)
                d = 1f
            resetCam()
            repaint()
        }
        addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent) {

            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent) {
                _x = e.x
                _y = e.y
            }

        })
        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent?) {
            }

            override fun mouseDragged(e: MouseEvent) {
                val dx = _x - e.x
                _x = e.x
                val dy = _y - e.y
                _y = e.y
                h -= dx / 100f
                v += dy / 100f
                if (v < -Math.PI / 2.0 + 0.001)
                    v = (-Math.PI / 2.0 + 0.001).toFloat()
                if (v > Math.PI / 2.0 - 0.001)
                    v = (Math.PI / 2.0 - 0.001).toFloat()
                resetCam()
                repaint()
            }

        })

        root.addChild(camera)
        addGLEventListener(object : GLEventListener {
            override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
                println("resize! $width x $height")
                setup(drawable.gl.gL2, width, height)
                drawable.gl.gL2.glViewport(x,y,width,height)
                camera.resize(width, height)
                repaint()
            }

            override fun display(drawable: GLAutoDrawable) {
                render(drawable.gl.gL2, drawable.surfaceWidth, drawable.surfaceHeight);
            }

            override fun init(drawable: GLAutoDrawable) {
                println("init opengl")
                this@View3D.gl = mogot.gl.GL(drawable.gl.gL2)
                this@View3D.init(drawable)
                repaint()
            }

            override fun dispose(drawable: GLAutoDrawable) {
                println("dispose opengl")
                this@View3D.dispose(drawable)
            }

        })
    }

    protected fun setup(gl2: GL2, width: Int, height: Int) {
    }

    val viewMatrix = Matrix4f()
    val rc = RenderContextImpl()
    protected open fun render(gl2: GL2, width: Int, height: Int) {
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT or GL.GL_DEPTH_BUFFER_BIT)
        gl2.glEnable(GL2.GL_DEPTH_TEST)
        gl2.glEnable(GL2.GL_CULL_FACE)

        gl2.glMatrixMode(GL2.GL_MODELVIEW)
        gl2.glMatrixMode(GL2.GL_MODELVIEW)
        camera.applyMatrix(viewMatrix.identity())

        rc.pointLights.clear()
        root.walk {
            if (it is PointLight)
                rc.pointLights += it
            true
        }
        update(root, viewMatrix)
        renderNode3D(root, viewMatrix, camera.projectionMatrix, rc)
    }

    private fun update(node: Node, model: Matrix4fc) {
        var pos = model
        if (node is Spatial) {
            pos = node.apply(model)
        }

        node.childs.forEach {
            update(it, pos)
        }
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node is Spatial) {
//            pos = node.apply(model)
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }
}

class RenderContextImpl : RenderContext {
    override val pointLights = ArrayList<PointLight>()
    override val sceneColor: Vector4fc
        get() = Vector4f(0f, 0f, 0f, 1f)

}
 */