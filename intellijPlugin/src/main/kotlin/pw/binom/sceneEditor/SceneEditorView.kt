package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.GLView
import mogot.math.*
import mogot.rendering.CanvasFinalRenderPass
import mogot.rendering.Display
import mogot.rendering.SceneToTextureRenderPass
import pw.binom.MockFileSystem
import pw.binom.Services
import pw.binom.SolidMaterial
import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.sceneEditor.dragdrop.SceneDropTarget
import pw.binom.sceneEditor.editors.EditActionFactory
import pw.binom.sceneEditor.editors.Keys
import pw.binom.sceneEditor.nodeController.EditAnimateNode
import pw.binom.sceneEditor.struct.makeTreePath
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.BorderFactory
import javax.swing.tree.TreePath
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

private class EditorHolder(val view: SceneEditorView) : Closeable {
    override fun close() {
    }

}

val Engine.editor: SceneEditorView
    get() = manager<EditorHolder>("Editor") { throw IllegalStateException("View not found") }.view

class SceneEditorView(val viewPlane: ViewPlane, val editor1: SceneEditor, val project: Project, val file: VirtualFile, fps: Int?) : GLView(EditorDisplay(), MockFileSystem(), fps) {
    enum class Mode {
        D2,
        D3
    }

    val changeAnimationModeEvent = EventDispatcher()

    var animateNode: EditAnimateNode? = null
        set(value) {
            field = value
            changeAnimationModeEvent.dispatch()
            border = if (value != null) {
                BorderFactory.createLineBorder(Color.RED, 2)
            } else {
                null
            }
        }

    interface RenderCallback {
        val node: Node
        val model: Matrix4fc
        val modelView: Matrix4fc
        val projection: Matrix4fc
        val context: Display.Context
        val view: SceneEditorView
    }

    private inner class RenderCallbackImpl : RenderCallback {
        override lateinit var node: Node
        override lateinit var model: Matrix4fc
        override lateinit var modelView: Matrix4fc
        override lateinit var projection: Matrix4fc
        override lateinit var context: Display.Context
        override val view: SceneEditorView
            get() = this@SceneEditorView
    }

    private val renderCallback = RenderCallbackImpl()
    val selectors = ArrayList<Selector3D>()

    public override var render3D: Boolean
        get() = super.render3D
        set(value) {
            super.render3D = value
        }

    fun updateGuids() {
        viewPlane.guideTop.position = editorCamera2D.position.x
        viewPlane.guideLeft.position = editorCamera2D.position.y
    }

    override val display: EditorDisplay
        get() = super.display as EditorDisplay
    var mode = Mode.D2
        set(value) {
            field = value
            when (value) {
                Mode.D2 -> {
                    display.setCamera2D(editorCamera2D)
                    display.setCamera(null)
                    render3D = false
                    sceneRoot.walk {
                        if (it.isVisualInstance2D()) {
                            it.visible = true
                        }
                        true
                    }
                    viewPlane.guideVisible = true
                }
                Mode.D3 -> {
                    display.setCamera2D(null)
                    display.setCamera(editorCamera)
                    render3D = true
                    sceneRoot.walk {
                        if (it.isVisualInstance2D()) {
                            it.visible = false
                        }
                        true
                    }
                    viewPlane.guideVisible = false
                }
            }
        }

    val settings = Settings()
    lateinit var grid2d: Grid2D
    val editorRoot = Node()
    val sceneRoot = Node()
    lateinit var editorCamera: Camera
    lateinit var editorCamera2D: Camera2D
    val eventSelectChanged = EventDispatcher()
    private var closed = false
    private var inited = false
    private val frameListeners = Stack<() -> Unit>()
    private lateinit var _default3DMaterial: Default3DMaterial
    val default3DMaterial: Default3DMaterial
        get() = _default3DMaterial

    lateinit var default2DMaterial: Default2DMaterial
        private set

    override fun destroy() {
        _default3DMaterial.dec()
        default2DMaterial.dec()
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
    private val editorFactories by Services.byClassSequence(EditActionFactory::class.java)
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

    fun removeNode(node: Node) {
        println("Remove ${node::class.java.name}")
        var sceneNode = false
        var editorNode = false
        node.currentToRoot {
            if (it == sceneRoot) {
                sceneNode = true
                return@currentToRoot false
            }
            if (it == editorRoot) {
                editorNode = true
                return@currentToRoot false
            }
            true
        }

        fun removeChilds(node: Node) {
            val service = getService(node)
            if (service?.isInternalChilds(node) != true) {
                node.childs.forEach {
                    removeChilds(it)
                }
            }
            node.parent = null
            service?.delete(this, node)
            node.close()
        }

        if (sceneNode) {
            node.parent = null
            println("Remove Scene Node  ${engine.frameListeners.size}")
            engine.waitFrame {
                try {
                    println("Remove...")
                    removeChilds(node)
                    println("Removed!")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            return
        }

        if (editorNode) {
            node.parent = null
            engine.waitFrame {
                node.close()
            }
            return
        }
        throw IllegalArgumentException("Unknown node")
    }

    init {

        dropTarget = SceneDropTarget(this)
//        transferHandler = SceneTransferHandler(this)
        sceneRoot.parent = editorRoot
        sceneRoot.id = "Scene Root"
        updateOnEvent = true

        addMouseWheelListener {
            if (mode == Mode.D2) {
                val v = editorCamera2D.zoom - it.wheelRotation / 10f
                if (v > 0f && v < 5f) {
                    editorCamera2D.zoom = v
                    viewPlane.guideLeft.zoom = v
                    viewPlane.guideTop.zoom = v
                }
            }
        }

        root = editorRoot
    }

    private val selectedNodes = ArrayList<Node>()
    val selected: List<Node>
        get() = selectedNodes
    val nodesMeta = HashMap<Node, Any>()
    fun select(nodeList: List<Node>) {
        renderThread {

            selectedNodes.asSequence()
                    .filter { it !in nodeList }
                    .forEach { node ->
                        selectors.removeIf {
                            if (it.node === node) {
                                it.parent = null
                                it.close()
                                true
                            } else
                                false
                        }
                        getService(node)?.selected(this, node, false)
                    }

            nodeList.asSequence()
                    .filter { it !in selectedNodes }
                    .forEach { node ->
                        val service = getService(node)
                        service?.selected(this, node, true)
                        selectedNodes += node
                        if (service != null && node is Spatial) {
                            val aabb = AABB()
                            if (service.getAABB(node, aabb)) {
                                val s = Selector3D(engine, node)
                                s.parent = editorRoot
                                s.material.value = default3DMaterial.instance(Vector4f(1f))
                                selectors.add(s)

                                s.size.set(aabb.size.x * 1.1f, aabb.size.y * 1.1f, aabb.size.z * 1.1f)
                            }
                        }
                    }

            selectedNodes.clear()
            selectedNodes.addAll(nodeList)
            /*
            if (node != null) {
                val service = getService(node)
                service?.selected(this, node)
                selectedNodes += node
                if (service != null && node is Spatial) {
                    val aabb = AABB()
                    if (service.getAABB(node, aabb)) {
                        val s = Selector3D(engine, node)
                        s.parent = editorRoot
                        s.material.value = default3DMaterial
                        selectors.add(s)

                        s.size.set(aabb.size.x * 1.1f, aabb.size.y * 1.1f, aabb.size.z * 1.1f)
                    }
                }
            }
*/
            eventSelectChanged.dispatch()
        }
    }

    var editor: EditAction? = null
        private set

    private val mouseDownListeners = ArrayList<(MouseEvent) -> Boolean>()
    private val mouseUpListeners = ArrayList<(MouseEvent) -> Boolean>()

    fun addMouseDownListener(listener: (MouseEvent) -> Boolean): Closeable {
        mouseDownListeners += listener
        return object : Closeable {
            override fun close() {
                mouseDownListeners -= listener
            }
        }
    }

    fun addUpListener(listener: (MouseEvent) -> Boolean): Closeable {
        mouseUpListeners += listener
        return object : Closeable {
            override fun close() {
                mouseUpListeners -= listener
            }
        }
    }

    override fun mouseDown(e: MouseEvent) {
        this.requestFocus()
        if (editor != null) {
            editor!!.mouseDown(e)
            return
        }
        editorFactories.forEach {
            it.mouseDown(this, e)
            if (editor != null)
                return
        }
        if (mouseDownListeners.isNotEmpty())
            mouseDownListeners.toTypedArray().forEach {
                if (!it(e))
                    return
            }
        super.mouseDown(e)
    }

    private fun getNode3D(x: Int, y: Int): Node? {
        val ray = editorCamera.screenPointToRay(x, y, MutableRay())
        val list = ArrayList<Pair<Node, Float>>()
        sceneRoot.walk {
            val service = getService(it) ?: return@walk true
            val col = service.getCollider(it) ?: return@walk true
            val vec = Vector3f()
            if (!col.rayCast(ray, vec))
                return@walk true
            list += it to vec.sub(editorCamera.position).lengthSquared
            true
        }
        var result = list.minBy { it.second }?.first
        result?.currentToRoot {
            if (getService(it)?.isInternalChilds(it) == true) {
                result = it
                return@currentToRoot false
            }
            return@currentToRoot true
        }
        return result
    }

    private fun getNode2D(x: Int, y: Int): Node? {
        var result: Node? = null
        val pos = editorCamera2D.screenToWorld(x, y, Vector2f())
        sceneRoot.walk {
            val service = getService(it) ?: return@walk true
            val col = service.getCollider2D(this, it) ?: return@walk true
            if (col.test(pos.x, pos.y)) {
                result = it
                return@walk false
            }
            true
        }
        result?.currentToRoot {
            if (getService(it)?.isInternalChilds(it) == true) {
                result = it
                return@currentToRoot false
            }
            return@currentToRoot true
        }
        return result
    }

    override fun mouseUp(e: MouseEvent) {
        this.requestFocus()
        if (editor != null) {
            editor!!.mouseUp(e)
            return
        }

        mouseUpListeners.forEach {
            if (!it(e))
                return
        }

        val shift = isKeyDown(16)
        val l = ArrayList<Node>()
        if (shift)
            l.addAll(selectedNodes)
        val node = when (mode) {
            Mode.D3 -> getNode3D(e.x, e.y)
            Mode.D2 -> getNode2D(e.x, e.y)
        }
        if (node != null)
            l.add(node)
        editor1.sceneStruct.tree.selectionModel.selectionPaths = l.map {
            it.makeTreePath()
        }.toTypedArray()
        super.mouseDown(e)
    }

    override fun keyDown(e: KeyEvent) {
        if (editor != null) {
            editor!!.keyDown(e.keyCode)
            return
        }
        editorFactories.forEach {
            it.keyDown(this, e)
            if (editor != null)
                return
        }
        if (e.keyCode == Keys.M) {
            mode = when (mode) {
                Mode.D2 -> Mode.D3
                Mode.D3 -> Mode.D2
            }
            return
        }

        if (e.keyCode == Keys.X) {
            viewPlane.guideVisible = !viewPlane.guideVisible
            return
        }
        super.keyDown(e)
    }

    private var hover: Node? = null
    override fun render2(dt: Float) {
        if (editor == null) {
            var node = when (mode) {
                Mode.D3 -> {
                    getNode3D(mousePosition.x, mousePosition.y)
                }
                Mode.D2 -> {
                    getNode2D(mousePosition.x, mousePosition.y)
                }
            }

            if (node != null) {
                if (hover != node) {
                    val service = getService(node)
                    if (service != null) {
                        hover?.let { getService(it)?.hover(this, it, false) }
                        node.let { getService(it)?.hover(this, it, true) }
                        hover = node
                    }
                }
            } else {
                hover?.let { getService(it)?.hover(this, it, false) }
                hover = null
            }

        }
        editor?.render(dt)
        super.render2(dt)
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

    override val update2DPhysics: Boolean
        get() = false

    fun startEditor(editor: EditAction) {
        this.editor?.onStop()
        this.editor = editor
//        startRender()
    }

    fun stopEditing() {
        editor?.onStop()
        editor = null
//        stopRender()
        repaint()
        save()
    }

    private val node2DRenderCallback = HashMap<Spatial2D, (RenderCallback) -> Unit>()

    fun setRenderCallback(node: Spatial2D, callback: (RenderCallback) -> Unit) {
        if (node2DRenderCallback.containsKey(node))
            throw IllegalStateException("Node ${node.id ?: node::class.java.simpleName} already has some callback")
        node2DRenderCallback[node] = callback
    }

    fun clearRenderCallback(node: Spatial2D) {
        node2DRenderCallback.remove(node)
    }

    override fun keyUp(e: KeyEvent) {
        if (editor != null) {
            editor!!.keyUp(e.keyCode)
            return
        }
        super.keyUp(e)
    }

    /**
     * Refresh UI Scene Tree component
     */
    private fun updateSceneTreeSelection() {
        editor1.sceneStruct.tree.selectionModel.selectionPaths = selected.map {
            TreePath(it.fullPath().toTypedArray())
        }.toTypedArray()
    }

    private lateinit var grid3d: Grid3D

    override fun init() {
        super.init()
        engine.manager("Editor") { EditorHolder(this) }
        _default3DMaterial = Default3DMaterial(engine.gl)
        _default3DMaterial.inc()

        default2DMaterial = Default2DMaterial(engine.gl)
        default2DMaterial.inc()

        grid3d = Grid3D(gl)
        grid3d.parent = editorRoot

        grid3d.material.value = default3DMaterial.instance(Vector4f(1f, 1f, 1f, 1f))

        editorCamera = Camera(engine)
        editorCamera.parent = editorRoot
        (display as EditorDisplay).setCamera(editorCamera)

        editorCamera.position.set(3f, 3f, 3f)
        editorCamera.lookTo(Vector3f(0f, 0f, 0f))

        editorCamera2D = Camera2D(engine)
        editorCamera2D.parent = editorRoot
        editorCamera2D.enabled = true

        grid2d = Grid2D(this)
        (display as EditorDisplay).canvasRender.grid = grid2d



        backgroundColor.set(0.376f, 0.376f, 0.376f, 1f)

//        editorCamera.behaviour = FpsCam(engine)
        selectorMaterial = SolidMaterial(engine.gl)


        SceneFileLoader.load(this, file)
        mode = Mode.D3
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
        closed = true
        super.dispose()
    }

    private var disposed = AtomicBoolean(false)
    private var render = 0
}