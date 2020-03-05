package pw.binom.ui

import java.awt.*
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.math.roundToInt

class AnimatePropertyView : JComponent() {

    private val backgroundColor = Color(83, 83, 83)
    private val timeLinesSeparatorColor = Color(62, 62, 62)
    private val frameLineColor = Color(95, 95, 95)

    interface Node {
        val icon: Icon?
        val text: String
        var visible: Boolean
        var lock: Boolean
        val properties: List<Property>
    }

    interface Property {
        val text: String
        var lock: Boolean
    }

    interface Model {
        val nodes: List<Node>
    }

    var model: Model? = null
        set(value) {
            field = value
            repaint()
        }

    var frameLineHeight: Float = 20f
        set(value) {
            if (value <= 0f)
                throw IllegalArgumentException()
            field = value
            repaint()
        }

    var scrollY = 0
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("scrollX must be more or equal than 0. Current value is $value")
            field = value
            repaint()
        }

    private val zeroDimension = Dimension(0, 0)
    private val preferredDimension = Dimension(0, 0)

    override fun getPreferredSize(): Dimension {
        val model = model ?: run {
            zeroDimension.setSize(100, 0)
            return zeroDimension
        }
        val lineCount = model.nodes.sumBy { it.properties.size + 1 }
        preferredDimension.setSize(100, (timeHeight + lineCount * frameLineHeight + frameLineHeight * 0.5f).roundToInt())
        return preferredDimension
    }

    private val timeHeight = 30
    private val font2 = Font("Arial", Font.BOLD, 9)


    override fun paint(g: Graphics) {
        g as Graphics2D
        val fontHeight = g.getFontMetrics(font2).height
        fun lineY(line: Int) = timeHeight + (line * frameLineHeight).roundToInt() - scrollY
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)
        val model = model ?: return
        var line = 0
        g.font = font2

        val ovalSize = 4

        model.nodes.forEach { node ->
            g.color = Color.WHITE
            g.drawString(node.text, 5, lineY(line) + fontHeight)
            g.color = frameLineColor
            g.drawLine(0, lineY(line + 1), width, lineY(line + 1))
            g.color = Color.BLACK
            g.fillOval(width - ovalSize - 10, (0f + lineY(line) + frameLineHeight * 0.5 - ovalSize * 0.5f).roundToInt(), ovalSize, ovalSize)
            line++
            node.properties.forEach { property ->
                g.color = Color.WHITE
                g.drawString(property.text, 20, lineY(line) + fontHeight)
                g.color = frameLineColor
                g.drawLine(0, lineY(line + 1), width, lineY(line + 1))
                g.color = Color.BLACK
                g.fillOval(width - ovalSize - 10, (0f + lineY(line) + frameLineHeight * 0.5 - ovalSize * 0.5f).roundToInt(), ovalSize, ovalSize)
                line++
            }
        }

        g.color = backgroundColor
        g.fillRect(0, 0, width, timeHeight)
        g.color = timeLinesSeparatorColor
        g.drawLine(0, timeHeight, width, timeHeight)
    }
}