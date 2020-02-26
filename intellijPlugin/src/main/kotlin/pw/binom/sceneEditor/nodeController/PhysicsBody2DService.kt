package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.math.AABBm
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

object Body2DService : NodeService {
    private val properties = listOf(Transform2DPropertyFactory, PhysicsBody2DPropertyPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != PhysicsBody2D::class.java.name)
            return null
        val node = PhysicsBody2D(view.engine)
        Spatial2DService.load(view.engine, node, properties)
        node.bodyType = properties["type"]?.let { BodyType.valueOf(it) } ?: BodyType.STATIC
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is PhysicsBody2D) return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, out)
        out["type"] = node.bodyType.name
        return out
    }

    override fun isEditor(node: Node): Boolean = node::class.java == PhysicsBody2D::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is PhysicsBody2D) return null
        val out = PhysicsBody2D(node.engine)
        out.bodyType = node.bodyType
        Spatial2DService.cloneSpatial2D(node, out)
        return out
    }
}