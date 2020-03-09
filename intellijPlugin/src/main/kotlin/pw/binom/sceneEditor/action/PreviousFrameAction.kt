package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import pw.binom.sceneEditor.SceneEditor

class PreviousFrameAction : AnAction() {
    override fun isTransparentUpdate(): Boolean = true
    override fun update(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        e.presentation.isVisible = true
        e.presentation.isEnabled = editor.animationTool?.animateModel != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val frame = editor.animationTool?.frameView?.currentFrame?:return
        editor.animationTool?.frameView?.currentFrame = maxOf(frame - 1, 0)
        editor.animationTool?.frameView?.repaint()
    }
}