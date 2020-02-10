package mogot.gl

import com.jogamp.opengl.*
import com.jogamp.opengl.awt.GLJPanel
import com.jogamp.opengl.util.Animator
import com.jogamp.opengl.util.FPSAnimator
import mogot.*
import mogot.math.*
import pw.binom.io.FileSystem
import java.awt.Cursor
import java.awt.Dimension
import java.awt.Point
import java.awt.Robot
import java.awt.event.*
import java.awt.image.BufferedImage


open class GLView(val fileSystem: FileSystem<Unit>, fps: Int? = 60) : Stage, GLJPanel(GLCapabilities(GLProfile.getDefault())) {
    override lateinit var gl: GL
    override val mouseDown = EventValueDispatcher<Int>()
    override val mouseUp = EventValueDispatcher<Int>()
    private val mouseButtonsDown = HashSet<Int>()
    private val keyDown = HashSet<Int>()

    protected open var render3D = true
    protected open var render2D = true

    override fun isMouseDown(button: Int): Boolean = button in mouseButtonsDown
    override fun isKeyDown(code: Int): Boolean = code in keyDown

    override val mousePosition = Vector2i()
    override var lockMouse: Boolean = false
    private val defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    private val hiddenCursor = run {
        val cursorImg = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        toolkit.createCustomCursor(cursorImg, Point(0, 0), "blank cursor")
    }

    var directLightShadowsRender: DirectLightShadowsRender? = null

    init {
        autoSwapBufferMode = false
    }

    var updateOnEvent = false

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
    protected open var camera2D: Camera2D? = null
        set(value) {
            value?.resize(width, height)
            field = value
        }
    protected open var camera: Camera? = null
        set(value) {
            value?.resize(width, height)
            field = value
        }
    protected open val root
        get() = camera?.asUpSequence()?.last()

    private lateinit var _engine: Engine
    val engine
        get() = _engine
    val backgroundColor
        get() = renderContext.sceneColor

    protected object renderContext : RenderContext {
        override val lights = ArrayList<Light>()
        override val shadowMaps: MutableList<Texture2D> = mutableListOf()
        override val sceneColor: Vector4f = Vector4f(0f, 0f, 0f, 1f)
    }

    init {
        animator = if (fps == null) Animator(this) else FPSAnimator(this, fps, true)
    }

    protected open fun mouseDown(e: MouseEvent) {
        mouseButtonsDown.add(e.button)
        mouseDown.dispatch(e.button)
        if (updateOnEvent) {
            requestFocus()
            repaint()
        }
    }

    protected open fun mouseUp(e: MouseEvent) {
        mouseButtonsDown.remove(e.button)
        mouseUp.dispatch(e.button)
        if (updateOnEvent) {
            requestFocus()
            repaint()
        }
    }

    init {
        addGLEventListener(object : GLEventListener {
            override fun reshape(drawable: GLAutoDrawable, x: Int, y: Int, width: Int, height: Int) {
                setup(width, height)
                if (updateOnEvent)
                    repaint()
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
                mouseUp(e)
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent) {

            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent) {
                mouseDown(e)
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
                if (updateOnEvent) {
                    requestFocus()
                    repaint()
                }
            }
        })

        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent) {
                keyDown(e)
            }

            override fun keyReleased(e: KeyEvent) {
                keyUp(e)
            }

        })
    }

    protected open fun keyDown(e: KeyEvent) {
        keyDown.add(e.keyCode)
        if (updateOnEvent)
            repaint()
    }

    protected open fun keyUp(e: KeyEvent) {
        keyDown.remove(e.keyCode)
        if (updateOnEvent)
            repaint()
    }

    protected open fun setup(width: Int, height: Int) {
        directLightShadowsRender = if (directLightShadowsRender != null) {
            directLightShadowsRender?.close()
            DirectLightShadowsRender(gl, width, height, width, height)
        }else{
            DirectLightShadowsRender(gl, width, height, width, height)
        }
        gl.gl.glViewport(0, 0, width, height)
        camera?.resize(width, height)
        camera2D?.resize(width, height)
        gl.gl.glClearColor(renderContext.sceneColor.x, renderContext.sceneColor.y, renderContext.sceneColor.z, renderContext.sceneColor.w)
        gl.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.gl.glEnable(GL2.GL_BLEND)
        gl.disable(gl.MULTISAMPLE)
        repaint()
    }

    private val cameraModel3DMatrix = Matrix4f()
    private val cameraModel2DMatrix = Matrix4f()
    private var oldLockMouse = false

    protected open fun render2(dt: Float) {
        if (!update2DPhysics)
            return
        try {
            engine.physicsManager2D.step(dt)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private var added = false

    override fun addNotify() {
        if (added)
            return
        added = true
        super.addNotify()
    }

    override fun destroy() {
        if (added) {
            super.removeNotify()
        }
        super.destroy()
    }

    protected open val update2DPhysics = true


    override fun removeNotify() {
//        super.removeNotify()
    }

    protected open fun render() {
        val time = System.nanoTime()
        val dt = (time - lastFrameTime) / 1e+9f

        if (oldLockMouse != lockMouse) {
            oldLockMouse = lockMouse
            mousePosition.set(size.x / 2, size.y / 2)
        }
        render2(dt)
        gl.gl.glClear(com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT or com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT)


        gl.gl.glMatrixMode(GL2.GL_MODELVIEW)
        camera?.globalToLocalMatrix(cameraModel3DMatrix.identity())

        camera2D?.globalToLocalMatrix(cameraModel2DMatrix.identity())
                ?: cameraModel2DMatrix.identity()

//        camera2D = null

        renderContext.lights.clear()
        renderContext.shadowMaps.clear()
        root?.walk {
            if (it is Light)
                renderContext.lights += it
            true
        }

        while (!engine.frameListeners.isEmpty) {
            engine.frameListeners.popFirst().invoke()
        }

        if (root != null) {
            update(dt, root!!, camModel = cameraModel3DMatrix, ortoModel = cameraModel2DMatrix)
            if (render3D) {
                if (camera != null)
                    camera?.begin()
                gl.gl.glEnable(GL2.GL_DEPTH_TEST)
                gl.gl.glEnable(GL2.GL_CULL_FACE)

                if (camera != null) {
                    (renderContext.shadowMaps as MutableList<Texture2D>).clear()
                    directLightShadowsRender?.render(camera!!.position,root!!, renderContext)
                    renderNode3D(root!!, cameraModel3DMatrix, camera!!.projectionMatrix, renderContext)
                }

                gl.gl.glDisable(GL2.GL_DEPTH_TEST)
                gl.gl.glDisable(GL2.GL_CULL_FACE)
                if (camera != null)
                    camera?.end(renderContext)
            }
            if (render2D) {
                if (!render3D) {
                    gl.gl.glDisable(GL2.GL_DEPTH_TEST)
                    gl.gl.glDisable(GL2.GL_CULL_FACE)
                }
                renderNode2D(root!!,
                        camera2D?.projectionMatrix
                                ?: cameraModel2DMatrix.identity().setOrtho2D(0f, size.x.toFloat(), size.y.toFloat(), 0f)
                        , renderContext)
            }
        }

        if (lockMouse) {
            val point = locationOnScreen
            point.x += size.x / 2
            point.y += size.y / 2

            robot.mouseMove(point.x, point.y)
        }

        lastFrameTime = time
    }

    private var lastFrameTime = System.nanoTime()

    private fun update(dt: Float, node: Node, camModel: Matrix4fc, ortoModel: Matrix4fc) {
        node.update(dt)
        var mat3d = camModel
        var mat2d = ortoModel

        if (node.isSpatial())
            mat3d = node.apply(mat3d)

        if (node.isSpatial2D())
            mat2d = node.apply(mat2d)


        node.childs.forEach {
            update(dt, it, camModel = mat3d, ortoModel = mat2d)
        }
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node.isVisualInstance) {
            node as VisualInstance
            if (!node.visible)
                return
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }

    protected open fun renderNode2D(node: Node, projection: Matrix4fc, renderContext: RenderContext) {
        if (node.isVisualInstance2D()) {
            if (!node.visible)
                return
            node.render(node.matrix, projection, renderContext)
        }


        node.childs.forEach {
            renderNode2D(it, projection, renderContext)
        }
    }

    protected open fun init() {
        _engine = Engine(this, fileSystem)
    }

    protected open fun dispose() {

    }

    fun startRender() {
        if (animator != null) {
            if (animator.isStarted)
                animator.resume()
            else
                animator.start()
        }
    }

    fun stopRender() {
        animator.pause()
    }
}