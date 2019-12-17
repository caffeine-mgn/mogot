package pw.binom.sceneEditor.properties

import com.intellij.ui.components.JBPanel
import mogot.Node
import mogot.Spatial
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.material.uniformEditor.asFloat
import pw.binom.sceneEditor.SceneEditorView
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.border.TitledBorder

typealias Panel = JBPanel<JBPanel<*>>

object PositionPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = PositionProperty(view)

}

class PositionProperty(val view: SceneEditorView) : Property, Panel() {

    private val flex = FlexLayout(this)
    private val spinerX = Value("X")
    private val spinerY = Value("Y")
    private val spinerZ = Value("Z")
    private var changeEventEnabled = true

    private var nodes: List<Node>? = null
    override fun setNodes(nodes: List<Node>) {
        changeEventEnabled = false
        println("update!")
        this.nodes = nodes

        if (nodes.isEmpty()) {
            spinerX.spiner.isEnabled = false
            spinerY.spiner.isEnabled = false
            spinerZ.spiner.isEnabled = false
            return
        }

        spinerX.spiner.isEnabled = nodes.asSequence().map { it as Spatial }.equalsBy { it.position.x }
        spinerY.spiner.isEnabled = nodes.asSequence().map { it as Spatial }.equalsBy { it.position.y }
        spinerZ.spiner.isEnabled = nodes.asSequence().map { it as Spatial }.equalsBy { it.position.z }

        if (spinerX.spiner.isEnabled)
            spinerX.value = nodes.first().let { it as Spatial }.position.x

        if (spinerY.spiner.isEnabled)
            spinerY.value = nodes.first().let { it as Spatial }.position.y

        if (spinerZ.spiner.isEnabled)
            spinerZ.value = nodes.first().let { it as Spatial }.position.z
        changeEventEnabled = true
    }

    private fun onChange() {
        if (!changeEventEnabled)
            return
        if (spinerX.spiner.isEnabled)
            nodes?.forEach { it as Spatial; it.position.x = spinerX.value }

        if (spinerY.spiner.isEnabled)
            nodes?.forEach { it as Spatial; it.position.y = spinerY.value }

        if (spinerZ.spiner.isEnabled)
            nodes?.forEach { it as Spatial; it.position.z = spinerZ.value }
        view.repaint()
    }

    private inner class Value(title: String) : Panel(BorderLayout()) {
        val spiner = JSpinner(SpinnerNumberModel(0.0, -100.0, 100.0, 0.1))

        init {
            border = TitledBorder(title)
            add(spiner, BorderLayout.CENTER)
            spiner.addChangeListener { onChange() }
        }

        var value
            get() = spiner.value.asFloat()
            set(value) {
                spiner.value = value
            }
    }

    init {
        spinerX.appendTo(flex) {
            margin.top = 15
            margin.bottom = 2
            margin.left = 2
        }
        spinerY.appendTo(flex) {
            this.margin.top = 15
            this.margin.bottom = 2
        }
        spinerZ.appendTo(flex) {
            this.margin.top = 15
            this.margin.bottom = 2
        }
        border = TitledBorder("Position")
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}

fun <T> Sequence<T>.equalsBy(func: (T) -> Any?): Boolean {
    val first = firstOrNull() ?: return true
    val value = func(first)
    return all { func(it) == value }
}