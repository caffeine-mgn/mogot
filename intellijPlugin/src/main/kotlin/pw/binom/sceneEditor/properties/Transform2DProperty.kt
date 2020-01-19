package pw.binom.sceneEditor.properties

import com.intellij.ui.components.JBPanel
import mogot.Node
import mogot.*
import mogot.Spatial
import mogot.math.*
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.Vector2Value
import pw.binom.ui.Vector3Value
import pw.binom.utils.common
import pw.binom.utils.isEmpty
import javax.swing.JComponent
import pw.binom.ui.FloatValue

object Transform2DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = Transform2DProperty(view)
}

class Transform2DProperty(val view: SceneEditorView) : Property, Spoler("Transform") {

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val positionEditor = Vector2Value().appendTo(flex)
    private val rotationEditor = FloatValue("Rotation").appendTo(flex)
    private val scaleEditor = Vector2Value().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<Spatial2D>? = null

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            positionEditor.isEnabled = false
            positionEditor.value.set(Float.NaN, Float.NaN)
            rotationEditor.value = Float.NaN
            scaleEditor.value.set(Float.NaN, Float.NaN)
            return
        } else {
            positionEditor.isEnabled = true
            positionEditor.value.set(nodes.map { it.position }.common)
            rotationEditor.value = toDegrees(nodes.map { it.rotation }.common)
            scaleEditor.value.set(nodes.map { it.scale }.common)
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().onlySpatial2D().toList()
        update()
    }

    init {
        positionEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.mapNotNull { it as? Spatial2D }
                        ?.forEach {
                            it.position.set(
                                    positionEditor.value.x.takeUnless { it.isNaN() } ?: it.position.x,
                                    positionEditor.value.y.takeUnless { it.isNaN() } ?: it.position.y
                            )
                        }
                view.repaint()
            }
        }

        rotationEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial2D }?.forEach {
                    it.rotation = toRadians(rotationEditor.value)
                }
                view.repaint()
            }
        }

        scaleEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial2D }?.forEach {
                    it.scale.set(
                            scaleEditor.value.x.takeUnless { it.isNaN() } ?: it.scale.x,
                            scaleEditor.value.y.takeUnless { it.isNaN() } ?: it.scale.y
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