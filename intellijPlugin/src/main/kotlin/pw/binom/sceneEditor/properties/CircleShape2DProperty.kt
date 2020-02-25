package pw.binom.sceneEditor.properties

import mogot.Node
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.nodeController.CircleShape2DView
import pw.binom.ui.FloatEditText
import pw.binom.ui.PropertyName
import pw.binom.ui.appendTo
import pw.binom.ui.gridBagLayout
import pw.binom.utils.common
import pw.binom.utils.isEmpty
import javax.swing.JComponent

object CircleShape2DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = CircleShape2DProperty(view)
}

class CircleShape2DProperty(val view: SceneEditorView) : Property, Spoler("Circle Shape") {

    private val layout = stage.gridBagLayout()
    //    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
//    private val sizeEditor = Vector2Value().appendTo(flex)
    private val radiusTitle = PropertyName("Radius").appendTo(layout, 0, 0)
    private val radiusEditor = FloatEditText().appendTo(layout, 1, 0)

    private var changeEventEnabled = true

    private var nodes: List<CircleShape2DView>? = null

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            radiusEditor.isEnabled = false
            return
        } else {
            radiusEditor.isEnabled = true
            radiusEditor.value = nodes.map { it.radius }.common
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().mapNotNull { it as? CircleShape2DView }.toList()
        update()
    }

    init {
        radiusEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.forEach {
                            it.radius = radiusEditor.value
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