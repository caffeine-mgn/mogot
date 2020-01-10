package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import mogot.Engine
import pw.binom.sceneEditor.properties.behaviourManager

object EmptyNodeService : NodeService {
    fun nodeDeleted(engine: Engine, node: Node){
        engine.behaviourManager.delete(node)
    }
    fun saveNode(engine: Engine, node: Node, output: MutableMap<String, String>) {
        val id = node.id
        if (id != null)
            output["id"] = id
        val b = engine.behaviourManager.get(node)
        if (b?.className != null) {
            output["behaviour.class"] = b.className!!
            b.properties.forEach {
                output["behaviour.property.${it.key}"] = it.value
            }
        }
    }

    fun loadNode(engine: Engine, node: Node, data: Map<String, String>) {
        data["id"]?.let { node.id = it }
        val behaviourClass = data["behaviour.class"]
        if (behaviourClass != null) {
            val v = engine.behaviourManager.getOrCreate(node)
            v.className = behaviourClass
            data.asSequence()
                    .filter { it.key.startsWith("behaviour.property.") }
                    .forEach {
                        v.properties[it.key.removePrefix("behaviour.property.")] = it.value
                    }
        }
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        val node = Node()
        loadNode(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        val out = HashMap<String, String>()
        saveNode(view.engine, node, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Node::class.java

    override fun delete(view: SceneEditorView, node: Node) {
        nodeDeleted(view.engine, node)
    }
}