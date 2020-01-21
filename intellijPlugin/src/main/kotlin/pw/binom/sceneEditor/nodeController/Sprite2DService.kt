package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.*
import mogot.math.*
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.*
import javax.swing.Icon

object Sprite2DCreator : NodeCreator {
    override val name: String
        get() = "Sprite2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? =
            Sprite(view.engine)

}

object Sprite2DService : NodeService {
    private val props = listOf(Transform2DPropertyFactory, Sprite2DPropertyFactory, MaterialPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            props

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Sprite::class.java.name)
            return null
        val node = Sprite(view.engine)
        Spatial2DService.load(view.engine, node, properties)
        MaterialNodeUtils.load(view, node, properties)
        node.size.set(
                properties["size.x"]?.toFloat() ?: 0f,
                properties["size.y"]?.toFloat() ?: 0f
        )
        if (node.material.value == null)
            node.material.value = view.default3DMaterial.instance(Vector4f(1f))
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java !== Sprite::class.java)
            return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node as Sprite, out)
        MaterialNodeUtils.save(view, node, out)
        out["size.x"] = node.size.x.toString()
        out["size.y"] = node.size.y.toString()
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.selected = true
    }

    override fun unselected(view: SceneEditorView, node: Node) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.selected = false
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Sprite::class.java

    override fun delete(view: SceneEditorView, node: Node) {
    }

    override fun hover(node: Node, hover: Boolean) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.hover = hover
    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false

    override fun getCollider2D(node: Node): Collider2D? {
        node as Sprite
        val c = Panel2DCollider()
        c.node = node
        c.size.set(node.size)
        return c
    }
}