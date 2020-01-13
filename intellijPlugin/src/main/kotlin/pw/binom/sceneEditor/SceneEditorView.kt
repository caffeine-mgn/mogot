package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.GLView
import mogot.math.Vector2i
import mogot.math.Vector2f
import mogot.math.Vector3f
import mogot.math.Vector4f
import pw.binom.MockFileSystem
import pw.binom.Services
import pw.binom.SolidMaterial
import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.sceneEditor.editors.EditMoveFactory
import pw.binom.sceneEditor.editors.RotateAllAxesFactory
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

private class EditorHolder(val view: SceneEditorView) : Closeable {
    override fun close() {
    }

}

val Engine.editor: SceneEditorView
    get() = manager<EditorHolder>("Editor") { throw IllegalStateException("View not found") }.view

class SceneEditorView(val editor1: SceneEditor, val project: Project, val file: VirtualFile) : GLView(MockFileSystem()) {

    val editorRoot = Node()
    val sceneRoot = Node()
    val editorCamera = Camera()
    val eventSelectChanged = EventDispatcher()
    private var closed = false
    private var inited = false
    private val frameListeners = Stack<() -> Unit>()
    private lateinit var _default3DMaterial: Default3DMaterial
    val default3DMaterial: Default3DMaterial
        get() = _default3DMaterial

    override fun setup(width: Int, height: Int) {
        engine.manager("Editor") { EditorHolder(this) }
        super.setup(width, height)
        _default3DMaterial = Default3DMaterial(engine)
        _default3DMaterial.inc()
    }

    override fun destroy() {
        _default3DMaterial.dec()
        super.destroy()
    }


    fun renderThread(func: () -> Unit) {
        if (rendering.get()) {
            func()
        } else {
            frameListeners.pushLast {
                func()
            }
            repaint()
        }
    }

    private lateinit var selectorMaterial: SolidMaterial
    private val editorFactories = listOf(EditMoveFactory, RotateAllAxesFactory, FpsCamEditorFactory)
    private val _services by Services.byClassSequence(NodeService::class.java)
    private val links = WeakHashMap<Node, NodeService>()

    fun getService(node: Node): NodeService? {
        var service = links[node]
        if (service == null) {
            service = _services.find { it.isEditor(node) }
            if (service != null)
                links[node] = service
        }
        return service
    }

    init {
        sceneRoot.parent = editorRoot
        sceneRoot.id = "Scene Root"
        updateOnEvent = true
        editorCamera.parent = editorRoot
        camera = editorCamera


        editorCamera.position.set(3f, 3f, 3f)
        editorCamera.lookTo(Vector3f(0f, 0f, 0f))
    }

    private val selectedNodes = ArrayList<Node>()
    val selected: List<Node>
        get() = selectedNodes
    private var selectors = ArrayList<Selector3D>()
    fun select(node: Node?) {
        renderThread {
            selectors.forEach {
                it.parent = null
                it.close()
            }
            selectedNodes.forEach {
                getService(it)?.unselected(this, it)
            }
            selectedNodes.clear()
            if (node != null) {
                getService(node)?.selected(this, node)
                selectedNodes += node
                if (node is Spatial) {
                    val s = Selector3D(engine, node)
                    s.parent = editorRoot
                    s.material.value = default3DMaterial
                    selectors.add(s)
                }

            }

            eventSelectChanged.dispatch()
        }
//        if (node != null && node is OmniLight) {
//            selectedNodes.add(node)
//            selector3D?.also {
//                it.parent = null
//                it.close()
//            }
//            println("Selector for $node")
//            val selector = Selector3D(engine, node)
//            selector.material = selectorMaterial
//            selector.size.set(1f, 1f, 1f)
//            selector.parent = editorRoot
//            repaint()
//        }
    }

    private var editor: EditAction? = null

    override fun mouseDown(e: MouseEvent) {
        this.requestFocus()
        if (editor != null) {
            editor!!.mouseDown(e)
            return
        }
        editorFactories.forEach {
            it.mouseDown(this, e);
            if (editor != null)
                return
        }
        super.mouseDown(e)
    }

    override fun mouseUp(e: MouseEvent) {
        this.requestFocus()
        if (editor != null) {
            editor!!.mouseUp(e)
            return
        }
        super.mouseDown(e)
    }

    override fun keyDown(e: KeyEvent) {
        if (editor != null) {
            editor!!.keyDown(e.keyCode)
            return
        }
        editorFactories.forEach {

            it.keyDown(this, e);
            if (editor != null)
                return
        }
        super.keyDown(e)
    }

    override fun render2(dt: Float) {
        super.render2(dt)
        editor?.render(dt)
    }

    private val rendering = AtomicBoolean(false)
    override fun render() {
        rendering.set(true)
        while (!frameListeners.isEmpty) {
            frameListeners.popFirst().invoke()
        }
        rendering.set(false)
        super.render()
    }

    fun startEditor(editor: EditAction) {
        this.editor?.onStop()
        this.editor = editor
        startDraw()
    }

    fun stopEditing() {
        editor?.onStop()
        editor = null
        stopDraw()
        repaint()
        save()
        editor1.save()
    }

    override fun keyUp(e: KeyEvent) {
        if (editor != null) {
            editor!!.keyUp(e.keyCode)
            return
        }
        super.keyUp(e)
    }

    override fun init() {
        super.init()
        backgroundColor.set(0.376f, 0.376f, 0.376f, 1f)
        val grid = Grid(engine)
        grid.parent = root
        val mat = SolidMaterial(engine)
        grid.material.value = mat
        mat.diffuseColor.set(1f, 1f, 1f, 0.5f)
//        editorCamera.behaviour = FpsCam(engine)
        selectorMaterial = SolidMaterial(engine)


        SceneFileLoader.load(this, file)
//        createStabs(sceneRoot)
        inited = true
    }

    fun save() {
        SceneFileLoader.save(this, file)
    }

    /*
        private fun createStabs(node: Node) {
            if (node is OmniLight) {
                val s = Sprite(engine)
                s.size.set(120f / 4f, 160f / 4f)

                s.material = omniLightMaterial
                val b = LightScreenPos(editorCamera, node)
                s.behaviour = b
                s.parent = root
                lights[node] = b
            }
            node.childs.forEach {
                createStabs(it)
            }
        }
    */

    override fun dispose() {
        println("SceneEditorView.dispose")
        closed = true
        super.dispose()
    }

    private var disposed = AtomicBoolean(false)
    private var render = 0

    private var updaterThread: UpdaterThread? = null

    fun startDraw() {
        if (updaterThread == null) {
            updaterThread = UpdaterThread().also { it.start() }
        }
        render++
    }

    fun stopDraw() {
        render--
        if (render <= 0) {
            updaterThread?.interrupt()
            updaterThread = null
        }
    }

    private inner class UpdaterThread : Thread() {
        override fun run() {
            while (!isInterrupted) {
                if (!isFocusOwner)
                    requestFocus()
                repaint()
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }
}


class Vector2fProperty(x: Float = 0f, y: Float = 0f) : Vector2f(x, y) {
    private var changeFlag = true
    override var x: Float
        get() = super.x
        set(value) {
            if (!changeFlag && value != super.x)
                changeFlag = true
            super.x = value
        }

    override var y: Float
        get() = super.y
        set(value) {
            if (!changeFlag && value != super.y)
                changeFlag = true
            super.y = value
        }

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = true
        return b
    }
}

class Vector3fProperty(x: Float = 0f, y: Float = 0f, z: Float = 0f) : Vector3f(x, y, z) {
    private var changeFlag = true
    override var x: Float
        get() = super.x
        set(value) {
            if (!changeFlag && value != super.x)
                changeFlag = true
            super.x = value
        }

    override var y: Float
        get() = super.y
        set(value) {
            if (!changeFlag && value != super.y)
                changeFlag = true
            super.y = value
        }

    override var z: Float
        get() = super.z
        set(value) {
            if (!changeFlag && value != super.z)
                changeFlag = true
            super.z = value
        }

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = true
        return b
    }
}

class Vector4fProperty(x: Float = 0f, y: Float = 0f, z: Float = 0f, w: Float = 0f) : Vector4f(x, y, z, w) {
    private var changeFlag = true
    override var x: Float
        get() = super.x
        set(value) {
            if (!changeFlag && value != super.x)
                changeFlag = true
            super.x = value
        }

    override var y: Float
        get() = super.y
        set(value) {
            if (!changeFlag && value != super.y)
                changeFlag = true
            super.y = value
        }

    override var z: Float
        get() = super.z
        set(value) {
            if (!changeFlag && value != super.z)
                changeFlag = true
            super.z = value
        }

    override var w: Float
        get() = super.z
        set(value) {
            if (!changeFlag && value != super.z)
                changeFlag = true
            super.z = value
        }

    fun resetChangeFlag(): Boolean {
        val b = changeFlag
        changeFlag = true
        return b
    }
}