package pw.binom.ui

import mogot.EventDispatcher
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

class IntEditText : JTextField() {
    var value: Int
        get() {
            return text.toIntOrNull() ?: 0
        }
        set(value) {
            text = value.toString()
        }

    var valid: Boolean = true
        set(value) {
            field = value
        }

    private var oldValue: Int? = null
    val eventChange = EventDispatcher()

    var validator: ((Int) -> Boolean)? = null

    init {
        addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                var f = text?.toIntOrNull()
                if (f != null && validator != null && !validator!!(f))
                    f = null

                if (f == null) {
                    value = oldValue ?: 0
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
                    value = oldValue ?: 0
                    this@IntEditText.parent.requestFocus()
                    return
                }
            }

            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 10) {
                    var f = text.toIntOrNull()
                    if (f != null && validator != null && !validator!!(f!!))
                        f = null
                    value = f ?: oldValue ?: 0
                    this@IntEditText.parent.requestFocus()
                    return
                }
            }
        })
    }
}