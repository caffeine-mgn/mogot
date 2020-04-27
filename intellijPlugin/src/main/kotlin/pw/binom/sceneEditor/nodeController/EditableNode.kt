package pw.binom.sceneEditor.nodeController

import pw.binom.sceneEditor.NodeService

interface EditableNode {
    fun getEditableFields(): List<NodeService.Field>
    fun afterInit() {}
}