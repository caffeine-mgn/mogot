package pw.binom

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.command.undo.BasicUndoableAction
import com.intellij.openapi.command.undo.DocumentReference
import com.intellij.openapi.keymap.impl.ActionProcessor
import com.intellij.openapi.ui.Messages;
import pw.binom.sceneEditor.SceneEditor


class TestAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        Messages.showMessageDialog(project, "Hello world!", "Greeting", Messages.getInformationIcon())

        val editor = event.getData(CommonDataKeys.EDITOR) as SceneEditor

        CommandProcessor.getInstance().executeCommand(project, object : BasicUndoableAction(editor.ref), Runnable {
            override fun redo() {
                println("--->REDO")
            }

            override fun undo() {
                println("--->UNDO")
            }

            override fun run() {
                println("ACTION!")
            }


        }, "ACTION1", "GROUP1", UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION)
    }

}

