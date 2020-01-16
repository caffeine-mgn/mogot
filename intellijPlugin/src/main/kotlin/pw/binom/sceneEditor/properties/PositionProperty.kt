package pw.binom.sceneEditor.properties

import com.intellij.ui.components.JBPanel
import mogot.Node
import mogot.Spatial
import mogot.math.*
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.Vector3Value
import pw.binom.utils.common
import pw.binom.utils.isEmpty
import javax.swing.JComponent

typealias Panel = JBPanel<JBPanel<*>>

object PositionPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = PositionProperty(view)
}

class PositionProperty(val view: SceneEditorView) : Property, Spoler("Transform") {

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val positionEditor = Vector3Value().appendTo(flex)
    private val rotationEditor = Vector3Value().appendTo(flex)
    private val scaleEditor = Vector3Value().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<Node>? = null

    fun update() {
        val nodes = nodes ?: return
        val spatials = nodes.asSequence().mapNotNull { it as? Spatial }

        changeEventEnabled = false
        if (spatials.isEmpty) {
            positionEditor.isEnabled = false
            positionEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            rotationEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            scaleEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            return
        } else {
            positionEditor.isEnabled = true
            positionEditor.value.set(spatials.map { it.position }.common)
            rotationEditor.value.set(spatials.map { RotationVector(it.quaternion).asDegrees }.common)
            scaleEditor.value.set(spatials.map { it.scale }.common)
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes
        update()
    }

    init {
        positionEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.mapNotNull { it as? Spatial }
                        ?.forEach { it.position.set(positionEditor.value) }
                view.repaint()
            }
        }

        rotationEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial }?.forEach { RotationVector(it.quaternion).set(rotationEditor.value.asRadian) }
                view.repaint()
            }
        }

        scaleEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial }?.forEach { it.scale.set(scaleEditor.value) }
                view.repaint()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }
}

val Vector3fc.asRadian
    get() = Vector3f(
            toRadians(x),
            toRadians(y),
            toRadians(z)
    )

val Vector3fc.asDegrees
    get() = Vector3f(
            toDegrees(x),
            toDegrees(y),
            toDegrees(z)
    )