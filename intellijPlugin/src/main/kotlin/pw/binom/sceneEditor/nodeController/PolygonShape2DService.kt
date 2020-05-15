package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Collider2D
import mogot.collider.Polygon2DCollider
import mogot.math.*
import mogot.physics.d2.PhysicsBody2D
import mogot.physics.d2.shapes.PolygonShape2D
import mogot.rendering.Display
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.polygonEditor.PolygonEditor
import pw.binom.sceneEditor.properties.*
import pw.binom.utils.Vector2fmDelegator
import javax.swing.Icon
import kotlin.collections.set

private val SHAPE_VIEW_COLOR = Vector4f.fromColor(128, 255, 0, 0)

object PolygonShape2DCreator : NodeCreator {
    override val name: String
        get() = "PolygonShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = PolygonShape2DViwer(view)
        node.vertexs = listOf(
                Vector2f(-50f, -50f),
                Vector2f(-50f, 50f),
                Vector2f(50f, 50f),
                Vector2f(50f, -50f)
        )
        node.material.value = view.default3DMaterial.instance(SHAPE_VIEW_COLOR)
        view.nodesMeta[node] = createMeta(view, node)
        return node
    }
}

private fun createMeta(view: SceneEditorView, node: PolygonShape2DViwer): PolygonShape2DMeta {
    val center = CenterNode2D(node, view)
    center.parent = view.editorRoot
    center.visible = false
    return PolygonShape2DMeta(
            Polygon2DCollider(node, node.vertexs),
            center
    )
}

private class PolygonShape2DMeta(
        val polygon2DCollider: Polygon2DCollider,
        val centerNode2D: CenterNode2D
) {
    var polygonEditor: PolygonShapeEditor? = null
}

object PolygonShape2DService : NodeService {

    private val properties = listOf(Transform2DPropertyFactory, PhysicsShapePropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String = PolygonShape2D::class.java.name

    fun getEditor(view: SceneEditorView, node: PolygonShape2DViwer): PolygonEditor? {
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        return meta.polygonEditor
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        node as PolygonShape2DViwer
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        meta.centerNode2D.visible = selected
        if (selected) {
            val editor = PolygonShapeEditor(node, view)
            editor.parent = view.editorRoot
            meta.polygonEditor = editor
        } else {
            meta.polygonEditor?.free()
        }
    }

    override fun isEditor(node: Node): Boolean = node::class.java == PolygonShape2DViwer::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PolygonShape2DViwer) return null
        val out = PolygonShape2DViwer(view)
        Spatial2DService.cloneSpatial2D(node, out)
        PhysicsShapeUtils.clone(node, out)
        out.vertexs = ArrayList(node.vertexs)
        out.sensor = node.sensor
        view.nodesMeta[out] = createMeta(view, out)
        return out
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        node as PolygonShape2DViwer
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        return meta.polygon2DCollider
    }

    override val nodeClass: String
        get() = PolygonShape2D::class.java.name

    override fun newInstance(view: SceneEditorView): Node {
        val node = PolygonShape2DViwer(view)
        view.nodesMeta[node] = createMeta(view, node)
        return node
    }

    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is PolygonShape2DViwer) return
        view.engine.waitFrame {
            val meta = view.nodesMeta.remove(node) as PolygonShape2DMeta
            meta.centerNode2D.free()
            meta.polygonEditor?.free()
        }
        super.delete(view, node)
    }
}

class PolygonShape2DViwer(view: SceneEditorView) : VisualInstance2D(view.engine), ShapeEditorNode, MaterialNode by MaterialNodeImpl(), EditableNode {

    val transformField = PositionField2D(this)
    val rotationField = RotationField2D(this)
    val densityEditableField = DensityEditableField(this, this)
    val frictionEditableField = FrictionEditableField(this, this)
    val restitutionEditableField = RestitutionEditableField(this, this)
    val sensorEditableField = SensorEditableField(this, this)

    private val fields = listOf(transformField, rotationField, densityEditableField, frictionEditableField, restitutionEditableField, sensorEditableField)
    override fun getEditableFields(): List<NodeService.Field> = fields

    override var rotation: Float
        get() = super.rotation
        set(value) {
            super.rotation = value
            rotationField.eventChange.dispatch()
        }
    override val position: Vector2fm = Vector2fmDelegator(super.position) {
        transformField.eventChange.dispatch()
    }

    var vertexs: List<Vector2f> = emptyList()
        set(value) {
            field = value
            needCheckGeom = true
        }
    private var geom by ResourceHolder<Geom2D>()
    private var indexBuffer: IntDataBuffer? = null
    private var vertexBuffer: FloatDataBuffer? = null

    private val body
        get() = parent as? PhysicsBody2D

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
        val indexes = AbstractSprite.calcPolygonTriangulation(vertexs)
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
        render(callback.model, callback.modelView, callback.projection, callback.context)
    }


    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        val mat = material.value ?: run {
            println("Material not set")
            return
        }
        if (vertexs.size < 3) {
            println("Vertex count less than 3")
            return
        }
        if (geom == null || needCheckGeom) {
            checkGeom()
            needCheckGeom = false
        }
        mat.use(model, modelView, projection, context)
        geom!!.draw()
        mat.unuse()
    }

    override var sensor: Boolean = false
    override var density: Float = 1f
    override var friction: Float = 0.5f
    override var restitution: Float = 0.2f
}

private class PolygonShapeEditor(val node: PolygonShape2DViwer, view: SceneEditorView) : PolygonEditor(view) {
    init {
        vertexs.addAll(node.vertexs)
    }

    override fun updateGeom() {
        super.updateGeom()
        val meta = view.nodesMeta[node] as PolygonShape2DMeta
        node.vertexs = vertexs
    }

    override fun saveVertex() {
        node.vertexs = vertexs
    }

    override fun update(delta: Float) {
        val mat = engine.mathPool.mat4f.poll()
        val vec = engine.mathPool.vec3f.poll()
        val vec4 = engine.mathPool.vec4f.poll()
        val q = engine.mathPool.quatf.poll()
        position.set(0f, 0f)
        node.localToGlobal(position, position)
        node.globalToLocalMatrix(mat)
        q.setFromUnnormalized(mat)
        q.getEulerAnglesXYZ(vec)
        mat.getAxisAngleRotation(vec4)

        rotation = -vec.z

        engine.mathPool.quatf.push(q)
        engine.mathPool.vec4f.push(vec4)
        engine.mathPool.vec3f.push(vec)
        engine.mathPool.mat4f.push(mat)
//        this.rotation = node.rotation
//        this.position.set(node.position)
        super.update(delta)
    }
}