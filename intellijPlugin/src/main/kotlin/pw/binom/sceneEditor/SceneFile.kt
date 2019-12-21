package pw.binom.sceneEditor

import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import pw.binom.FbxFileEditor
import pw.binom.FbxLanguage
import javax.swing.Icon

object SceneLanguage : Language("SCENE") {
    val ICON = IconLoader.getIcon("/fbx.png")
}

object SceneFileType : FileType {
    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    override fun isBinary(): Boolean = true

    override fun isReadOnly(): Boolean = false

    override fun getIcon(): Icon? = SceneLanguage.ICON

    override fun getName(): String = "SCENE"

    override fun getDefaultExtension(): String = "scene"

    override fun getDescription(): String = "Scene File"
}

class SceneFileEditorProvider : FileEditorProvider {
    override fun getEditorTypeId(): String = "SceneEditor"

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.extension?.toLowerCase() == "scene"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = SceneEditor(project, file)

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}