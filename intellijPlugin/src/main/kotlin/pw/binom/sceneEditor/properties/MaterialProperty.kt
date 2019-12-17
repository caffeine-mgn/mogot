package pw.binom.sceneEditor.properties

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.vfs.VirtualFile
import mogot.MaterialNode
import mogot.Node
import pw.binom.glsl.psi.GLSLFile
import pw.binom.sceneEditor.ExternalMaterial
import pw.binom.sceneEditor.SceneEditorView
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent

object MaterialPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property =
            MaterialProperty(view)

}

class MaterialProperty(val view: SceneEditorView) : Property, Panel() {
    val selectBtn = JButton("select")
    val clearBtn = JButton("X")

    var MaterialNode.exMaterial: VirtualFile?
        get() {
            if (this.material as? ExternalMaterial == null) {
                this.material = ExternalMaterial(view)
            }
            return (this.material as ExternalMaterial).file
        }
        set(value) {
            if (this.material as? ExternalMaterial == null) {
                this.material = ExternalMaterial(view)
            }
            (this.material as ExternalMaterial).file = value
        }


    init {
        add(selectBtn, BorderLayout.CENTER)
        add(clearBtn, BorderLayout.EAST)
    }

    private var nodes: List<MaterialNode>? = null

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.mapNotNull { it as? MaterialNode }
    }

    private fun setMaterial(file: VirtualFile?) {
        nodes?.forEach {
            it.exMaterial = file
        }
    }

    init {
        selectBtn.addActionListener {
            val chooser = TreeFileChooserFactory
                    .getInstance(view.editor1.project)
                    .createFileChooser(
                            "Select Material",
                            null,
                            null
                    ) {
                        it is GLSLFile
                    }
            chooser.showDialog()
            val file = chooser.selectedFile
            if (file != null) {
                println("Selected ${file}   ${file::class.java.name}")
                setMaterial(file.virtualFile)
                selectBtn.text = file.virtualFile.name
            }
        }

        clearBtn.addActionListener {
            setMaterial(null)
            selectBtn.text = "No Material"
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}