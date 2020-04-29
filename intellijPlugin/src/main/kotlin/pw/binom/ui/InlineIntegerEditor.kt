package pw.binom.ui

import com.intellij.ui.JBIntSpinner
import mogot.EventDispatcher
import pw.binom.*
import pw.binom.sceneEditor.properties.Panel
import java.awt.event.*
import java.awt.font.TextAttribute
import javax.swing.JLabel
import javax.swing.JTextField

class InlineIntegerEditor(initValue: Int, var minValue: Int = Int.MIN_VALUE, var maxValue: Int = Int.MAX_VALUE, stepSize: Int = 1, suffix: String) : Panel() {
    init {
        require(initValue in minValue..maxValue)
    }

    private val layout = FlexLayout(this)
    var value: Int = initValue
        set(value) {
            field = value
            edit.text = value.toString()
            refreshValueLabel()
        }

    private fun refreshValueLabel(){
        valueLabel.text = if (invalid) "-" else "$value"
    }

    var invalid: Boolean = false
        set(value) {
            field = value
            refreshValueLabel()
        }

    private var oldValue: Int = initValue

    private val valueLabel = JLabel(value.toString()).appendTo(layout, grow = 0)
    private val edit = JTextField().appendTo(layout, grow = 1).appendTo(layout, grow = 0)
    private val suffixLabel = JLabel().appendTo(layout, grow = 0)
    val changeEvent = EventDispatcher()

    var suffix: String
        get() = suffixLabel.text.substring(2)
        set(value) {
            suffixLabel.text = "  $value"
        }

    init {
        this.suffix = suffix
        this.value = value
        val attr = valueLabel.font.attributes as MutableMap<TextAttribute, Any?>
        attr[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_ON
        valueLabel.font = valueLabel.font.deriveFont(attr)
        edit.isVisible = false
        val activateListener = object : MouseListenerImpl {
            override fun mouseClicked(e: MouseEvent) {
                if (!isEnabled)
                    return
                valueLabel.isVisible = false
                edit.isVisible = true
                edit.requestFocus()
            }
        }
        valueLabel.addMouseListener(activateListener)
        suffixLabel.addMouseListener(activateListener)
        edit.addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                valueLabel.isVisible = true
                suffixLabel.isVisible = true
                edit.isVisible = false
            }

            override fun focusGained(e: FocusEvent?) {
                valueLabel.isVisible = false
                suffixLabel.isVisible = false
                oldValue = value
                edit.text = value.toString()
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
                    this@InlineIntegerEditor.edit.parent.requestFocus()
                    return
                }
                if (e.keyCode == 10) {
                    val f = edit.text.toIntOrNull()?.takeIf { it >= minValue }?.takeIf { it <= maxValue }
                    value = f ?: oldValue
                    if (f != null) {
                        changeEvent.dispatch()
                    }
                    this@InlineIntegerEditor.edit.parent.requestFocus()
                    return
                }
            }
        })
    }
}