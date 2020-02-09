package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Collider2D
import mogot.collider.Polygon2DCollider
import mogot.math.Matrix4fc
import mogot.math.Vector2f
import mogot.math.Vector4f
import mogot.math.getAxisAngleRotation
import mogot.physics.d2.shapes.PolygonShape2D
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.*
import javax.swing.Icon
import kotlin.collections.set

object PolygonShape2DCreator : NodeCreator {
    override val name: String
        get() = "PolygonShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = PolygonShape2D(view.engine)

        view.nodesMeta[node] = createMeta(view, node)
        return node
    }
}

private fun createMeta(view: SceneEditorView, node: PolygonShape2D): PolygonShape2DMeta {
    val viewNode = PolygonShape2DViwer(node)
    val center = CenterNode2D(node, view)
    center.parent = view.editorRoot
    center.visible = false
    viewNode.material.value = view.default3DMaterial.instance(Vector4f(0f, 1f, 0f, 0.5f))
    view.setRenderCallback(node, viewNode::render)
    return PolygonShape2DMeta(viewNode, Polygon2DCollider(node, viewNode.vertexs), center)
}

private class PolygonShape2DMeta(
        val shape: PolygonShape2DViwer,
        val polygon2DCollider: Polygon2DCollider,
        val centerNode2D: CenterNode2D
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



        view.nodesMeta[node] = createMeta(view, node)


        return node
    }

    fun getEditor(view: SceneEditorView, node: PolygonShape2D): PolygonEditor? {
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        return meta.polygonEditor
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
        val meta = view.nodesMeta[node] as PolygonShape2DMeta? ?: return
        meta.centerNode2D.visible = selected
        if (selected) {
            val editor = PolygonShapeEditor(node, view)
            editor.parent = view.editorRoot
            meta.polygonEditor = editor
        } else {
            meta.polygonEditor?.free()
        }
    }

    override fun isEditor(node: Node): Boolean = node::class.java == PolygonShape2D::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PolygonShape2D) return null
        val out = PolygonShape2D(node.engine)
        Spatial2DService.cloneSpatial2D(node, out)
        out.setVertex(node.getVertex())
        view.nodesMeta[out] = createMeta(view, out)
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
        view.engine.waitFrame {
            val meta = view.nodesMeta.remove(node) as PolygonShape2DMeta
            meta.centerNode2D.free()
            meta.polygonEditor?.free()
            meta.shape.free()
        }
        super.delete(view, node)
    }
}

class PolygonShape2DViwer(val node: PolygonShape2D) : VisualInstance2D(node.engine), MaterialNode by MaterialNodeImpl() {
    val vertexs = ArrayList<Vector2f>(node.getVertex().map { Vector2f(it) })
    private var geom by ResourceHolder<Geom2D>()
    private var indexBuffer: IntDataBuffer? = null
    private var vertexBuffer: FloatDataBuffer? = null

    private fun rebuildVertex(): FloatDataBuffer {
        if (true || (vertexBuffer != null && vertexBuffer!!.size != vertexs.size * 2)) {
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
            println("recreate index")
        } else {
            println("reset index")
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
            geom!!.uploadIndex(rebuildIndexes())
        }
    }

    private var needCheckGeom = false

    fun updatePositions() {
        node.setVertex(vertexs)
        needCheckGeom = true
    }

    override fun close() {
        engine.waitFrame {
            geom = null
            indexBuffer?.close()
            vertexBuffer?.close()
            vertexBuffer = null
            indexBuffer = null
            material.dispose()
        }
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
        val mat = engine.mathPool.mat4f.poll()
        val vec = engine.mathPool.vec3f.poll()
        val vec4 = engine.mathPool.vec4f.poll()
        position.set(0f, 0f)
        node.localToGlobal(position, position)
        node.globalToLocalMatrix(mat)
        mat.getTranslation(vec)
        mat.getAxisAngleRotation(vec4)
        rotation = -vec4.z * vec4.w

        engine.mathPool.vec4f.push(vec4)
        engine.mathPool.vec3f.push(vec)
        engine.mathPool.mat4f.push(mat)
//        this.rotation = node.rotation
//        this.position.set(node.position)
        super.update(delta)
    }
}