package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView

object EmptyNodeService : NodeService {
    fun saveNode(node: Node, output: MutableMap<String, String>) {
        val id = node.id ?: return
        if (node.id != null)
            output["id"] = id
    }

    fun loadNode(node: Node, data: Map<String, String>) {
        data["id"]?.let { node.id = it }
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        val node = Node()
        loadNode(node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        val out = HashMap<String, String>()
        saveNode(node, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Node::class.java

    override fun delete(view: SceneEditorView, node: Node) {

    }
}