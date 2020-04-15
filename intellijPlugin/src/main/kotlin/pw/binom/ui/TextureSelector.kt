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
import pw.binom.sceneEditor.properties.Panel
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel

class TextureSelector(val sceneEditor: SceneEditor) : Panel() {

    private inner class SelectImage : AnAction() {
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

    var psi: PsiFile? = null
        set(value) {
            field = value
            if (value == null) {
                this.icon.icon = null
                this.icon.isVisible = false
                selectBtn.text = "No Image"
                goto.isEnabled = false
                eventChange.dispatch()
            } else {
                selectBtn.text = value.virtualFile.name
                var image = value.virtualFile.inputStream.use {
                    ImageIO.read(it)
                }
                var icon: Icon
                if (image.width > 100 || image.height > 100) {
                    var w: Int
                    var h: Int
                    if (image.width > image.height) {
                        w = 100
                        h = (image.height.toFloat() / image.width.toFloat() * w.toFloat()).toInt()
                    } else {
                        h = 100
                        w = (image.width.toFloat() / image.height.toFloat() * h.toFloat()).toInt()
                    }
                    icon = ImageIcon(image.getScaledInstance(w, h, Image.SCALE_SMOOTH))
                } else {
                    icon = ImageIcon(image)
                }
                this.icon.icon = icon
                this.icon.isVisible = true
                goto.isEnabled = true
                eventChange.dispatch()
            }
        }
    private val flex = FlexLayout(this)
    private var icon = JLabel().appendTo(flex, grow = 0)
    private val selectBtn = JButton("No Image").appendTo(flex)
    private var goto = ActionButton(SelectImage(), Presentation("").also { it.icon = AllIcons.General.Locate }, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
            .appendTo(flex, grow = 0)

    init {
        isOpaque = false
        selectBtn.addActionListener {
            val chooser = TreeFileChooserFactory
                    .getInstance(sceneEditor.project)
                    .createFileChooser(
                            "Select Texture",
                            psi,
                            null
                    ) {
                        it.virtualFile.extension?.toLowerCase() == "png"
                    }
            chooser.showDialog()
            val file = chooser.selectedFile
            if (file != null)
                psi = file
        }

        psi = null
    }
}