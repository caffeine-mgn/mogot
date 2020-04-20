package pw.binom.sceneEditor

import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import pw.binom.FbxFileEditor
import pw.binom.FbxLanguage
import javax.swing.Icon

object AnimationLanguage : Language("ANIMATION") {
    val ICON = IconLoader.getIcon("/fbx.png")
}

object AnimationFileType : FileType {
    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    override fun isBinary(): Boolean = false

    override fun isReadOnly(): Boolean = false

    override fun getIcon(): Icon? = SceneLanguage.ICON

    override fun getName(): String = "ANIMATION"

    override fun getDefaultExtension(): String = "anim"

    override fun getDescription(): String = "Animation File"
}