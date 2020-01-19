package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.math.AABBm
import mogot.collider.Collider
import mogot.collider.Collider2D
import pw.binom.sceneEditor.properties.PropertyFactory

interface NodeService {
    fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node?
    fun save(view: SceneEditorView, node: Node): Map<String, String>?
    fun selected(view: SceneEditorView, node: Node)
    fun unselected(view: SceneEditorView, node: Node)
    fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = emptyList()
    fun isEditor(node: Node): Boolean
    fun delete(view: SceneEditorView, node: Node)
    fun getAABB(node: Node, aabb: AABBm): Boolean
    fun getCollider(node: Node): Collider? = null
    fun getCollider2D(node: Node): Collider2D? = null
    fun hover(node: Node, hover: Boolean) {
        //NOP
    }
}