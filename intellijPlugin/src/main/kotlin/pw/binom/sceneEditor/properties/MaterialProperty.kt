package pw.binom.sceneEditor.properties

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.vfs.VirtualFile
import mogot.MaterialNode
import mogot.Node
import mogot.math.Vector4f
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.glsl.psi.GLSLFile
import pw.binom.sceneEditor.MInstance
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.loadMaterial
import pw.binom.sceneEditor.properties.meterial.MaterialProperties
import pw.binom.utils.equalsAllBy
import java.awt.Color
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

object MaterialPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property =
            MaterialProperty(view)
}

class MaterialProperty(val view: SceneEditorView) : Property, Spoler("Material") {
    private val flex = FlexLayout(stage, direction = FlexLayout.Direction.COLUMN)

    private val editor = JPanel().appendTo(flex, grow = 0)
    private val editorFlex = FlexLayout(editor, direction = FlexLayout.Direction.ROW)
    private val selectBtn = JButton("select").appendTo(editorFlex)
    private val clearBtn = JButton("X").appendTo(editorFlex, grow = 0)

    val MaterialNode.materialInstance: MaterialInstance?
        get() = material.value as? MaterialInstance

    var MaterialNode.materialFile: VirtualFile?
        get() {
            if (this.material.value == null || this.material.value is MInstance)
                return null
            if (this.material.value is MaterialInstance) {
                return (this.material as MaterialInstance).root.file
            }
            TODO()
        }
        set(value) {
            if (value == null)
                this.material.value = view.default3DMaterial.instance(Vector4f(1f))
            else
                this.material.value = view.engine.resources.loadMaterial(view.project, value)
        }

    private var materialProperties = MaterialProperties(this).appendTo(flex, grow = 0)
    private var instances: List<MaterialInstance>? = null

    private fun refreshInstances() {
        instances = nodes
                .takeIf { it.isNotEmpty() }
                ?.asSequence()
                ?.map { it.materialInstance }
                ?.takeIf { it.all { it != null } }
                ?.map { it!! }
                ?.takeIf { it.equalsAllBy { it.root } }
                ?.toList()

        selectBtn.text = instances?.firstOrNull()?.root?.file?.name ?: "none"
    }

    private fun updateProperties() {

        materialProperties.isVisible = instances != null

        if (instances != null) {
            materialProperties.setMaterials(instances!!)
        } else {
            materialProperties.setMaterials(emptyList())
        }
    }

    private var nodes: List<MaterialNode> = emptyList()

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.mapNotNull { it as? MaterialNode }
        refreshInstances()
        updateProperties()
    }

    private fun setMaterial(file: VirtualFile?) {
        nodes.forEach {
            it.materialFile = file
        }
        refreshInstances()
        updateProperties()
        view.repaint()
    }

    init {
        refreshInstances()
        updateProperties()

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
                setMaterial(file.virtualFile)
            }
        }

        clearBtn.addActionListener {
            setMaterial(null)
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

    init {
        background = Color.green
    }

}