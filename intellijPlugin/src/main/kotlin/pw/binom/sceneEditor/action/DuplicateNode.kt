package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import mogot.Node
import mogot.isChild
import mogot.waitFrame
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.struct.makeTreePath

class DuplicateNode : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        e.presentation.isVisible = true


        e.presentation.isEnabled = editor.viewer.view.selected.isNotEmpty()
    }

    override fun isTransparentUpdate(): Boolean = true

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor ?: return
        val view = editor.viewer.view
        val nodes = view.selected
                .filter { it.parent != null }
                .let { nodes ->
                    nodes.filter { node ->
                        !nodes.any { it.isChild(node) } && node.parent != null
                    }
                }

        if (nodes.isEmpty())
            return
        val createdNodes = ArrayList<Node>()
        view.engine.waitFrame {
            nodes.forEach { node ->
                val service = view.getService(node) ?: return@forEach
                val clone = service.deepClone(editor.viewer.view, node) ?: return@forEach
                clone.parent = node.parent
                createdNodes += clone
                editor.sceneStruct.model.created(editor.sceneStruct.tree, clone)
            }
            editor.sceneStruct.tree.selectionPaths = createdNodes.map {
                it.makeTreePath()
            }.toTypedArray()
        }
    }

}