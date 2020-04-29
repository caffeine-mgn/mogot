package pw.binom.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditor
import javax.swing.JButton
import javax.swing.JPanel

class MaterialEditor(val sceneEditor: SceneEditor) : JPanel() {
    private val flex = FlexLayout(this)
    private val selectBtn = JButton("No Material").appendTo(flex)

    private inner class SelectMaterial : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            psi?.navigate(false)
        }
    }

    val eventChange = EventDispatcher()

    var value: String
        get() = psi?.virtualFile?.let { sceneEditor.getRelativePath(it) } ?: ""
        set(value) {
            val psiManager = PsiManager.getInstance(sceneEditor.project)
            psi = value.takeIf { it.isNotEmpty() }?.let { sceneEditor.findFileByRelativePath(it) }?.let { psiManager.findFile(it) }
        }

    private var psi: PsiFile? = null
        set(value) {
            field = value
            if (value == null) {
                selectBtn.text = "No Material"
                goto.isEnabled = false
                eventChange.dispatch()
            } else {
                selectBtn.text = value.virtualFile.name
                goto.isEnabled = true
                eventChange.dispatch()
            }
        }

    private var goto = ActionButton(SelectMaterial(), Presentation("").also { it.icon = AllIcons.General.Locate }, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
            .appendTo(flex, grow = 0)

    init {
        isOpaque = false
        selectBtn.addActionListener {
            val chooser = TreeFileChooserFactory
                    .getInstance(sceneEditor.project)
                    .createFileChooser(
                            "Select Material",
                            psi,
                            null
                    ) {
                        it.virtualFile.extension?.toLowerCase() == "mat"
                    }
            chooser.showDialog()
            val file = chooser.selectedFile
            if (file != null)
                psi = file
        }

        psi = null
    }
}
