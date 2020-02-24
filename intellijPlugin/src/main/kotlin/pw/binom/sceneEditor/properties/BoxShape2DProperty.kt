package pw.binom.sceneEditor.properties

import mogot.Node
import mogot.math.set
import mogot.onlySpatial2D
import mogot.physics.d2.shapes.BoxShape2D
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.nodeController.BoxShape2DView
import pw.binom.ui.Vector2Value
import pw.binom.utils.common
import pw.binom.utils.isEmpty
import javax.swing.JComponent

object BoxShape2DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = BoxShape2DProperty(view)
}

class BoxShape2DProperty(val view: SceneEditorView) : Property, Spoler("BoxShape2D") {

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val sizeEditor = Vector2Value().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<BoxShape2DView>? = null

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            sizeEditor.isEnabled = false
            return
        } else {
            sizeEditor.isEnabled = true
            sizeEditor.value.set(nodes.map { it.size }.common)
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().mapNotNull { it as? BoxShape2DView }.toList()
        update()
    }

    init {
        sizeEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.forEach {
                            it.size.set(
                                    sizeEditor.value.x.takeUnless { it.isNaN() } ?: it.size.x,
                                    sizeEditor.value.y.takeUnless { it.isNaN() } ?: it.size.y
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