package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.Spatial2D
import mogot.Engine
import mogot.math.*
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory

object Spatial2DService : NodeService {

    private val props = listOf(Transform2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            props

    fun cloneSpatial2D(from: Spatial2D, to: Spatial2D) {
        EmptyNodeService.cloneNode(from, to)
        to.position.set(from.position)
        to.rotation = from.rotation
        to.scale.set(from.scale)
    }

    fun save(engine: Engine, node: Spatial2D, data: MutableMap<String, String>) {
        EmptyNodeService.saveNode(engine, node, data)
        data["position.x"] = node.position.x.toString()
        data["position.y"] = node.position.y.toString()

        data["scale.x"] = node.scale.x.toString()
        data["scale.y"] = node.scale.y.toString()

        data["rotation"] = node.rotation.toString()
    }

    fun load(engine: Engine, node: Spatial2D, data: Map<String, String>) {
        EmptyNodeService.loadNode(engine, node, data)
        node.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f
        )
        node.scale.set(
                data["scale.x"]?.toFloat() ?: 1f,
                data["scale.y"]?.toFloat() ?: 1f
        )
        node.rotation = data["rotation"]?.toFloat() ?: 0f
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Spatial2D::class.java.name)
            return null
        val node = Spatial2D(view.engine)
        load(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java !== Spatial2D::class.java)
            return null
        val out = HashMap<String, String>()
        save(view.engine, node as Spatial2D, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Spatial2D::class.java
    override fun clone(node: Node): Node? {
        if (node !is Spatial2D) return null
        val out = Spatial2D(node.engine)
        cloneSpatial2D(node, out)
        return out
    }

    override fun delete(view: SceneEditorView, node: Node) {
    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false
}