package pw.binom.ui

import mogot.EventDispatcher
import mogot.math.Vector3fm
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Color
import javax.swing.JPanel

class Vector3Value : JPanel() {
    private val flex = FlexLayout(this)
    private val x = FloatValue(" X ").appendTo(flex)
    private val y = FloatValue(" Y ").appendTo(flex)
    private val z = FloatValue(" Z ").appendTo(flex)

    val value = object : Vector3fm {
        override var x: Float
            get() = this@Vector3Value.x.value
            set(value) {
                this@Vector3Value.x.value = value
            }
        override var y: Float
            get() = this@Vector3Value.y.value
            set(value) {
                this@Vector3Value.y.value = value
            }
        override var z: Float
            get() = this@Vector3Value.z.value
            set(value) {
                this@Vector3Value.z.value = value
            }

        override fun set(x: Float, y: Float, z: Float): Vector3fm {
            this@Vector3Value.x.value = x
            this@Vector3Value.y.value = y
            this@Vector3Value.z.value = z
            return this
        }

    }
    val eventChange = EventDispatcher()

    init {
        x.labelBackground = Color(128, 30, 17)
        y.labelBackground = Color(8, 131, 19)
        z.labelBackground = Color(49, 6, 136)

        x.eventChange.on {
            eventChange.dispatch()
        }
        y.eventChange.on {
            eventChange.dispatch()
        }
        z.eventChange.on {
            eventChange.dispatch()
        }
    }
}