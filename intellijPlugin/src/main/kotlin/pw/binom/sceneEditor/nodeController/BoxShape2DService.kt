package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.Sprite
import mogot.collider.Collider2D
import mogot.collider.Panel2DCollider
import mogot.math.*
import mogot.physics.d2.shapes.BoxShape2D
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.BoxShape2DPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import javax.swing.Icon

private val hoverColor = Vector4f(1f, 0f, 0f, 0.8f)
private val outColor = Vector4f(1f, 0f, 0f, 0.5f)

object BoxShape2DCreator : NodeCreator {
    override val name: String
        get() = "BoxShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = BoxShape2D(view.engine)
        node.material.value = view.default3DMaterial.instance(outColor)
        return node
    }

}

object BoxShape2DService : NodeService {
    private val properties = listOf(Transform2DPropertyFactory, BoxShape2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != BoxShape2D::class.java.name)
            return null
        val node = BoxShape2D(view.engine)
        Spatial2DService.load(view.engine, node, properties)
        node.size.set(
                properties["size.x"]?.toFloatOrNull() ?: 0f,
                properties["size.y"]?.toFloatOrNull() ?: 0f
        )
        node.material.value = view.default3DMaterial.instance(outColor)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is BoxShape2D) return null
        val out = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, out)
        out["size.x"] = node.size.x.toString()
        out["size.y"] = node.size.y.toString()
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
        if (node !is BoxShape2D) return
        val mat = node.material.value as MInstance
        mat.color.set(hoverColor)
    }

    override fun unselected(view: SceneEditorView, node: Node) {
        if (node !is BoxShape2D) return
        val mat = node.material.value as MInstance
        mat.color.set(outColor)
    }

    override fun hover(node: Node, hover: Boolean) {
        if (node !is BoxShape2D) return
        val mat = node.material.value as MInstance
        mat.color.set(if (hover) hoverColor else outColor)
    }

    override fun isEditor(node: Node): Boolean = node::class.java === BoxShape2D::class.java

    override fun clone(node: Node): Node? {
        if (node !is BoxShape2D) return null
        val mat = node.material.value as MInstance
        val out = BoxShape2D(node.engine)
        out.material.value = (mat.root as Default3DMaterial).instance(outColor)
        Spatial2DService.cloneSpatial2D(node, out)
        out.size.set(node.size)
        return out
    }

    override fun getCollider2D(node: Node): Collider2D? {
        node as BoxShape2D
        val c = Panel2DCollider()
        c.node = node
        c.size.set(node.size)
        return c
    }

    override fun delete(view: SceneEditorView, node: Node) {

    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false
}