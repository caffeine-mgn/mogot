package mogot.gl

import com.jogamp.opengl.*
import com.jogamp.opengl.awt.GLJPanel
import mogot.*
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector4f

open class GLView : Stage, GLJPanel(GLCapabilities(GLProfile.getDefault())) {
    override lateinit var gl: GL
    protected open var camera: Camera? = null
    protected open val root
        get() = camera?.asUpSequence()?.last()

    private lateinit var _engine: Engine
    val engine
        get() = _engine
    val backgroundColor
        get() = renderContext.sceneColor

    private object renderContext : RenderContext {
        override val pointLights = ArrayList<PointLight>()
        override val sceneColor: Vector4f = Vector4f(0f, 0f, 0f, 1f)
    }

    init {
        addGLEventListener(object : GLEventListener {
            override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
                setup(width, height)
            }

            override fun display(drawable: GLAutoDrawable) {
                render()
            }

            override fun init(drawable: GLAutoDrawable) {
                this@GLView.gl = GL(drawable.gl.gL2)
                this@GLView.init()
            }

            override fun dispose(drawable: GLAutoDrawable) {
                this@GLView.dispose()
            }

        })
    }

    protected open fun setup(width: Int, height: Int) {
        gl.gl.glViewport(x, y, width, height)
        camera?.resize(width, height)
        gl.gl.glClearColor(renderContext.sceneColor.x, renderContext.sceneColor.y, renderContext.sceneColor.z, renderContext.sceneColor.w)

        println("($width,$height)  projectionMatrix=\n${camera?.projectionMatrix}")
        repaint()
    }

    private val viewMatrix = Matrix4f()

    protected open fun render() {
        gl.gl.glClear(com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT or com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT)
        gl.gl.glEnable(GL2.GL_DEPTH_TEST)
        gl.gl.glEnable(GL2.GL_CULL_FACE)

        gl.gl.glMatrixMode(GL2.GL_MODELVIEW)
        camera?.applyMatrix(viewMatrix.identity())

        renderContext.pointLights.clear()
        root?.walk {
            if (it is PointLight)
                renderContext.pointLights += it
            true
        }
        if (root != null) {
            update(root!!, viewMatrix)
            renderNode3D(root!!, viewMatrix, camera!!.projectionMatrix, renderContext)
        }
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
        if (node is VisualInstance) {
//            pos = node.apply(model)
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }

    protected open fun init() {
        _engine = Engine(this)
    }

    protected open fun dispose() {

    }
}