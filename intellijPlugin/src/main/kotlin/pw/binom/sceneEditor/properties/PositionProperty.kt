package pw.binom.sceneEditor.properties

import com.intellij.ui.components.JBPanel
import mogot.Node
import mogot.Spatial
import mogot.math.Vector3f
import mogot.math.Vector3fc
import mogot.math.set
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.Vector3Editor
import pw.binom.utils.common
import pw.binom.utils.isEmpty
import javax.swing.JComponent

typealias Panel = JBPanel<JBPanel<*>>

object PositionPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = PositionProperty(view)
}

class PositionProperty(val view: SceneEditorView) : Property, Spoler("Position") {

    private val flex = FlexLayout(stage)
    val editor = Vector3Editor().appendTo(flex)
    private var changeEventEnabled = true

    private var nodes: List<Node>? = null

    fun update() {
        val nodes = nodes ?: return
        val spatials = nodes.asSequence().mapNotNull { it as? Spatial }

        changeEventEnabled = false
        if (spatials.isEmpty) {
            editor.isEnabled = false
            editor.value.set(Float.NaN, Float.NaN, Float.NaN)
            return
        } else {
            editor.isEnabled = true
            editor.value.set(spatials.map { it.position }.common)
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes
        update()
    }

    init {
        editor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()?.mapNotNull { it as? Spatial }?.forEach { it.position.set(editor.value) }
                view.repaint()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}