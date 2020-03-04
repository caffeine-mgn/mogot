package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.EditAnimateNode

class EnterAnimationEditModeAction : AnAction() {

    override fun isTransparentUpdate(): Boolean = true

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val selected = editor.viewer.view.selected
        e.presentation.isVisible = true
        e.presentation.isEnabled = editor.viewer.view.animateNode == null && selected.size == 1 && selected[0] is EditAnimateNode
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val selected = editor.viewer.view.selected
        editor.viewer.view.animateNode = selected[0] as EditAnimateNode
        editor.animationTool.enterAnimation()
        println("Enter to Animate Mode")
    }
}