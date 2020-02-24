package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Collider2D
import mogot.collider.Panel2DCollider
import mogot.math.Matrix4fc
import mogot.math.Vector2fProperty
import mogot.math.Vector4f
import mogot.math.set
import mogot.physics.d2.PhysicsBody2D
import mogot.physics.d2.shapes.BoxShape2D
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.*
import javax.swing.Icon
import kotlin.collections.set

private val hoverColor = Vector4f(1f, 0f, 0f, 0.8f)
private val outColor = Vector4f(1f, 0f, 0f, 0.5f)

object BoxShape2DCreator : NodeCreator {
    override val name: String
        get() = "BoxShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = BoxShape2DView(view)
        node.size.set(100f, 100f)
        return node
    }

}

object BoxShape2DService : NodeService {
    private val properties = listOf(Transform2DPropertyFactory, BoxShape2DPropertyFactory, PhysicsShapePropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String =
            BoxShape2D::class.java.name

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != BoxShape2D::class.java.name)
            return null
        val node = BoxShape2DView(view)
        Spatial2DService.load(view.engine, node, properties)
        node.size.set(
                properties["size.x"]?.toFloatOrNull() ?: 0f,
                properties["size.y"]?.toFloatOrNull() ?: 0f
        )
        node.sensor = properties["sensor"] == "1"
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is BoxShape2DView) return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, out)
        out["size.x"] = node.size.x.toString()
        out["size.y"] = node.size.y.toString()
        out["sensor"] = if (node.sensor) "1" else "0"
        return out
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        if (node !is BoxShape2DView) return
        node.selected = selected
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        if (node !is BoxShape2DView) return
        node.hover = hover
    }

    override fun isEditor(node: Node): Boolean = node::class.java === BoxShape2DView::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is BoxShape2DView) return null
        val out = BoxShape2DView(view)
        out.sensor = node.sensor
        Spatial2DService.cloneSpatial2D(node, out)
        out.size.set(node.size)
        return out
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        node as BoxShape2DView
        val c = Panel2DCollider()
        c.node = node
        c.size.set(node.size)
        return c
    }
}

class BoxShape2DView(val view: SceneEditorView) : VisualInstance2D(view.engine), ShapeEditorNode {
    private var rect2D by ResourceHolder(Rect2D(engine.gl, null))
    val size = Vector2fProperty()
    private var material by ResourceHolder(view.default3DMaterial.instance(Vector4f()))
    private var center: CenterNode2D? = null


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
        if (rect2D == null) {
            rect2D = Rect2D(engine.gl, size)
            refreshColor()
        }
        if (size.resetChangeFlag()) {
            rect2D!!.size.set(size)
        }
        material!!.use(model, projection, renderContext)
        rect2D!!.draw()
        material!!.unuse()
    }

    override fun close() {
        material = null
        rect2D = null
        center?.free()
        center = null
    }

    override var sensor: Boolean = false
}