package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Field
import mogot.Node
import mogot.physics.box2d.dynamics.BodyType
import mogot.physics.d2.PhysicsBody2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PhysicsBody2DPropertyPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import javax.swing.Icon

object Body2DCreator : NodeCreator {
    override val name: String
        get() = "PhysicsBody2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? =
            PhysicsBody2D(view.engine)
}

class PhysicsBody2DTypeEditableField(override val node: EditablePhysicsBody2D) : NodeService.AbstractField() {
    override var realValue: Any
        get() = node.bodyType.toString()
        set(value) {
            node.bodyType = BodyType.valueOf(value as String)
        }

    override fun cloneRealValue(): Any = node.bodyType.toString()

    override val groupName: String
        get() = "Physics"

    override val name: String
        get() = "type"

    override val displayName: String
        get() = "Type"

    override val fieldType: Field.Type
        get() = Field.Type.STRING
}

class EditablePhysicsBody2D(view: SceneEditorView) : PhysicsBody2D(view.engine), EditableNode {
    val transformField = PositionField2D(this)
    val rotationField = RotationField2D(this)
    val physicsBody2DTypeEditableField = PhysicsBody2DTypeEditableField(this)
    private val fields = listOf(transformField, rotationField, physicsBody2DTypeEditableField)
    override fun getEditableFields(): List<NodeService.Field> = fields
}

object Body2DService : NodeService {
    private val properties = listOf(Transform2DPropertyFactory, PhysicsBody2DPropertyPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun isEditor(node: Node): Boolean = node::class.java == PhysicsBody2D::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PhysicsBody2D) return null
        val out = PhysicsBody2D(node.engine)
        out.bodyType = node.bodyType
        Spatial2DService.cloneSpatial2D(node, out)
        return out
    }

    override val nodeClass: String
        get() = PhysicsBody2D::class.java.name

    override fun newInstance(view: SceneEditorView): Node = PhysicsBody2D(view.engine)
}