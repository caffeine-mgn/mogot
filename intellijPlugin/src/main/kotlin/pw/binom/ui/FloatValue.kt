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

class FloatValue(label: String) : JPanel() {
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

    var value: Float
        get() {
            if (edit.text == "-")
                return Float.NaN

            return edit.text.toFloatOrNull() ?: 0f
        }
        set(value) {
            edit.text = if (value.isNaN())
                "-"
            else
                value.toString2()
        }

    private var oldValue: Float = 0f

    init {
        value = Float.NaN
        edit.isOpaque = false
        edit.border = null
        background = Color(173, 173, 173)
        this._label.background = Color(145, 145, 145)
        this._label.isOpaque = true

        edit.addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                this@FloatValue._label.isVisible = true
                if (oldValue != value) {
                    eventChange.dispatch()
                }
            }

            override fun focusGained(e: FocusEvent?) {
                this@FloatValue._label.isVisible = false
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
                    this@FloatValue.edit.parent.requestFocus()
                    return
                }
                if (e.keyCode == 10) {
                    val f = edit.text.toFloatOrNull()
                    value = f ?: oldValue
                    this@FloatValue.edit.parent.requestFocus()
                    return
                }
            }
        })
    }
}

fun Float.toString2(): String {
    val v = roundToInt()
    return if (v.toFloat() == this)
        v.toString()
    else
        toString()
}