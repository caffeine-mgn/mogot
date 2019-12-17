package pw.binom.sceneEditor.nodeController

import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Node
import pw.binom.io.Closeable
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon

interface NodeServiceFactory {
    fun create(view: SceneEditorView): NodeService
}

interface NodeService : Closeable {
    val createItems: List<CreateItem>
    fun load(clazz: String, json: ObjectNode): Node?
    fun save(node: Node): ObjectNode?
    fun selected(node: Node) {}
    fun unselected(node: Node) {}
    fun getProperties(node: Node): List<PropertyFactory> = emptyList()

    interface CreateItem {
        val name: String
        val icon: Icon?
        fun create(): Node
    }
}


interface NodeController2 {
    fun isCanLoad(classname: String): Boolean
    fun isCanSave(node: Node): Boolean
    fun load(view: SceneEditorView, node: ObjectNode): Node
    fun save(view: SceneEditorView, node: Node): ObjectNode
}