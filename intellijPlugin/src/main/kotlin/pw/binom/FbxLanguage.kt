package pw.binom

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.lang.Language
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Color
import java.beans.PropertyChangeListener
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.swing.Icon
import javax.swing.JComponent

object FbxLanguage : Language("FBX") {
    val ICON = IconLoader.getIcon("/fbx.png")
}

object FlxFileType : FileType {
    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    override fun isBinary(): Boolean = true

    override fun isReadOnly(): Boolean = true

    override fun getIcon(): Icon? = FbxLanguage.ICON

    override fun getName(): String = "FBX"

    override fun getDefaultExtension(): String = "fbx"

    override fun getDescription(): String = "FBX File"
}

class FbxFileEditorProvider : FileEditorProvider {
    override fun getEditorTypeId(): String = "FbxEditor"

    override fun accept(project: Project, file: VirtualFile): Boolean {
        println("accept  ${file.extension}   $file")
        return file.extension?.toLowerCase() == "fbx"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor = FbxFileEditor(project, file)

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

class FbxFileEditor(private val project: Project,
                    private val sourceFile: VirtualFile) : FileEditor {
    override fun isModified(): Boolean = false

    init {
        println("Init Fbx Editor")
    }

    override fun getFile(): VirtualFile? = sourceFile
    private val propertyChangeListeners = ArrayList<PropertyChangeListener>()

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners += listener
    }

    override fun getName(): String = "FBX Viewer"

    private var state: FileEditorState? = null

    override fun setState(state: FileEditorState) {
        this.state = state
    }

    private val component = run {
        val b = ByteArrayOutputStream()
        sourceFile.inputStream.use { it.copyTo(b) }
        FbxViewer(b.toByteArray())
    }

    override fun getComponent(): JComponent = component

    override fun getPreferredFocusedComponent(): JComponent? = component

    override fun <T : Any?> getUserData(key: Key<T>): T? = userData[key] as T?

    override fun selectNotify() {
    }

    private val userData = HashMap<Key<*>, Any?>()

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
        userData[key] = value
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deselectNotify() {
    }

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return null
    }

    override fun isValid(): Boolean = true

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeListeners -= listener
    }

    override fun dispose() {
        component.destroy()
        println("destroy FBX Editor")
    }

}