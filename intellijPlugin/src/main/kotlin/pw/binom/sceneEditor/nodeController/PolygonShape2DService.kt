package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.*
import mogot.math.*
import mogot.physics.d2.shapes.PolygonShape2D
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.PolygonEditor
import pw.binom.sceneEditor.SceneEditorView
import javax.swing.Icon
import kotlin.collections.set

object PolygonShape2DCreator : NodeCreator {
    override val name: String
        get() = "PolygonShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = PolygonShape2D(view.engine)
        val viewNode = PolygonShape2DViwer(node)
        view.nodesMeta[node] = viewNode
        view.setRenderCallback(node, viewNode::render)
        viewNode.material.value = view.default3DMaterial.instance(Vector4f(0f, 1f, 0f, 0.5f))
        return node
    }

}

private class PolygonShape2DMeta(
        val shape: PolygonShape2DViwer,
        val polygon2DCollider: Polygon2DCollider
) {
    var polygonEditor: PolygonShapeEditor? = null
}

object PolygonShape2DService : NodeService {
    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != PolygonShape2D::class.java.name)
            return null
        val vertex = properties["vertex"]?.split('|')?.map {
            val items = it.split('+')
            Vector2f(
                    items.getOrNull(0)?.toFloatOrNull() ?: 0f,
                    items.getOrNull(1)?.toFloatOrNull() ?: 0f
            )
        } ?: emptyList()
        val node = PolygonShape2D(view.engine)
        Spatial2DService.load(view.engine, node, properties)
        node.setVertex(vertex)
        val viewNode = PolygonShape2DViwer(node)
        val meta = PolygonShape2DMeta(viewNode, Polygon2DCollider(node, viewNode.vertexs))
        view.nodesMeta[node] = meta
        view.setRenderCallback(node, viewNode::render)
        viewNode.material.value = view.default3DMaterial.instance(Vector4f(0f, 1f, 0f, 0.5f))
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is PolygonShape2D) return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, out)
        out["vertex"] = node.getVertex().map { "${it.x}+${it.y}" }.joinToString("|")
        return out
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        node as PolygonShape2D
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        if (selected) {
            val editor = PolygonShapeEditor(node, view)
            editor.parent = view.editorRoot
            meta.polygonEditor = editor
        } else {
            meta.polygonEditor?.let {
                it.parent = null
                it.close()
            }
        }
    }

    override fun isEditor(node: Node): Boolean = node::class.java == PolygonShape2D::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PolygonShape2D) return null
        val out = PolygonShape2D(node.engine)
        out.setVertex(node.getVertex())
        val viewNode = PolygonShape2DViwer(out)
        val meta = PolygonShape2DMeta(viewNode, Polygon2DCollider(node, viewNode.vertexs))
        view.nodesMeta[out] = meta
        view.setRenderCallback(out, viewNode::render)
        return out
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        node as PolygonShape2D
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        return meta.polygon2DCollider
    }

    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is PolygonShape2D) return
        view.clearRenderCallback(node)
        view.nodesMeta.remove(node)
        super.delete(view, node)
    }
}

class PolygonShape2DViwer(val node: PolygonShape2D) : VisualInstance2D(node.engine), MaterialNode by MaterialNodeImpl() {
    val vertexs = ArrayList<Vector2f>(node.getVertex().map { Vector2f(it) })
    private var geom by ResourceHolder<Geom2D>()
    private var indexBuffer: IntDataBuffer? = null
    private var vertexBuffer: FloatDataBuffer? = null

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
        }
        return floatBuffer
    }

    private fun rebuildIndexes(): IntDataBuffer {
        val indexes = Sprite.calcPolygonTriangulation(vertexs)
        if (indexBuffer != null && indexBuffer!!.size != indexes.size) {
            indexBuffer?.close()
            indexBuffer = null
        }
        if (indexBuffer == null) {
            indexBuffer = IntDataBuffer.alloc(indexes.size)
        }
        val intBuffer = indexBuffer!!
        indexes.forEachIndexed { index, i ->
            intBuffer[index] = i
        }
        return intBuffer
    }

    private fun checkGeom() {
        if (geom == null) {
            geom = Geom2D(engine.gl, rebuildIndexes(), rebuildVertex(), null, null)
        } else {
            geom!!.vertexBuffer.uploadArray(rebuildVertex())
            geom!!.indexBuffer.uploadArray(rebuildIndexes())
        }
    }

    private var needCheckGeom = false

    fun updatePositions() {
        needCheckGeom = true
        node.setVertex(vertexs)
    }

    override fun close() {
        geom = null
        indexBuffer?.close()
        vertexBuffer?.close()
        vertexBuffer = null
        indexBuffer = null
        material.dispose()
        super.close()
    }

    fun render(callback: SceneEditorView.RenderCallback) {
        render(callback.model, callback.projection, callback.renderContext)
    }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        val mat = material.value ?: return
        if (vertexs.size < 3)
            return
        if (geom == null || needCheckGeom) {
            checkGeom()
            needCheckGeom = false
        }
        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
    }
}

private class PolygonShapeEditor(val node: PolygonShape2D, view: SceneEditorView) : PolygonEditor(view) {
    init {
        vertexs.addAll(node.getVertex().map { Vector2f(it) })
    }

    override fun updateGeom() {
        super.updateGeom()
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        meta.shape.vertexs.clear()
        meta.shape.vertexs.addAll(vertexs)
        meta.shape.updatePositions()
    }

    override fun saveVertex() {
        node.setVertex(vertexs)
    }

    override fun update(delta: Float) {
        this.rotation = node.rotation
        this.position.set(node.position)
    }
}