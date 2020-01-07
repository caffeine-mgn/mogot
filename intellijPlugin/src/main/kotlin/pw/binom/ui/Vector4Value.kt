package pw.binom.ui

import mogot.EventDispatcher
import mogot.math.Vector4fm
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Color
import javax.swing.JPanel

class Vector4Value : JPanel() {
    private val flex = FlexLayout(this)
    private val x = FloatValue(" X ").appendTo(flex)
    private val y = FloatValue(" Y ").appendTo(flex)
    private val z = FloatValue(" Z ").appendTo(flex)
    private val w = FloatValue(" Z ").appendTo(flex)

    val value = object : Vector4fm {
        override var x: Float
            get() = this@Vector4Value.x.value
            set(value) {
                this@Vector4Value.x.value = value
            }
        override var y: Float
            get() = this@Vector4Value.y.value
            set(value) {
                this@Vector4Value.y.value = value
            }
        override var z: Float
            get() = this@Vector4Value.z.value
            set(value) {
                this@Vector4Value.z.value = value
            }

        override var w: Float
            get() = this@Vector4Value.z.value
            set(value) {
                this@Vector4Value.z.value = value
            }

        override fun set(x: Float, y: Float, z: Float, w: Float): Vector4fm {
            this@Vector4Value.x.value = x
            this@Vector4Value.y.value = y
            this@Vector4Value.z.value = z
            this@Vector4Value.w.value = z
            return this
        }

    }
    val eventChange = EventDispatcher()

    init {
        x.labelBackground = Color(128, 30, 17)
        y.labelBackground = Color(8, 131, 19)
        z.labelBackground = Color(49, 6, 136)
        w.labelBackground = Color(136, 123, 32)

        x.eventChange.on {
            eventChange.dispatch()
        }
        y.eventChange.on {
            eventChange.dispatch()
        }
        z.eventChange.on {
            eventChange.dispatch()
        }
        w.eventChange.on {
            eventChange.dispatch()
        }
    }
}