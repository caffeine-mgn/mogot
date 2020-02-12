package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import pw.binom.sceneEditor.SceneEditor
import mogot.*
import mogot.physics.d2.shapes.PolygonShape2D
import pw.binom.sceneEditor.nodeController.PolygonShape2DService
import pw.binom.sceneEditor.nodeController.PolygonShape2DViwer

class AddPolygonAction : AnAction() {

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
                .mapNotNull { it as? PolygonShape2DViwer }
                .mapNotNull {
                    val service = editor.viewer.view.getService(it) as PolygonShape2DService
                    service.getEditor(editor.viewer.view, it)
                }.singleOrNull { it.newPointPosition != null } != null
    }

    override fun isTransparentUpdate(): Boolean = true

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val polygonEditor = editor.viewer.view.selected
                .asSequence()
                .mapNotNull { it as? PolygonShape2DViwer }
                .mapNotNull {
                    val service = editor.viewer.view.getService(it) as PolygonShape2DService
                    service.getEditor(editor.viewer.view, it)
                }
                .filter { it.newPointPosition != null }
                .singleOrNull() ?: return
        val newPoint = polygonEditor.newPointPosition!!
        polygonEditor.addPoint(newPoint.first + 1, newPoint.second)
    }

}