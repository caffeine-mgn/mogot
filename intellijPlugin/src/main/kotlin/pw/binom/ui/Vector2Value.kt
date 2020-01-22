package pw.binom.ui

import mogot.EventDispatcher
import mogot.math.Vector2fm
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Color
import javax.swing.JPanel

class Vector2Value : JPanel() {
    private val flex = FlexLayout(this)
    private val x = FloatValue(" X ").appendTo(flex)
    private val y = FloatValue(" Y ").appendTo(flex)

    val value = object : Vector2fm {
        override var x: Float
            get() = this@Vector2Value.x.value
            set(value) {
                this@Vector2Value.x.value = value
            }
        override var y: Float
            get() = this@Vector2Value.y.value
            set(value) {
                this@Vector2Value.y.value = value
            }

        override fun set(x: Float, y: Float): Vector2fm {
            this@Vector2Value.x.value = x
            this@Vector2Value.y.value = y
            return this
        }

    }
    val eventChange = EventDispatcher()

    init {
        x.labelBackground = Color(128, 30, 17)
        y.labelBackground = Color(8, 131, 19)

        x.eventChange.on {
            eventChange.dispatch()
        }
        y.eventChange.on {
            eventChange.dispatch()
        }
    }
}