package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Circle2DCollider
import mogot.collider.Collider2D
import mogot.math.Matrix4fc
import mogot.math.Vector4f
import mogot.math.set
import mogot.physics.d2.PhysicsBody2D
import mogot.physics.d2.shapes.CircleShape2D
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.*
import javax.swing.Icon

object CircleShapeNodeCreator : NodeCreator {
    override val name: String
        get() = "CircleShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        return CircleShape2DView(view)
    }

}

object CircleShapeService : NodeService {

    private val properties = listOf(Transform2DPropertyFactory, CircleShape2DPropertyFactory, PhysicsShapePropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String =
            CircleShape2D::class.java.name

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != CircleShape2D::class.java.name)
            return null
        val node = CircleShape2DView(view)
        Spatial2DService.load(view.engine, node, properties)
        node.radius = properties["radius"]?.toFloatOrNull() ?: 50f
        PhysicsShapeUtils.load(node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is CircleShape2DView) return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, out)
        out["radius"] = node.radius.toString()
        PhysicsShapeUtils.save(node, out)
        return out
    }

    override fun isEditor(node: Node): Boolean = node::class.java === CircleShape2DView::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is CircleShape2DView) return null
        val out = CircleShape2DView(view)
        PhysicsShapeUtils.clone(node, out)
        Spatial2DService.cloneSpatial2D(node, out)
        out.radius = node.radius
        return out
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        if (node !is CircleShape2DView) return
        node.selected = selected
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        if (node !is CircleShape2DView) return
        node.hover = hover
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        if (node !is CircleShape2DView) return null
        return node.collider
    }
}

class CircleShape2DView(val view: SceneEditorView) : VisualInstance2D(view.engine), ShapeEditorNode {
    private var geom by ResourceHolder<Geom2D>()
    private var material by ResourceHolder(view.default3DMaterial.instance(Vector4f()))
    private var center: CenterNode2D? = null
    val collider = Circle2DCollider().also {
        it.node = this
    }
    var radius = 50f
        set(value) {
            field = value
            collider.radius = value
        }


    private val body
        get() = parent as? PhysicsBody2D

    private fun refreshColor() {
        material!!.color.set(view.settings.getShapeColor(
                body?.bodyType,
                hover,
                selected
        ))
    }

    var hover = false
        set(value) {
            field = value
            refreshColor()
        }
    var selected = false
        set(value) {
            field = value
            center!!.visible = value
            refreshColor()
        }

    override var parent: Node?
        get() = super.parent
        set(value) {
            super.parent = value
            refreshColor()
        }

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (center == null) {
            center = CenterNode2D(this, view)
            center!!.parent = view.editorRoot
            center!!.visible = selected
        }
        if (geom == null) {
            geom = Geoms.circle(engine.gl, 0.5f, 12)
            refreshColor()
        }
        val mat = engine.mathPool.mat4f.poll()
        mat.set(model)
        mat.scale(radius * 2f, radius * 2f, 1f)
        material!!.use(mat, projection, renderContext)
        engine.mathPool.mat4f.push(mat)
        geom!!.draw()
        material!!.unuse()
    }

    override fun close() {
        material = null
        geom = null
        center?.free()
        center = null
    }

    override var sensor: Boolean = false
    override var density: Float = 1f
    override var friction: Float = 0.5f
    override var restitution: Float = 0.2f
}