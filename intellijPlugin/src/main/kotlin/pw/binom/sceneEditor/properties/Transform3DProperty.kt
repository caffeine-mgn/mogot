package pw.binom.sceneEditor.properties

import com.intellij.ui.components.JBPanel
import mogot.Node
import mogot.*
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

object Transform3DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = Transform3DProperty(view)
}

class Transform3DProperty(val view: SceneEditorView) : Property, Spoler("Transform") {

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val positionEditor = Vector3Value().appendTo(flex)
    private val rotationEditor = Vector3Value().appendTo(flex)
    private val scaleEditor = Vector3Value().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<Spatial>? = null

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            positionEditor.isEnabled = false
            positionEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            rotationEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            scaleEditor.value.set(Float.NaN, Float.NaN, Float.NaN)
            return
        } else {
            positionEditor.isEnabled = true
            positionEditor.value.set(nodes.map { it.position }.common)
            rotationEditor.value.set(nodes.map { RotationVector(it.quaternion).asDegrees }.common)
            scaleEditor.value.set(nodes.map { it.scale }.common)
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().onlySpatial().toList()
        update()
    }

    init {
        positionEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.mapNotNull { it as? Spatial }
                        ?.forEach {
                            it.position.set(
                                    positionEditor.value.x.takeUnless { it.isNaN() } ?: it.position.x,
                                    positionEditor.value.y.takeUnless { it.isNaN() } ?: it.position.y,
                                    positionEditor.value.z.takeUnless { it.isNaN() } ?: it.position.z
                            )
                        }
                view.repaint()
            }
        }

        rotationEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial }?.forEach {
                    val r = RotationVector(it.quaternion)
                    r.set(
                            rotationEditor.value.x.takeUnless { it.isNaN() }?.let { toRadians(it) } ?: r.x,
                            rotationEditor.value.y.takeUnless { it.isNaN() }?.let { toRadians(it) } ?: r.y,
                            rotationEditor.value.z.takeUnless { it.isNaN() }?.let { toRadians(it) } ?: r.z
                    )
                }
                view.repaint()
            }
        }

        scaleEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial }?.forEach {
                    it.scale.set(
                            scaleEditor.value.x.takeUnless { it.isNaN() } ?: it.scale.x,
                            scaleEditor.value.y.takeUnless { it.isNaN() } ?: it.scale.y,
                            scaleEditor.value.z.takeUnless { it.isNaN() } ?: it.scale.z
                    )
                }
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