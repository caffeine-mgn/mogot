package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.GLView
import mogot.math.AABB
import mogot.math.MutableRay
import mogot.math.Vector3f
import mogot.math.sub
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
import javax.swing.tree.TreePath
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.asSequence
import kotlin.collections.forEach
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.minBy
import kotlin.collections.plus
import kotlin.collections.plusAssign
import kotlin.collections.reversed
import kotlin.collections.set
import kotlin.collections.toTypedArray

private class EditorHolder(val view: SceneEditorView) : Closeable {
    override fun close() {
    }

}

val Engine.editor: SceneEditorView
    get() = manager<EditorHolder>("Editor") { throw IllegalStateException("View not found") }.view

class SceneEditorView(val editor1: SceneEditor, val project: Project, val file: VirtualFile, fps: Int?) : GLView(MockFileSystem(), fps) {
    enum class Mode {
        D2,
        D3
    }

    val mode = Mode.D2

    val editorRoot = Node()
    val sceneRoot = Node()
    val editorCamera = Camera()
    val editorCamera2D = Camera2D()
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
        editorCamera2D.parent = editorRoot
        camera = editorCamera
        camera2D = editorCamera2D


        editorCamera.position.set(3f, 3f, 3f)
        editorCamera.lookTo(Vector3f(0f, 0f, 0f))
    }

    private val selectedNodes = ArrayList<Node>()
    val selected: List<Node>
        get() = selectedNodes
    private var selectors = ArrayList<Selector3D>()
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
                        getService(node)?.unselected(this, node)
                    }

            nodeList.asSequence()
                    .filter { it !in selectedNodes }
                    .forEach { node ->
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
        return list.minBy { it.second }?.first
    }

    private fun getNode2D(x: Int, y: Int): Node? {
        var result: Node? = null
        sceneRoot.walk {
            val service = getService(it) ?: return@walk true
            val col = service.getCollider2D(it) ?: return@walk true
            if (col.test(x.toFloat(), y.toFloat())) {
                result = it
                return@walk false
            }
            true
        }
        return result
    }

    override fun mouseUp(e: MouseEvent) {
        this.requestFocus()
        if (editor != null) {
            editor!!.mouseUp(e)
            return
        }

        val shift = isKeyDown(16)
        val l = ArrayList<Node>()
        if (shift)
            l.addAll(selectedNodes)
        val node = when (mode){
            Mode.D3->getNode3D(e.x, e.y)
            Mode.D2->getNode2D(e.x, e.y)
        }
        if (node != null)
            l.add(node)
        editor1.sceneStruct.tree.selectionModel.selectionPaths = l.map {
            val bb = it.asUpSequence().filter { it != root }.toList().reversed() + it
            TreePath(bb.toTypedArray())
        }.toTypedArray()
        //updateSceneTreeSelection()
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

    private var hover: Node? = null
    override fun render2(dt: Float) {
        if (editor == null) {
            val node = when (mode) {
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
                        hover?.let { getService(it)?.hover(it, false) }
                        node.let { getService(it)?.hover(it, true) }
                        hover = node
                    }
                }
            } else {
                hover?.let { getService(it)?.hover(it, false) }
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

    fun startEditor(editor: EditAction) {
        this.editor?.onStop()
        this.editor = editor
//        startRender()
        println("startEditor")
    }

    fun stopEditing() {
        println("stopEditing")
        editor?.onStop()
        editor = null
//        stopRender()
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

    /**
     * Refresh UI Scene Tree component
     */
    private fun updateSceneTreeSelection() {
        editor1.sceneStruct.tree.selectionModel.selectionPaths = selected.map {
            TreePath(it.fullPath().toTypedArray())
        }.toTypedArray()
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
}