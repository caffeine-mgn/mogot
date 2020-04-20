package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.AnimateFile

class DeleteFrameAction : AnAction() {
    override fun isTransparentUpdate(): Boolean = true
    override fun update(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        if (editor.viewer.view.animateNode == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabled = editor.animationTool?.frameView?.isFocusable == true && editor.animationTool?.frameView?.selectedFrames?.asSequence()?.flatMap { it.value.asSequence() }?.any() == true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val model = editor.animationTool!!.animateModel!!
        editor.animationTool?.frameView?.selectedFrames!!.forEach { line, frames ->
            val property = (model.line(line) as? AnimateFile.AnimateProperty) ?: return@forEach
            frames.forEach {
                property.remove(it)
            }
        }
        editor.animationTool?.frameView?.repaint()
        ApplicationManager.getApplication().runWriteAction {
            model.save()
        }
    }
}