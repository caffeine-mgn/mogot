package pw.binom.sceneEditor.properties.meterial

import com.intellij.ide.util.TreeFileChooserFactory
import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.ExternalTextureFS
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.loadTexture
import pw.binom.sceneEditor.properties.Panel
import pw.binom.utils.equalsAll
import javax.swing.JButton
import javax.swing.JComponent

object TexturePropertyEditorFactory : MaterialProperties.PropertyEditorFactory {
    override fun create(panel: MaterialProperties, uniform: List<MaterialInstance.Uniform>): MaterialProperties.PropertyEditor? {
        check(uniform.isNotEmpty()) { "Uniform List is Empty" }
        if (uniform.first().type != MaterialInstance.Type.Texture)
            return null
        return TexturePropertyEditor(panel, uniform)
    }

}

class TexturePropertyEditor(val panel: MaterialProperties, override val uniform: List<MaterialInstance.Uniform>) : Panel(), MaterialProperties.PropertyEditor {
    private var texture: ExternalTextureFS? = null
        set(value) {
            field?.dec()
            field = value
            value?.inc()
            updateButton()
            eventChange.dispatch()
            panel.property.view.repaint()
        }
    override var value: Any?
        get() = texture
        set(value) {
            texture = value as ExternalTextureFS?
        }
    override val component: JComponent
        get() = this
    override val eventChange = EventDispatcher()
    private val flex = FlexLayout(this, FlexLayout.Direction.ROW)
    val selectBtn = JButton("select").appendTo(flex) { grow = 1f }

    private var eqAll = uniform.asSequence().map { it.materialInstance.get(it) }.equalsAll()

    private fun updateButton() {
        selectBtn.text = if (eqAll) {
            texture?.file?.name ?: "none"
        } else {
            "different"
        }
    }

    init {
        val u = uniform.first()
        texture = if (eqAll) {
            u.materialInstance.get(u) as? ExternalTextureFS
        } else {
            null
        }
/*
        if (texture == null) {
            texture = u.value
                    ?.let { u.materialInstance.root.file.parent.findFileByRelativePath(it) }
                    ?.let { u.materialInstance.root.engine.resources.loadTexture(it) }
        }
*/
        selectBtn.addActionListener {
            val chooser = TreeFileChooserFactory
                    .getInstance(panel.property.view.editor1.project)
                    .createFileChooser(
                            "Select Texture",
                            null,
                            null
                    ) {
                        it.virtualFile.extension?.toLowerCase() == "png"
                    }
            chooser.showDialog()
            val file = chooser.selectedFile
            if (file != null) {
                texture = u.materialInstance.engine.resources.loadTexture(file.virtualFile)
            }
        }
    }

    override fun close() {
        texture?.dec()
        texture = null
    }

}