package pw.binom

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages;


class TestAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        Messages.showMessageDialog(project, "Hello world!", "Greeting", Messages.getInformationIcon())
    }

}