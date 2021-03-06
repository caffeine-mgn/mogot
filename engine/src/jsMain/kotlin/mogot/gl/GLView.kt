package mogot.gl

import mogot.*
import mogot.math.Matrix4f
import mogot.math.Matrix4fc
import mogot.math.Vector2i
import mogot.math.Vector2ic
import mogot.rendering.Display
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import pw.binom.io.FileSystem
import kotlin.browser.document
import kotlin.browser.window

open class GLView(val display: Display, val fileSystem: FileSystem<Unit>) : AbstractGLView() {
    open var camera: Camera? = null
    protected open val root
        get() = camera?.asUpSequence()?.last()

    val backgroundColor
        get() = display.context.backgroundColor

    private val viewMatrix = Matrix4f()

    val engine = Engine(this, fileSystem)
    private var oldLockMouse = false

    override fun draw() {
        val time = window.performance.now().unsafeCast<Float>()
        val dt = (time - lastFrameTime) / 1e+6f
        super.draw()

        if (oldLockMouse != lockMouse) {
            oldLockMouse = lockMouse
            if (lockMouse) {
                mousePosition.set(size.x / 2, size.y / 2)
            }
        }

        gl.ctx.viewport(0, 0, width, height)
        gl.ctx.clear(WebGLRenderingContext.COLOR_BUFFER_BIT or WebGLRenderingContext.DEPTH_BUFFER_BIT)
        gl.ctx.enable(WebGLRenderingContext.DEPTH_TEST)
        gl.ctx.enable(WebGLRenderingContext.CULL_FACE)
        camera?.globalToLocalMatrix(viewMatrix.identity())

        while (!engine.frameListeners.isEmpty) {
            engine.frameListeners.popLast().invoke()
        }

        if (root != null) {
            display.render(gl, root!!)
        }


        if (lockMouse)
            mousePosition.set(size.x / 2, size.y / 2)

        lastFrameTime = time
    }

    override val mouseDown = EventValueDispatcher<Int>()
    override val mouseUp = EventValueDispatcher<Int>()

    init {
        canvas.addEventListener("mousemove", {
            it as MouseEvent
            if (lockMouse) {
                mousePosition.x = size.x / 2 + it.asDynamic().movementX.unsafeCast<Int>()
                mousePosition.y = size.y / 2 + it.asDynamic().movementY.unsafeCast<Int>()
            } else {
                mousePosition.x = it.x.toInt()
                mousePosition.y = it.y.toInt()
            }
        })

        canvas.addEventListener("mousedown", {
            it as MouseEvent
            canvas.focus()
            mouseButtonsDown.add(it.button.unsafeCast<Int>())
            mouseDown.dispatch(it.button.toInt())
            it.preventDefault()
        })
        canvas.addEventListener("mouseup", {
            it as MouseEvent
            canvas.focus()
            mouseButtonsDown.remove(it.button.unsafeCast<Int>())
            mouseUp.dispatch(it.button.toInt())
            it.preventDefault()
        })

        canvas.addEventListener("keydown", {
            it as KeyboardEvent
            keyDown.add(it.keyCode.unsafeCast<Int>())
            it.preventDefault()
        })
        canvas.addEventListener("keyup", {
            it as KeyboardEvent
            keyDown.remove(it.keyCode.unsafeCast<Int>())
            it.preventDefault()
        })

        canvas.addEventListener("contextmenu", {
            it.preventDefault()
        })
    }

    private val mouseButtonsDown = HashSet<Int>()
    private val keyDown = HashSet<Int>()

    override fun isMouseDown(button: Int): Boolean = button in mouseButtonsDown

    override fun isKeyDown(code: Int): Boolean = code in keyDown

    override val mousePosition = Vector2i()
    override var lockMouse: Boolean
        get() = document.asDynamic().pointerLockElement === canvas
        set(value) {
            if (value)
                canvas.asDynamic().requestPointerLock()
            else
                document.asDynamic().exitPointerLock()

        }

    override var cursorVisible: Boolean
        get() = canvas.style.cursor != "pointer"
        set(value) {
            canvas.style.cursor = if (value)
                ""
            else
                "pointer"
        }


    override val size: Vector2ic
        get() {
            val r = canvas.getBoundingClientRect()
            return Vector2i(r.width.toInt(), r.height.toInt())
        }

    private var width: Int = 0
    private var height: Int = 0
    override fun setup(width: Int, height: Int) {
        this.width = width
        this.height = height
        super.setup(width, height)
        display.setup(gl, 0, 0, width, height)
    }

    private var lastFrameTime = window.performance.now().unsafeCast<Float>()
    private fun update(dt: Float, node: Node, camModel: Matrix4fc, ortoModel: Matrix4fc) {
        node.update(dt)
        val pos = when (node) {
            is Spatial -> node.apply(camModel)
            is Spatial2D -> node.apply(ortoModel)
            else -> camModel
        }


        node.childs.forEach {
            update(dt, it, camModel = pos, ortoModel = ortoModel)
        }
    }
}