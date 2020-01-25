package pw.binom.sceneEditor.action

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import pw.binom.sceneEditor.SceneFileType

private const val TEMPLATE_SCENE_3D = "Scene3DTemplate"

class CreateSceneFileAction : CreateFileFromTemplateAction("Scene", "Create new Scene file", SceneFileType.icon) {

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
            "Scene"

    override fun buildDialog(project: Project?, directory: PsiDirectory?, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("Create new Scene file")
        builder.addKind("3D", SceneFileType.icon, TEMPLATE_SCENE_3D)
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        if (templateName != TEMPLATE_SCENE_3D)
            return null
        val text = "{\"scene\":[]}"
        val file = PsiFileFactory.getInstance(dir.project).createFileFromText("$name.scene", SceneFileType, text)
        dir.add(file)
        val virtualFile = file.virtualFile
        if (virtualFile != null)
            FileEditorManager.getInstance(dir.project).openFile(virtualFile, true)
        else
            if (file.canNavigate())
                file.navigate(true)
        return file
    }
}