package pw.binom.sceneEditor.properties

import mogot.Node
import mogot.physics.d2.shapes.Shape2D
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.PropertyName
import pw.binom.ui.appendTo
import pw.binom.ui.gridBagLayout
import pw.binom.utils.equalsAll
import javax.swing.JCheckBox
import javax.swing.JComponent

object PhysicsShapePropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = PhysicsShapeProperty(view)
}


class PhysicsShapeProperty(view: SceneEditorView) : Property, Spoler("Shape") {
    private var nodes: List<Shape2D> = emptyList()

    private val layout = stage.gridBagLayout()

    private val sensorTitle = PropertyName("Sensor").appendTo(layout, 0, 0)
    private val sensorEditor = JCheckBox("On").appendTo(layout, 1, 0)

    private var enableDispatchEvent = true

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.mapNotNull { it as? Shape2D }

        if (nodes.isEmpty()) {
            enableDispatchEvent = false
            sensorTitle.resetVisible = false
            sensorEditor.isEnabled = false
            enableDispatchEvent = true
        } else {
            enableDispatchEvent = false
            sensorEditor.isSelected = if (this.nodes.asSequence().map { it.sensor }.equalsAll())
                this.nodes.first().sensor
            else
                false
            sensorTitle.resetVisible = sensorEditor.isSelected
            enableDispatchEvent = true
        }
    }

    init {
        sensorTitle.resetAction {
            sensorEditor.isSelected = false
            nodes.forEach { it.sensor = false }
            sensorTitle.resetVisible = false
        }
        sensorEditor.addActionListener {
            if (enableDispatchEvent) {
                nodes.forEach { it.sensor = sensorEditor.isSelected }
                sensorTitle.resetVisible = sensorEditor.isSelected
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}