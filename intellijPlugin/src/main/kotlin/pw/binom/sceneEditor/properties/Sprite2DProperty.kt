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

object Sprite2DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = Sprite2DProperty(view)
}

class Sprite2DProperty(val view: SceneEditorView) : Property, Spoler("Sprite2D") {

    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val sizeEditor = Vector2Value().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<Sprite>? = null

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
        this.nodes = nodes.asSequence().onlySpatial2D().mapNotNull { it as? Sprite }.toList()
        update()
    }

    init {
        sizeEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.mapNotNull { it as? Sprite }
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