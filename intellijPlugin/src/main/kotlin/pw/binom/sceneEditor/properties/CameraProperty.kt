package pw.binom.sceneEditor.properties

import mogot.Node
import mogot.Camera
import mogot.Spatial
import mogot.math.set
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.FloatValue
import pw.binom.ui.Vector3Editor
import pw.binom.utils.common
import pw.binom.utils.equalsAll
import pw.binom.utils.isEmpty
import pw.binom.utils.isNotEmpty
import javax.swing.JComponent

object CameraPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = CameraProperty(view)
}

class CameraProperty(val view: SceneEditorView) : Property, Spoler("Camera") {

    private var nodes: List<Node>? = null
    private var changeEventEnabled = true

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    val near = FloatValue(" Near ").appendTo(flex)
    val far = FloatValue(" Far ").appendTo(flex)
    val fieldOfView = FloatValue(" Field Of View ").appendTo(flex)

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes
        update()
    }

    fun update() {
        val nodes = nodes ?: return
        val spatials = nodes.asSequence().mapNotNull { it as? Camera }
        changeEventEnabled = false

        if (spatials.isNotEmpty) {
            if (spatials.map { it.near }.equalsAll()) {
                near.value = spatials.first().near
            } else {
                near.value = Float.NaN
            }

            if (spatials.map { it.far }.equalsAll()) {
                far.value = spatials.first().far
            } else {
                far.value = Float.NaN
            }

            if (spatials.map { it.fieldOfView }.equalsAll()) {
                fieldOfView.value = spatials.first().fieldOfView
            } else {
                fieldOfView.value = Float.NaN
            }
        } else {
            near.value = Float.NaN
            far.value = Float.NaN
            fieldOfView.value = Float.NaN
        }
        changeEventEnabled = true
    }

    init {
        near.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Camera }?.forEach { it.near = near.value }
                view.repaint()
            }
        }

        far.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Camera }?.forEach { it.far = far.value }
                view.repaint()
            }
        }

        fieldOfView.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Camera }?.forEach { it.fieldOfView = fieldOfView.value }
                view.repaint()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}