package pw.binom.sceneEditor.polygonEditor

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.MouseShortcut
import mogot.*
import mogot.math.*
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.MInstance
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.action.AddPolygonAction
import pw.binom.sceneEditor.action.RemovePolygonAction
import pw.binom.sceneEditor.editors.EditorWithVirtualMouse
import pw.binom.sceneEditor.editors.Keys
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

class PolygonMoveEditor(val polygonEditor: PolygonEditor, val vertex: Vector2f) : EditorWithVirtualMouse(polygonEditor.view) {

    private val oldMousePos = Vector2i(virtualMouse)
    private val initPos = Vector2f(vertex)

    override fun keyUp(code: Int) {
        super.keyUp(code)
        if (code == Keys.ESCAPE) {
            resetInitPosition()
            polygonEditor.view.stopEditing()
            polygonEditor.updateGeom()
        }
    }

    override fun resetInitPosition() {
        vertex.set(initPos)
    }

    override fun mouseUp(e: MouseEvent) {
        polygonEditor.view.stopEditing()
        polygonEditor.updateGeom()
        polygonEditor.saveVertex()
    }

    override fun render(dt: Float) {
        super.render(dt)
        val dx = virtualMouse.x - oldMousePos.x
        val dy = virtualMouse.y - oldMousePos.y
        if (dx != 0 || dy != 0) {
            val v = engine.mathPool.vec2f.poll()
            v.set(vertex)
            polygonEditor.localToGlobal(v, v)
            if (dx != 0)
                v.x += dx / polygonEditor.view.editorCamera2D.zoom
            if (dy != 0)
                v.y += dy / polygonEditor.view.editorCamera2D.zoom
            polygonEditor.globalToLocal(v, v)
            vertex.set(v)
            polygonEditor.updateGeom()
            oldMousePos.set(virtualMouse)
        }
    }

}

open class PolygonEditor(val view: SceneEditorView) : VisualInstance2D(view.engine) {
    companion object {
        val circleHover = Vector4f.fromColor(255, 255, 0, 0)
        val circleOut = Vector4f.fromColor(128, 255, 0, 0)
    }

    val vertexs = ArrayList<Vector2f>()
    private var indexBuffer: IntDataBuffer? = null
    private var vertexBuffer: FloatDataBuffer? = null

    private var geom by ResourceHolder<Geom2D>()
    private var circleGeom by ResourceHolder<Geom2D>()
    private var lineMat by ResourceHolder<MInstance>()
    private var circleMat by ResourceHolder<MInstance>()
    private var addPolygonNode = AddPolygonNode(view).parent(this)
    private var deletePolygonNode = DeletePolygonNode(view).parent(this)


    private fun rebuildVertex(): FloatDataBuffer {
        if (vertexBuffer != null && vertexBuffer!!.size != vertexs.size * 2) {
            vertexBuffer?.close()
            vertexBuffer = null
        }
        if (vertexBuffer == null) {
            vertexBuffer = FloatDataBuffer.alloc(vertexs.size * 2)
        }
        val floatBuffer = vertexBuffer!!
        vertexs.forEachIndexed { index, pos ->
            floatBuffer[(index * 2) + 0] = pos.x
            floatBuffer[(index * 2) + 1] = pos.y
            println("${pos.x} ${pos.y}")
        }
        return floatBuffer
    }

    private fun rebuildIndexes(): IntDataBuffer {
        val size = vertexs.size + 1
        if (indexBuffer != null && indexBuffer!!.size != size) {
            indexBuffer?.close()
            indexBuffer = null
        }
        if (indexBuffer == null) {
            indexBuffer = IntDataBuffer.alloc(size)
        }
        val intBuffer = indexBuffer!!
        (0 until intBuffer.size).forEach {
            intBuffer[it] = it
        }
        intBuffer[vertexs.size] = 0
        return intBuffer
    }

    private fun checkGeom() {
        if (geom == null) {
            geom = Geom2D(engine.gl, rebuildIndexes(), rebuildVertex(), null, null)
            geom!!.mode = Geometry.RenderMode.LINES_STRIP
        } else {
            geom!!.vertexBuffer.uploadArray(rebuildVertex())
            geom!!.uploadIndex(rebuildIndexes())
        }
    }

    private var needCheckGeom = false

    open fun updateGeom() {
        needCheckGeom = true
    }

    private val addPolygonAction = ActionManager.getInstance().getAction(AddPolygonAction::class.java.name)
    private val removePolygonAction = ActionManager.getInstance().getAction(RemovePolygonAction::class.java.name)

    private val mouseDownListener = view.addMouseDownListener { e ->
        var result = true
        val mousePos = engine.mathPool.vec2f.poll()
        view.editorCamera2D.screenToWorld(e.x, e.y, mousePos)
        globalToLocal(mousePos, mousePos)
        val selectedVertex = vertexs.minBy { it.distanceSquaredTo(mousePos) }?.takeIf { it.distanceSquaredTo(mousePos) < 5f * 5f }


        if (selectedVertex != null) {
            view.startEditor(PolygonMoveEditor(this@PolygonEditor, selectedVertex))
            result = false
        }
        engine.mathPool.vec2f.push(mousePos)
        result
    }

    fun addPoint(index: Int, position: Vector2fc) {
        if (view.editor is PolygonMoveEditor)
            view.stopEditing()
        check(index >= 0)
        check(index <= vertexs.size)
        vertexs.add(index, Vector2f(position))
        updateGeom()
    }

    fun removePoint(index: Int) {
        if (view.editor is PolygonMoveEditor)
            view.stopEditing()
        check(index >= 0)
        check(index < vertexs.size)
        vertexs.removeAt(index)
        updateGeom()
    }

    override fun close() {
        engine.waitFrame {
            geom = null
            circleGeom = null
            indexBuffer?.close()
            vertexBuffer?.close()
            vertexBuffer = null
            indexBuffer = null
            lineMat = null
            circleMat = null
            addPolygonNode.close()
        }
        mouseDownListener.close()
        super.close()
    }

    open fun saveVertex() {

    }

    private fun index(index: Int): Vector2f {
        if (index == 0)
            return vertexs[0]
        return if (index > 0) {
            vertexs[index % vertexs.size]
        } else {
            vertexs[(vertexs.size - (index % -vertexs.size))]
        }
    }

    private val DISTATION_TO_POINT = 10f
    var selectedPoint: Int? = null
        private set

    var newPointPosition: Pair<Int, Vector2fc>? = null
        private set

    private val distationToPoint
        get() = DISTATION_TO_POINT / view.editorCamera2D.zoom

    override fun update(delta: Float) {
        if (view.editor != null && view.editor !is PolygonMoveEditor) {
            selectedPoint = null
            newPointPosition = null
            return
        }
        val mousePos = engine.mathPool.vec2f.poll()
        view.editorCamera2D.screenToWorld(view.mousePosition, mousePos)
        globalToLocal(mousePos, mousePos)
        selectedPoint = (0 until vertexs.size)
                .minBy { vertexs[it].distanceSquaredTo(mousePos) }
                ?.takeIf { vertexs[it].distanceSquaredTo(mousePos) < distationToPoint * distationToPoint }

        val addPolygonActive = addPolygonAction.shortcutSet.shortcuts.any {
            if (!it.isKeyboard) {
                it as MouseShortcut
                (
                        (it.modifiers and InputEvent.CTRL_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_CONTROL))
                                || (it.modifiers and InputEvent.ALT_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_ALT))
                                || (it.modifiers and InputEvent.SHIFT_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_SHIFT))
                                || (it.modifiers and InputEvent.META_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_META))
                                || (it.modifiers and InputEvent.ALT_GRAPH_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_ALT_GRAPH))
                        )
            } else
                false
        }
        val removePolygonActive = removePolygonAction.shortcutSet.shortcuts.any {
            if (!it.isKeyboard) {
                it as MouseShortcut
                (
                        (it.modifiers and InputEvent.CTRL_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_CONTROL))
                                || (it.modifiers and InputEvent.ALT_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_ALT))
                                || (it.modifiers and InputEvent.SHIFT_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_SHIFT))
                                || (it.modifiers and InputEvent.META_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_META))
                                || (it.modifiers and InputEvent.ALT_GRAPH_DOWN_MASK != 0 && view.isKeyDown(KeyEvent.VK_ALT_GRAPH))
                        )
            } else
                false
        }

        if (selectedPoint != null && removePolygonActive) {
            deletePolygonNode.visible = true
            deletePolygonNode.scale.set(1f / view.editorCamera2D.zoom, 1f / view.editorCamera2D.zoom)
            deletePolygonNode.rotation = -rotation
            deletePolygonNode.position.set(mousePos)
        } else {
            deletePolygonNode.visible = false
        }

        if (selectedPoint == null && addPolygonActive) {
            val closesPoint = engine.mathPool.vec2f.poll()
            val newPoint2 = vertexs.indices.asSequence().map {
                val a = index(it)
                val b = index(it + 1)
                Intersectionf.findClosestPointOnLineSegment(a.x, a.y, b.x, b.y, mousePos.x, mousePos.y, closesPoint)
                it to closesPoint.distanceSquaredTo(mousePos)
            }.minBy { it.second }?.takeIf { it.second <= distationToPoint * distationToPoint }
            if (newPoint2 == null) {
                addPolygonNode.visible = false
                newPointPosition = null
            } else {
                val a = index(newPoint2.first)
                val b = index(newPoint2.first + 1)
                Intersectionf.findClosestPointOnLineSegment(a.x, a.y, b.x, b.y, mousePos.x, mousePos.y, closesPoint)
                addPolygonNode.scale.set(1f / view.editorCamera2D.zoom, 1f / view.editorCamera2D.zoom)
                addPolygonNode.rotation = -rotation
                addPolygonNode.position.set(closesPoint)
                addPolygonNode.visible = true
                newPointPosition = newPoint2.first to addPolygonNode.position
            }
            engine.mathPool.vec2f.push(closesPoint)
        } else {
            addPolygonNode.visible = false
            newPointPosition = null
        }

        engine.mathPool.vec2f.push(mousePos)
        super.update(delta)
    }

    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (lineMat == null) {
            lineMat = view.default3DMaterial.instance(Vector4f.fromColor(255, 255, 102, 0))
            circleMat = view.default3DMaterial.instance(circleOut)
        }
        if (vertexs.size < 3)
            return

        if (geom == null || needCheckGeom) {
            checkGeom()
            needCheckGeom = false
        }



        if (circleGeom == null) {
            circleGeom = Geoms.circle(engine.gl, 5f, 12)
        }

        engine.gl.gl.glLineWidth(1.5f)
        lineMat!!.use(model, modelView, projection, context)
        geom!!.draw()
        engine.gl.gl.glLineWidth(1f)
        lineMat!!.unuse()

        val mat = engine.mathPool.mat4f.poll()
        val zoomScale = 1f / view.editorCamera2D.zoom
        vertexs.forEachIndexed { index, it ->
            mat.set(model)
            mat.translate(it.x, it.y, 0f)
            mat.scale(zoomScale, zoomScale, 1f)
            circleMat!!.color.set(if (index == selectedPoint) circleHover else circleOut)
            circleMat!!.use(mat, modelView, projection, context)
            circleGeom!!.draw()
            engine.gl.gl.glLineWidth(1f)
            circleMat!!.unuse()
        }
        engine.mathPool.mat4f.push(mat)
    }
}