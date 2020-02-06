package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import mogot.physics.d2.shapes.PolygonShape2D
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.PolygonShape2DService

class RemovePolygonAction : AnAction() {

    override fun update(e: AnActionEvent) {
        super.update(e)
        val editor = SceneEditor.currentSceneEditor
        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        } else {
            e.presentation.isVisible = true
        }

        e.presentation.isEnabled = editor.viewer.view.selected
                .asSequence()
                .mapNotNull { it as? PolygonShape2D }
                .mapNotNull {
                    val service = editor.viewer.view.getService(it) as PolygonShape2DService
                    service.getEditor(editor.viewer.view, it)
                }.singleOrNull { it.selectedPoint != null } != null
    }

    override fun isTransparentUpdate(): Boolean = true

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val polygonEditor = editor.viewer.view.selected
                .asSequence()
                .mapNotNull { it as? PolygonShape2D }
                .mapNotNull {
                    val service = editor.viewer.view.getService(it) as PolygonShape2DService
                    service.getEditor(editor.viewer.view, it)
                }
                .filter { it.selectedPoint != null }
                .singleOrNull() ?: return
        polygonEditor.removePoint(polygonEditor.selectedPoint!!)
    }

}