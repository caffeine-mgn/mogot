package pw.binom.sceneEditor.properties.behaviour

import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.Panel
import pw.binom.ui.FloatValue
import javax.swing.JComponent

class FloatEditor(override val property: BehaviourProperties.Property) : BehaviourProperties.PropertyEditor, Panel() {
    private val flex = FlexLayout(this, FlexLayout.Direction.COLUMN)
    override var value: String?
        get() = c.value?.takeIf { !it.isNaN() }?.toString()?.let { "FLOAT $it" }
        set(value) {
            c.value = value?.removePrefix("FLOAT ")?.toFloatOrNull() ?: Float.NaN
        }
    override val component: JComponent
        get() = this
    override val eventChange = EventDispatcher()
    private val c = FloatValue("${property.display} ").appendTo(flex)

    init {
        c.eventChange.on {
            eventChange.dispatch()
        }
    }

    override fun close() {
    }
}