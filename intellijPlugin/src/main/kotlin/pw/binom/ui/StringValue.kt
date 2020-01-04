package pw.binom.ui

import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Color
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import kotlin.math.roundToInt

class StringValue(label: String) : JPanel() {
    private val flex = FlexLayout(this)
    private val _label = JLabel(label).appendTo(flex, grow = 0)
    private val edit = JTextField().appendTo(flex, grow = 1)
    val eventChange = EventDispatcher()

    var labelBackground: Color
        get() = _label.foreground
        set(value) {
            _label.foreground = value
        }
    var label: String
        get() = _label.text
        set(value) {
            _label.text = value
        }

    var value: String
        get() {
            return edit.text
        }
        set(value) {
            edit.text = value
        }

    private var oldValue = ""

    init {
        value = ""
        edit.isOpaque = false
        edit.border = null
        background = Color(173, 173, 173)
        this._label.background = Color(145, 145, 145)
        this._label.isOpaque = true

        edit.addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                this@StringValue._label.isVisible = true
                if (oldValue != value) {
                    eventChange.dispatch()
                }
            }

            override fun focusGained(e: FocusEvent?) {
                this@StringValue._label.isVisible = false
                oldValue = value
                edit.selectAll()
            }
        })

        edit.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
            }

            override fun keyPressed(e: KeyEvent?) {
            }

            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == 27) {
                    value = oldValue
                    this@StringValue.edit.parent.requestFocus()
                    return
                }
                if (e.keyCode == 10) {
                    val f = edit.text
                    value = f ?: oldValue
                    this@StringValue.edit.parent.requestFocus()
                    return
                }
            }
        })
    }
}