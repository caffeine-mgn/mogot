package mogot.gl

import com.jogamp.opengl.*
import com.jogamp.opengl.awt.GLJPanel
import mogot.*
import mogot.math.*
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Point
import java.awt.Robot
import java.awt.event.*
import java.awt.image.BufferedImage


open class GLView : Stage, GLJPanel(GLCapabilities(GLProfile.getDefault())) {
    override lateinit var gl: GL
    override val mouseDown = EventValueDispatcher<Int>()
    override val mouseUp = EventValueDispatcher<Int>()
    private val mouseButtonsDown = HashSet<Int>()
    private val keyDown = HashSet<Int>()
    override fun isMouseDown(button: Int): Boolean = button in mouseButtonsDown
    override fun isKeyDown(code: Int): Boolean = code in keyDown

    override val mousePosition = Vector2i()
    override var lockMouse: Boolean = false
    private val defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    private val hiddenCursor = run {
        val cursorImg = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        toolkit.createCustomCursor(cursorImg, Point(0, 0), "blank cursor")
    }
    override var cursorVisible: Boolean
        get() = cursor == defCursor
        set(value) {
            cursor = if (value)
                defCursor
            else
                hiddenCursor
        }

    private var tempDimension = Dimension()
    private var tempSize = Vector2i()
    override val size: Vector2ic
        get() {
            getSize(tempDimension)
            tempSize.set(tempDimension.width, tempDimension.height)
            return tempSize
        }
    private val robot = Robot()
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

        addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent) {
                mouseButtonsDown.remove(e.button)
                mouseUp.dispatch(e.button)
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent) {

            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent) {
                mouseButtonsDown.add(e.button)
                mouseDown.dispatch(e.button)
            }
        })

        addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {
                mousePosition.x = e.x
                mousePosition.y = e.y
            }

            override fun mouseDragged(e: MouseEvent) {
                mousePosition.x = e.x
                mousePosition.y = e.y
            }
        })

        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent) {
                keyDown.add(e.keyCode)
            }

            override fun keyReleased(e: KeyEvent) {
                keyDown.remove(e.keyCode)
            }

        })
    }

    protected open fun setup(width: Int, height: Int) {
        gl.gl.glViewport(x, y, width, height)
        camera?.resize(width, height)
        gl.gl.glClearColor(renderContext.sceneColor.x, renderContext.sceneColor.y, renderContext.sceneColor.z, renderContext.sceneColor.w)
        repaint()
    }

    private val tempMatrix = Matrix4f()
    private var oldLockMouse = false
    protected open fun render() {
        val time = System.nanoTime()
        val dt = (time - lastFrameTime) / 1e+9f

        if (oldLockMouse != lockMouse) {
            oldLockMouse = lockMouse
            mousePosition.set(size.x / 2, size.y / 2)
        }
        gl.gl.glClear(com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT or com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT)
        gl.gl.glEnable(GL2.GL_DEPTH_TEST)
        gl.gl.glEnable(GL2.GL_CULL_FACE)

        gl.gl.glMatrixMode(GL2.GL_MODELVIEW)
        camera?.applyMatrix(tempMatrix.identity())

        renderContext.pointLights.clear()
        root?.walk {
            if (it is PointLight)
                renderContext.pointLights += it
            true
        }

        while (!engine.frameListeners.isEmpty) {
            engine.frameListeners.popFirst().invoke()
        }

        if (root != null) {
            update(dt,root!!, camModel = tempMatrix, ortoModel = MATRIX4_ONE)
            renderNode3D(root!!, tempMatrix, camera!!.projectionMatrix, renderContext)

            gl.gl.glDisable(GL2.GL_DEPTH_TEST)
            gl.gl.glDisable(GL2.GL_CULL_FACE)

            tempMatrix.identity().ortho2D(0f, size.x.toFloat(), size.y.toFloat(), 0f)
            renderNode2D(root!!, tempMatrix, renderContext)
        }

        if (lockMouse) {
            val point = locationOnScreen
            point.x += size.x / 2
            point.y += size.y / 2

            robot.mouseMove(point.x, point.y)
        }
        swapBuffers()
        lastFrameTime = time
    }

    private var lastFrameTime = System.nanoTime()

    private fun update(dt:Float,node: Node, camModel: Matrix4fc,ortoModel:Matrix4fc) {

        val time = System.nanoTime()
        node.update((time - lastFrameTime) / 1e+9f)
        lastFrameTime = time
        val pos = when (node){
            is Spatial->node.apply(camModel)
            is Spatial2D->node.apply(ortoModel)
            else->camModel
        }


        node.childs.forEach {
            update(dt,it, camModel=pos,ortoModel = ortoModel)
        }
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node is VisualInstance) {
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }

    private fun renderNode2D(node: Node, projection: Matrix4fc, renderContext: RenderContext) {
        //var pos = model
        if (node is VisualInstance2D) {
            //pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }


        node.childs.forEach {
            renderNode2D(it, projection, renderContext)
        }
    }

    protected open fun init() {
        _engine = Engine(this)
    }

    protected open fun dispose() {

    }
}