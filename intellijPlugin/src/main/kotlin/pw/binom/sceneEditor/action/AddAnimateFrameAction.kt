package pw.binom.sceneEditor.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import mogot.Field
import mogot.math.*
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.AnimateFile
import pw.binom.sceneEditor.nodeController.getField
import mogot.findByRelative

class AddAnimateFrameAction : AnAction() {
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
        e.presentation.isEnabled = editor.animationTool?.animateModel != null && editor.viewer.view.selected.isNotEmpty()
    }

    private fun cloneValue(field: NodeService.Field) =
            when (field.fieldType) {
                Field.Type.FLOAT -> field.currentValue
                Field.Type.VEC2 -> field.currentValue.let { it as Vector2fc }.let { Vector2f(it.x, it.y) }
                Field.Type.VEC3 -> field.currentValue.let { it as Vector3fc }.let { Vector3f(it.x, it.y, it.z) }
                Field.Type.STRING -> field.currentValue.let { it as String }
                Field.Type.FILE -> field.currentValue.let { it as String }
                Field.Type.INT -> field.currentValue.let { it as Int }
                Field.Type.BOOL -> field.currentValue.let { it as Boolean }
                Field.Type.VEC4 -> field.currentValue.let { it as Vector4fc }.let { Vector4f(it.x, it.y, it.z, it.w) }
            }

    override fun actionPerformed(e: AnActionEvent) {
        val editor = SceneEditor.currentSceneEditor!!
        val model = editor.animationTool?.animateModel ?: return
        val lines = editor.animationTool?.frameView?.selectedLines() ?: return
        val animNode = editor.viewer.view.animateNode!!
        val selected = editor.viewer.view.selected
        if (selected.isEmpty())
            return
        val currentFrame = editor.animationTool?.frameView?.currentFrame ?: return
        model.nodes.forEach { line ->
            val node = animNode.findByRelative(line.nodePath) ?: return@forEach
            if (node in selected) {
                val service = editor.viewer.view.getService(node) ?: return@forEach
                line.properties.forEach { property ->
                    val field = service.getFields(editor.viewer.view, node).find { it.name == property.name } ?: return
                    property.addFrame(currentFrame, cloneValue(field))
                }
            }
        }
        editor.animationTool?.repaint()
//        lines.forEach {
//            val property = model.line(it) as? AnimateFile.AnimateProperty ?: return@forEach
//            val field = property.getField(editor.viewer.view, animNode) ?: return@forEach
////            val currentNode = animNode.findByRelative(property.node.nodePath) ?: return@forEach
////            val service = editor.viewer.view.getService(currentNode) ?: return@forEach
////            val field = service.getFields(editor.viewer.view, currentNode)
////                    .find { it.name == property.name }
////                    ?: return@forEach
//            val value = when (field.fieldType) {
//                NodeService.FieldType.VEC2 -> Vector2f(field.currentValue as Vector2fc)
//                NodeService.FieldType.VEC3 -> Vector3f(field.currentValue as Vector3fc)
//                NodeService.FieldType.FLOAT -> field.currentValue as Float
//                else -> TODO()
//            }
//            property.addFrame(editor.animationTool.frameView.currentFrame, value)
//            editor.animationTool.repaint()
//        }

        ApplicationManager.getApplication().runWriteAction {
            model.save()
        }
    }
}