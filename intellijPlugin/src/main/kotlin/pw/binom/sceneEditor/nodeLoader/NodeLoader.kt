package pw.binom.sceneEditor.nodeLoader

import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Node
import pw.binom.sceneEditor.SceneEditorView

interface NodeLoader {
    fun isCanLoad(classname: String): Boolean
    fun isCanSave(node: Node): Boolean
    fun load(view: SceneEditorView, node: ObjectNode): Node
    fun save(view: SceneEditorView, node: Node): ObjectNode
}