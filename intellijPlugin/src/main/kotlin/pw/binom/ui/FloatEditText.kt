package pw.binom.ui

import mogot.EventDispatcher
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

class FloatEditText : JTextField() {
    var value: Float
        get() {
            if (text == "-")
                return Float.NaN

            return text.toFloatOrNull() ?: 0f
        }
        set(value) {
            text = if (value.isNaN())
                "-"
            else
                value.toString2()
        }

    private var oldValue: Float = Float.NaN
    val eventChange = EventDispatcher()

    var validator: ((Float) -> Boolean)? = null

    init {
        addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                var f = text?.toFloatOrNull()
                if (f != null && validator != null && !validator!!(f))
                    f = null

                if (f == null) {
                    value = oldValue
                } else
                    if (oldValue != f) {
                        value = f
                        eventChange.dispatch()
                    }
            }

            override fun focusGained(e: FocusEvent?) {
                oldValue = value
                selectAll()
            }
        })

        addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
            }

            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == 27) {
                    value = oldValue
                    this@FloatEditText.parent.requestFocus()
                    return
                }
            }

            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 10) {
                    var f = text.toFloatOrNull()
                    if (f != null && validator != null && !validator!!(f!!))
                        f = null
                    value = f ?: oldValue
                    this@FloatEditText.parent.requestFocus()
                    return
                }
            }
        })
    }
}