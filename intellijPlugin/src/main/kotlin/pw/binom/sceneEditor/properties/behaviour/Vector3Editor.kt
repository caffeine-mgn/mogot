package pw.binom.sceneEditor.properties.behaviour

import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.Panel
import pw.binom.ui.FloatValue
import pw.binom.ui.Vector3Value
import mogot.math.*
import javax.swing.JComponent
import javax.swing.JLabel

class Vector3Editor(override val property: BehaviourProperties.Property) : BehaviourProperties.PropertyEditor, Panel() {
    private val flex = FlexLayout(this, FlexLayout.Direction.ROW)
    private val label = JLabel(property.display).appendTo(flex, grow = 0)
    override var value: String?
        get() = c.value?.takeIf { !it.isNaN }?.let { "VEC3F ${it.x};${it.y};${it.z}" }
        set(value) {
            if (value == null)
                c.value.set(Float.NaN, Float.NaN, Float.NaN)
            else {
                val items = value.removePrefix("VEC3F ").split(';')
                c.value.set(
                        items.getOrNull(0)?.toFloatOrNull() ?: Float.NaN,
                        items.getOrNull(1)?.toFloatOrNull() ?: Float.NaN,
                        items.getOrNull(2)?.toFloatOrNull() ?: Float.NaN
                )
            }
        }
    override val component: JComponent
        get() = this
    override val eventChange = EventDispatcher()
    private val c = Vector3Value().appendTo(flex)

    init {
        c.eventChange.on {
            eventChange.dispatch()
        }
    }

    override fun close() {
    }
}