package pw.binom.ui

import mogot.EventDispatcher
import pw.binom.MouseListenerImpl
import java.awt.*
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JComponent
import kotlin.math.floor
import kotlin.math.roundToInt

class AnimatePropertyView : JComponent() {

    private val backgroundColor = Color(83, 83, 83)
    private val timeLinesSeparatorColor = Color(62, 62, 62)
    private val frameLineColor = Color(95, 95, 95)
    private val backlightNodeColor = Color(212, 56, 0, 127)
    private val selectedColor = Color(38, 130, 235)

    interface Line {
        val text: String
    }

    interface Node : Line {
        val icon: Icon?
        var visible: Boolean
        var lock: Boolean
        val properties: List<Property>
        fun remove(property: Property)
    }

    interface Property : Line {
        var lock: Boolean
    }

    var backlightNodes = HashSet<Node>()

    interface Model {
        val nodes: List<Node>
        fun remove(node: Node)
        fun property(index: Int): Line? {
            var i = 0
            nodes.forEach { node ->
                if (i == index)
                    return node
                i++
                node.properties.forEach { property ->
                    if (i == index)
                        return property
                    i++
                }
            }
            return null
        }
    }

    var model: Model? = null
        set(value) {
            field = value
            repaint()
        }

    private val _selectedLines = HashSet<Line>()
    val selectedLines: Set<Line>
        get() = _selectedLines

    private val _selected = HashSet<Int>()
    val selected: Set<Int>
        get() = _selected

    fun addSelect(index: Int) {
        val model = model ?: throw IllegalStateException("Model is null")
        val prop = model.property(index) ?: throw IllegalStateException("Can't find property #$index")
        _selectedLines += prop
        _selected += index

    }

    fun clearSelect() {
        _selectedLines.clear()
        _selected.clear()
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
    val selectedPropertyChangeEvent = EventDispatcher()

    init {
        addMouseListener(object : MouseListenerImpl {
            override fun mouseReleased(e: MouseEvent) {
                val model = model ?: return
                val index = floor((e.y - timeHeight + scrollY).toFloat() / frameLineHeight).toInt()
                val prop = model.property(index)


                if (!e.isShiftDown)
                    clearSelect()
                if (prop != null)
                    addSelect(index)
                selectedPropertyChangeEvent.dispatch()
            }
        })
    }

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
            if (node in selectedLines) {
                g.color = selectedColor
                g.fillRect(0, lineY(line), width, frameLineHeight.roundToInt())
            }
            if (node in backlightNodes) {
                g.color = backlightNodeColor
                g.fillRect(0, lineY(line), width, frameLineHeight.roundToInt())
            }
            g.color = Color.WHITE
            g.drawString(node.text, 5, lineY(line) + fontHeight)
            g.color = frameLineColor
            g.drawLine(0, lineY(line + 1), width, lineY(line + 1))
            g.color = Color.BLACK
            g.fillOval(width - ovalSize - 10, (0f + lineY(line) + frameLineHeight * 0.5 - ovalSize * 0.5f).roundToInt(), ovalSize, ovalSize)
            line++
            node.properties.forEach { property ->
                if (property in selectedLines) {
                    g.color = selectedColor
                    g.fillRect(0, lineY(line), width, frameLineHeight.roundToInt())
                }
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