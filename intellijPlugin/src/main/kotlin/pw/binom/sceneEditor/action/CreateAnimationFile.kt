package pw.binom.sceneEditor.action

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import pw.binom.sceneEditor.AnimationFileType
import pw.binom.sceneEditor.SceneFileType

private const val ANIMATION_SCENE_3D = "AnimationTemplate"

class CreateAnimationFile : CreateFileFromTemplateAction("Animation", "Create new Animation file", SceneFileType.icon) {

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
            "Animation"

    override fun buildDialog(project: Project?, directory: PsiDirectory?, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("Create new Animation file")
        builder.addKind("Animation", AnimationFileType.icon, ANIMATION_SCENE_3D)
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        if (templateName != ANIMATION_SCENE_3D)
            return null
        val text = "{\"frameInSecond\": 60}"
        val file = PsiFileFactory.getInstance(dir.project).createFileFromText("$name.${AnimationFileType.defaultExtension}", AnimationFileType, text)
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