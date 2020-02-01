package pw.binom.sceneEditor

import mogot.*
import mogot.math.*
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.MouseListenerImpl
import pw.binom.sceneEditor.editors.EditorWithVirtualMouse
import pw.binom.sceneEditor.editors.Keys
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
            vertex.x += dx
            vertex.y += dy
            polygonEditor.updateGeom()
            oldMousePos.set(virtualMouse)
        }
    }

    override fun onStop() {
        super.onStop()
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

    private fun rebuildVertex(): FloatDataBuffer {
        if (vertexBuffer != null && vertexBuffer!!.size != vertexs.size * 2) {
            vertexBuffer?.close()
            vertexBuffer = null
        }
        if (vertexBuffer == null) {
            vertexBuffer = FloatDataBuffer.alloc(vertexs.size * 2)
        }
        val floatBuffer = vertexBuffer!!
        println("Vertex[${vertexs.size}]:")
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
            geom!!.indexBuffer.uploadArray(rebuildIndexes())
        }
    }

    private var needCheckGeom = false

    open fun updateGeom() {
        needCheckGeom = true
    }

    private val mouseListener = object : MouseListenerImpl {
        override fun mousePressed(e: MouseEvent) {
            val mousePos = engine.mathPool.vec2f.poll()
            view.editorCamera2D.screenToWorld(view.mousePosition, mousePos)
            val selectedVertex = vertexs.minBy { it.distanceSquaredTo(mousePos) }?.takeIf { it.distanceSquaredTo(mousePos) < 5f * 5f }
            engine.mathPool.vec2f.push(mousePos)

            if (selectedVertex != null) {
                view.startEditor(PolygonMoveEditor(this@PolygonEditor, selectedVertex))
            }
        }
    }

    init {
        view.addMouseListener(mouseListener)
    }

    override fun close() {
        view.removeMouseListener(mouseListener)
        geom = null
        circleGeom = null
        indexBuffer?.close()
        vertexBuffer?.close()
        vertexBuffer = null
        indexBuffer = null
        lineMat = null
        circleMat = null
        super.close()
    }

    open fun saveVertex() {

    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
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
        val mousePos = engine.mathPool.vec2f.poll()
        view.editorCamera2D.screenToWorld(view.mousePosition, mousePos)
        val selectedVertex = vertexs.minBy { it.distanceSquaredTo(mousePos) }?.takeIf { it.distanceSquaredTo(mousePos) < 5f * 5f }
        engine.mathPool.vec2f.push(mousePos)

        if (circleGeom == null) {
            circleGeom = Geoms.circle(engine.gl, 5f, 12)
        }

        engine.gl.gl.glLineWidth(1.5f)
        lineMat!!.use(model, projection, renderContext)
        geom!!.draw()
        engine.gl.gl.glLineWidth(1f)
        lineMat!!.unuse()

        val mat = engine.mathPool.mat4f.poll()
        val zoomScale = 1f / view.editorCamera2D.zoom
        vertexs.forEach {
            mat.set(model)
            mat.translate(it.x, it.y, 0f)
            mat.scale(zoomScale, zoomScale, 1f)
            circleMat!!.color.set(if (it == selectedVertex) circleHover else circleOut)
            circleMat!!.use(mat, projection, renderContext)
            circleGeom!!.draw()
            engine.gl.gl.glLineWidth(1f)
            circleMat!!.unuse()
        }
        engine.mathPool.mat4f.push(mat)
    }
}