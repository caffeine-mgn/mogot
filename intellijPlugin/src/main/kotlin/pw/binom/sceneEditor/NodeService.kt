package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.collider.Collider
import mogot.collider.Collider2D
import mogot.math.AABBm
import pw.binom.Services
import pw.binom.sceneEditor.nodeController.EmptyNodeService
import pw.binom.sceneEditor.properties.PropertyFactory

interface NodeService {
    fun getClassName(node: Node): String = node::class.java.name
    fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node?
    fun save(view: SceneEditorView, node: Node): Map<String, String>?
    fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        //NOP
    }

    fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = emptyList()
    fun isEditor(node: Node): Boolean
    fun clone(view: SceneEditorView, node: Node): Node?
    fun delete(view: SceneEditorView, node: Node) {
        EmptyNodeService.nodeDeleted(view.engine, node)
    }

    fun getAABB(node: Node, aabb: AABBm): Boolean = false
    fun getCollider(node: Node): Collider? = null
    fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? = null
    fun isInternalChilds(node: Node): Boolean = false
    fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        //NOP
    }

    fun deepClone(view: SceneEditorView, node: Node): Node? {
        val clone = clone(view, node)
        val services by Services.byClassSequence(NodeService::class.java)
        node.childs.forEach { child ->
            val childService = services.find { it.isEditor(child) } ?: return@forEach
            childService.deepClone(view, child)?.parent = clone
        }
        return clone
    }
}