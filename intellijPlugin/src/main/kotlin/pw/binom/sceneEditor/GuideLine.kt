package pw.binom.sceneEditor

import java.awt.*
import javax.swing.JPanel
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.roundToInt


class GuideLine(val place: Place) : JPanel() {
    enum class Place {
        TOP, LEFT
    }

    private val font2 = Font("Arial", Font.BOLD, 9)
    var position = 0f
        set(value) {
            field = value
            repaint()
        }

    private inline val pos2
        get() = -position
    var scale = 1f
        set(value) {
            if (value <= 0f)
                throw IllegalArgumentException("Scale can't be equal or less that 0")
            field = value
            println("fullLine=$fullLine")
            repaint()
        }

    private val fullLine
        get() = when (scale) {
            in (2f..3f) -> 50f
            in (1.5f..2f) -> 100f
            in (1f..1.5f) -> 500f
            in (0.5f..1f) -> 250f
            in (0f..0.5f) -> 125f
            else -> 5000f
        }
    private val bigLine get() = fullLine * 0.5f
    private val smallLine get() = bigLine * 0.25f

    private fun toLocal(x: Float) = ((pos2 + x) * scale + length / 2f).roundToInt()
    private fun toGlobal(x: Int) = (x - length / 2) / scale - pos2

    var lineHight = 15
    private val length
        get() = when (place) {
            Place.TOP -> size.width
            Place.LEFT -> size.height
        }

    init {
        preferredSize = when (place) {
            Place.TOP -> Dimension(size.width, lineHight)
            Place.LEFT -> Dimension(lineHight, size.height)
        }
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        g as Graphics2D
        val orig = g.transform
        preferredSize = when (place) {
            Place.TOP -> Dimension(size.width, lineHight)
            Place.LEFT -> Dimension(lineHight, size.height)
        }


        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.font = font2
        g.color = Color.BLACK
        val fontHeight = g.getFontMetrics(font2).height
        val left = toGlobal(0)
        val right = toGlobal(length)

        fun drawLine(pos: Int, height: Float) {
            when (place) {
                Place.TOP -> g.drawLine(pos, (size.height - size.height * height).roundToInt(), pos, size.height)
                Place.LEFT -> g.drawLine((size.width - size.width * height).roundToInt(), pos, size.width, pos)
            }
        }

        var x = floor(left / fullLine) * fullLine
        while (x < right) {
            val xx = toLocal(x)
            drawLine(xx, 1f)
            when (place) {
                Place.TOP -> g.drawString(x.toInt().toString(), xx + 5, fontHeight)
                Place.LEFT -> {
                    g.rotate(-PI / 2f, fontHeight.toDouble(), xx - 5.0)
                    g.drawString(x.toInt().toString(), fontHeight, xx - 5)
                    g.transform = orig
                }
            }

            x += fullLine
        }

        x = floor(left / bigLine) * bigLine
        while (x < right) {
            val xx = toLocal(x)
            drawLine(xx, 0.7f)
            x += bigLine
        }

        x = floor(left / smallLine) * smallLine
        while (x < right) {
            val xx = toLocal(x)
            drawLine(xx, 0.3f)
            x += smallLine
        }
    }
}