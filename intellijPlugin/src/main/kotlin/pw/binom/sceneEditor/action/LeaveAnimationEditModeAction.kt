package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import pw.binom.sceneEditor.SceneEditor

class LeaveAnimationEditModeAction : AnAction() {

    override fun isTransparentUpdate(): Boolean = true

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isVisible = true
        e.presentation.isEnabled = editor.viewer.view.animateNode != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val model = editor.animationTool?.animateModel!!
        editor.viewer.view.animateNode = null

        ApplicationManager.getApplication().runWriteAction {
            model.save()
        }
    }
}