package pw.binom.sceneEditor.properties

import mogot.Node
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.FloatEditText
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
    private var nodes: List<ShapeEditorNode> = emptyList()

    private val layout = stage.gridBagLayout()

    private val sensorTitle = PropertyName("Sensor").appendTo(layout, 0, 0)
    private val sensorEditor = JCheckBox("On").appendTo(layout, 1, 0)

    private val densityTitle = PropertyName("Density").appendTo(layout, 0, 1)
    private val densityEditor = FloatEditText().appendTo(layout, 1, 1)

    private val frictionTitle = PropertyName("Friction").appendTo(layout, 0, 2)
    private val frictionEditor = FloatEditText().appendTo(layout, 1, 2)

    private val restitutionTitle = PropertyName("Restitution").appendTo(layout, 0, 3)
    private val restitutionEditor = FloatEditText().appendTo(layout, 1, 3)

    private var enableDispatchEvent = true

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.mapNotNull { it as? ShapeEditorNode }

        if (this.nodes.isEmpty()) {
            enableDispatchEvent = false
            sensorTitle.resetVisible = false
            sensorEditor.isEnabled = false
            enableDispatchEvent = true
            densityEditor.isEnabled = false
            frictionEditor.isEnabled = false
            restitutionTitle.isEnabled = false
        } else {
            enableDispatchEvent = false
            sensorEditor.isEnabled = true
            densityEditor.isEnabled = true
            frictionEditor.isEnabled = true
            restitutionTitle.isEnabled = true

            densityEditor.value = if (this.nodes.asSequence().map { it.density }.equalsAll()) this.nodes.first().density else Float.NaN
            restitutionEditor.value = if (this.nodes.asSequence().map { it.restitution }.equalsAll()) this.nodes.first().restitution else Float.NaN
            frictionEditor.value = if (this.nodes.asSequence().map { it.friction }.equalsAll()) this.nodes.first().friction else Float.NaN

            sensorEditor.isSelected = if (this.nodes.asSequence().map { it.sensor }.equalsAll())
                this.nodes.first().sensor
            else
                false
            sensorTitle.resetVisible = sensorEditor.isSelected
            enableDispatchEvent = true
        }
    }

    private fun updateReset() {
        densityTitle.resetVisible = densityEditor.value != 1f
        sensorTitle.resetVisible = sensorEditor.isSelected
        frictionTitle.resetVisible = frictionEditor.value != 0.5f
        restitutionTitle.resetVisible = restitutionEditor.value != 0.2f
    }

    init {

        restitutionTitle.resetAction {
            restitutionEditor.value = 0.2f
            updateReset()
        }

        densityTitle.resetAction {
            densityEditor.value = 1f
            updateReset()
        }
        frictionTitle.resetAction {
            frictionEditor.value = 0.5f
            updateReset()
        }
        sensorTitle.resetAction {
            sensorEditor.isSelected = false
            nodes.forEach { it.sensor = false }
            updateReset()
        }
        densityEditor.eventChange.on {
            if (enableDispatchEvent) {
                nodes.forEach { it.density = densityEditor.value }
                updateReset()
            }
        }
        frictionEditor.eventChange.on {
            if (enableDispatchEvent) {
                nodes.forEach { it.friction = frictionEditor.value }
                updateReset()
            }
        }
        restitutionEditor.eventChange.on {
            if (enableDispatchEvent) {
                nodes.forEach { it.restitution = restitutionEditor.value }
                updateReset()
            }
        }
        sensorEditor.addActionListener {
            if (enableDispatchEvent) {
                nodes.forEach { it.sensor = sensorEditor.isSelected }
                updateReset()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }
}

interface ShapeEditorNode {
    var sensor: Boolean
    var density: Float
    var friction: Float
    var restitution: Float
}