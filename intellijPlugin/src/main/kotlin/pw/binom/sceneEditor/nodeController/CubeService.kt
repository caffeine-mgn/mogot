package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.CSGBox
import mogot.Engine
import mogot.MaterialNode
import mogot.Node
import mogot.collider.BoxCollider
import mogot.collider.Collider
import mogot.math.AABBm
import mogot.math.Quaternionfm
import mogot.math.Vector3fm
import mogot.math.Vector4f
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.utils.QuaternionfmDelegator
import pw.binom.utils.Vector2fmDelegator
import pw.binom.utils.Vector3fmDelegator
import javax.swing.Icon
import javax.swing.ImageIcon

class EditableCSGBox(view: SceneEditorView) : CSGBox(view.engine), EditableNode {
    override val position: Vector3fm = Vector3fmDelegator(super.position) {
        positionField.eventChange.dispatch()
    }
    override val scale: Vector3fm = Vector3fmDelegator(super.scale) {
        positionField.eventChange.dispatch()
    }
    override val quaternion: Quaternionfm = QuaternionfmDelegator(super.quaternion) {
        rotationField.eventChange.dispatch()
    }


    val positionField = PositionField3D(this)
    val scaleField = ScaleField3D(this)
    val rotationField = RotateField3D(this)
    val materialField = MaterialField(view, this)

    private val fields = listOf(positionField, rotationField, scaleField, materialField)
    override fun getEditableFields(): List<NodeService.Field> = fields
}

object CubeNodeCreator : NodeCreator {
    override val name: String
        get() = "CSVBox"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/cube-icon-16.png"))

    override fun create(view: SceneEditorView): Node {
        val node = EditableCSGBox(view)
        node.material.value = view.default3DMaterial.instance(Vector4f(1f))
        return node
    }
}

object CubeService : NodeService {
    private val props = listOf(Transform3DPropertyFactory, MaterialPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun isEditor(node: Node): Boolean = node::class.java == EditableCSGBox::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is EditableCSGBox) return null
        val out = EditableCSGBox(view)
        out.width = node.width
        out.height = node.height
        out.depth = node.depth
        SpatialService.cloneSpatial(node, out)
        MaterialNodeUtils.clone(node, out)
        return out
    }

    override fun getCollider(node: Node): Collider? {
        node as EditableCSGBox
        val collider = BoxCollider()
        collider.node = node
        collider.size.set(node.width, node.height, node.depth)
        return collider
    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean {
        node as EditableCSGBox
        aabb.position.set(0f)
        aabb.size.set(node.width, node.height, node.depth)
        return true
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.selected = selected
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.hover = hover
    }

    override val nodeClass: String
        get() = CSGBox::class.java.name

    override fun newInstance(view: SceneEditorView): Node = EditableCSGBox(view)
}