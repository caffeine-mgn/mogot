package pw.binom.sceneEditor

import java.awt.*
import javax.swing.JPanel
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
    var zoom = 1f
        set(value) {
            if (value <= 0f)
                throw IllegalArgumentException("Scale can't be equal or less than 0")
            if (value.isNaN())
                throw IllegalArgumentException("Scale can't be NaN")
            if (value.isInfinite())
                throw IllegalArgumentException("Scale can't be Infinite()")
            field = value
            println("fullLine=$fullLine")
            repaint()
        }
    val scale
        get() = 1 / zoom


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

    companion object {
        fun toLocal(length: Int, zoom: Float, position: Float, x: Float):Int =
                ((x - position) * zoom + length * 0.5f).roundToInt()

        fun toGlobal(length: Int, zoom: Float, position: Float, x: Int) =
                (x - length * 0.5f) / zoom + position
    }

    private fun toLocal(x: Float) = toLocal(length, zoom, position, x)
    private fun toGlobal(x: Int) = toGlobal(length, zoom, position, x)

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
        if (place != Place.TOP)
            return
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
        g.drawRect(toLocal(left), 0, lineHight, lineHight)
        g.drawRect(toLocal(right) - lineHight, 0, lineHight, lineHight)

        println("left=$left length=$length position=$position zoom=$zoom")
        run {
            val H = 50f
            var x = floor(left / H) * H
            //println("$left -> $right every $H")
            while (x < right) {
                val xx = toLocal(x)
                drawLine(xx, 1f)
                x += H
            }
        }
/*
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
        */
    }
}