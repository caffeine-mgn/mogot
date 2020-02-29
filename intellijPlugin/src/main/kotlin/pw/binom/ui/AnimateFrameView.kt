package pw.binom.ui

import mogot.math.Math.PI
import pw.binom.MouseListenerImpl
import pw.binom.MouseMotionListenerImpl
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JComponent
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Component for view TimeLine
 */
class AnimateFrameView : JComponent() {

    private val frameLineColor = Color(95, 95, 95)
    private val backgroundColor = Color(83, 83, 83)
    private val backgroundOddColor = Color(77, 77, 77)
    private val selectedColor = Color(38, 130, 235)
    private val preSelectedColor = Color(38, 130, 235, 128)
    private val timeLinesColor = Color(128, 128, 128)
    private val timeLinesSeparatorColor = Color(62, 62, 62)
    private val currentFrameLineColor = Color(178, 0, 0)
    private val currentFrameBackgroundColor = Color(178, 0, 0, 128)

    interface Frame {
        val color: Color
        var time: Int
    }

    interface FrameLine {
        fun iterator(startTime: Float): Iterator<Frame>
        fun frame(time: Int): Frame?
        fun remove(frame: Frame)
    }

    interface Model {
        val lineCount: Int
        fun line(index: Int): FrameLine
    }

    var model: Model? = null


    var frameCount = 0
        set(value) {
            if (value < 0)
                throw IllegalArgumentException()
            field = value
            repaint()
        }
    var scrollX = 0
        set(value) {
            if (value < 0)
                throw IllegalArgumentException("scrollX must be more or equal than 0. Current value is $value")
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

    var frameLineHeight: Float = 20f
        set(value) {
            if (value <= 0f)
                throw IllegalArgumentException()
            field = value
            repaint()
        }
    var frameWidth = 10f
        set(value) {
            if (value <= 0f)
                throw IllegalArgumentException()
            field = value
            repaint()
        }

    private val selected = TreeMap<Int, TreeSet<Int>>()

    private var lastClickFrame: Point? = null
    var currentFrame = 30
    var frameInSeconds = 24

    private fun getFrame(x: Int, y: Int): Point? {
        val model = model ?: return null
        val frameNum = floor((x + scrollX) / frameWidth).roundToInt()
        val line = floor((y - timeHeight + scrollY) / frameLineHeight).roundToInt()
        if (line >= model.lineCount)
            return null
        if (y < timeHeight)
            return null
        return Point(frameNum, line)
    }

    private class SelectByDrag(val startFrame: Int, val startLine: Int) {
        var currentFrame = 0
        var currentLine = 0
    }

    private class MoveByDrag(val startFrame: Int, val startLine: Int) {
        var deltaFrame = 0
    }

    private var state: Any? = null

    fun isSelected(frame: Int, line: Int) = selected[line]?.contains(frame) == true

    private val zeroDimension = Dimension(0, 0)
    private val preferredDimension = Dimension(0, 0)

    override fun getPreferredSize(): Dimension {
        val model = model ?: run {
            zeroDimension.setSize(0, 0)
            return zeroDimension
        }
        preferredDimension.setSize((frameCount * frameWidth + frameWidth).roundToInt(), (timeHeight + model.lineCount * frameLineHeight + frameLineHeight * 0.5f).roundToInt())
        return preferredDimension
    }

    init {
        addFocusListener(object : FocusListener {
            override fun focusLost(e: FocusEvent?) {
                selected.clear()
                repaint()
            }

            override fun focusGained(e: FocusEvent?) {
            }

        })
        addMouseMotionListener(object : MouseMotionListenerImpl {
            override fun mouseDragged(e: MouseEvent) {
                if (state == null && lastClickFrame != null) {
                    val frame = getFrame(e.x, e.y) ?: return
                    if (!isSelected(frame.x, frame.y)) {
                        val selectState = SelectByDrag(frame.x, frame.y)
                        selectState.currentFrame = frame.x
                        selectState.currentLine = frame.y
                        state = selectState
                    } else {
                        state = MoveByDrag(frame.x, frame.y)
                    }
                    repaint()
                    return
                }

                if (state == null && lastClickFrame == null) {
                    currentFrame = maxOf(floor((e.x + scrollX) / frameWidth).roundToInt(), 0)
                    repaint()
                    return
                }

                if (state is SelectByDrag) {
                    val frame = getFrame(e.x, e.y) ?: return
                    (state as SelectByDrag).also {
                        it.currentFrame = frame.x
                        it.currentLine = frame.y
                        repaint()
                        return
                    }
                }

                if (state is MoveByDrag) {
                    val frame = getFrame(e.x, e.y) ?: return
                    (state as MoveByDrag).also {
                        it.deltaFrame = frame.x - it.startFrame
                        repaint()
                        return
                    }
                }
            }
        })

        addMouseListener(object : MouseListenerImpl {

            override fun mousePressed(e: MouseEvent) {
                lastClickFrame = getFrame(e.x, e.y)
            }

            override fun mouseReleased(e: MouseEvent) {
                if (state is SelectByDrag) {
                    val state = state as SelectByDrag
                    if (!e.isControlDown)
                        selected.clear()
                    cycle(state.startLine, state.currentLine).forEach { l ->
                        val line = selected.getOrPut(l) { TreeSet() }
                        cycle(state.startFrame, state.currentFrame).forEach { f ->
                            line.add(f)
                        }
                    }
                    this@AnimateFrameView.state = null
                    repaint()
                }

                if (state is MoveByDrag) {
                    val model = model ?: return
                    val state = state as MoveByDrag
                    selected.forEach { row, frames ->
                        val l = model.line(row)
                        frames.forEach { f ->
                            val ff = l.frame(f)
                            if (ff != null) {
                                val newTime = ff.time + state.deltaFrame
                                l.frame(newTime)?.let { l.remove(it) }
                                ff.time += state.deltaFrame
                            }
                        }
                    }
                    selected.forEach { row, frame ->
                        val oldValues = frame.toList()
                        frame.clear()
                        oldValues.forEach {
                            frame.add(it + state.deltaFrame)
                        }
                    }
                    this@AnimateFrameView.state = null
                    repaint()
                }
            }

            override fun mouseClicked(e: MouseEvent) {
                if (e.y < timeHeight) {
                    currentFrame = maxOf(floor((e.x + scrollX) / frameWidth).roundToInt(), 0)
                    repaint()
                    return
                }
                val frame = getFrame(e.x, e.y)
                val frameNum = frame?.x
                val line = frame?.y

                when {
                    e.isShiftDown -> {
                        line ?: return
                        frameNum ?: return
                        val min = selected.map {
                            it.key to it.value.minBy { abs(it - frameNum) }
                        }.filter { it.second != null }.minBy { it.second!! }
                        if (min == null) {
                            selected.clear()
                            selected.getOrPut(line) { TreeSet() }.add(frameNum)
                        } else {
                            val minLine = min.first
                            val minFrame = min.second!!
                            selected.clear()
                            cycle(minLine, line).forEach { l ->
                                val ll = selected.getOrPut(l) { TreeSet() }
                                cycle(minFrame, frameNum).forEach { f ->
                                    ll.add(f)
                                }
                            }
                        }
                    }
                    e.isControlDown -> {
                        line ?: return
                        frameNum ?: return

                        val ll = selected.getOrPut(line) { TreeSet() }
                        if (frameNum in ll) {
                            ll.remove(frameNum)
                        } else {
                            ll.add(frameNum)
                        }
                    }
                    else -> {
                        selected.clear()
                        if (line != null && frameNum != null)
                            selected.getOrPut(line) { TreeSet() }.add(frameNum)
                    }
                }
                repaint()
            }
        })
    }

    private val timeHeight = 30
    private val font2 = Font("Arial", Font.BOLD, 9)

    override fun paint(g: Graphics) {
        g as Graphics2D
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)

        val offset = scrollX / frameWidth
        val model = model ?: return
        val hight = (model.lineCount * frameLineHeight).roundToInt()
        val startFrame = floor(offset).toInt()

        for (i in 0 until frameCount) {
            val x = (i * frameWidth - scrollX).roundToInt()
            if (x < 0)
                continue
            if (x > width)
                break
            if ((i + 1) % 5 == 0) {
                g.color = backgroundOddColor
                g.fillRect(x, 0 + timeHeight, frameWidth.roundToInt(), hight)
            }
            for (row in 0 until model.lineCount) {
                val y = (row * frameLineHeight - scrollY).roundToInt()

                if (state is SelectByDrag) {
                    val state = state as SelectByDrag
                    if (i.between(state.startFrame, state.currentFrame) && row.between(state.startLine, state.currentLine)) {
                        g.color = preSelectedColor
                        g.fillRect(x, y + timeHeight, frameWidth.roundToInt(), frameLineHeight.roundToInt())
                    }
                }

                val selectRow = selected[row]
                if (selectRow != null)
                    if (i in selectRow) {
                        g.color = selectedColor
                        g.fillRect(x, y + timeHeight, frameWidth.roundToInt(), frameLineHeight.roundToInt())
                    }
            }
            g.color = frameLineColor
            g.drawLine(x, timeHeight, x, hight + timeHeight)
        }

        val xx = (currentFrame * frameWidth - scrollX + frameWidth * 0.5f).roundToInt()
        g.color = currentFrameLineColor
        g.drawLine(xx, timeHeight, xx, timeHeight + hight)

        val defaultTransform = g.transform
        for (i in 0 until model.lineCount) {
            val y = (i * frameLineHeight + frameLineHeight - scrollY + timeHeight).roundToInt()
            g.color = frameLineColor
            g.drawLine(0, y, width, y)
            val line = model.line(i)
            val it = line.iterator(0f)

            it.forEach {
                val pointY = (y - frameLineHeight * 0.5f).roundToInt()
                val x = (it.time * frameWidth - scrollX + frameWidth * 0.5f).roundToInt()
                g.translate(x, pointY)
                g.rotate(PI * 0.25)
                g.color = it.color
                g.fillRect(-3, -3, 6, 6)
                g.transform = defaultTransform
            }
        }

        if (state is MoveByDrag) {
            val state = state as MoveByDrag
            selected.forEach { row, frames ->

                val y = (row * frameLineHeight - scrollY + timeHeight).roundToInt()
                frames.forEach FRAMES@{ f ->
                    val x = ((f + state.deltaFrame) * frameWidth - scrollX).roundToInt()
                    g.color = preSelectedColor
                    g.fillRect(x, y, frameWidth.roundToInt(), frameLineHeight.roundToInt())
                }
            }
            selected.forEach { y, frames ->
                val row = model.line(y)
                frames.forEach FRAMES@{ f ->
                    val y = (y * frameLineHeight + frameLineHeight - scrollY + timeHeight).roundToInt()
                    val it = row.frame(f) ?: return@FRAMES
                    val pointY = (y - frameLineHeight * 0.5f).roundToInt()

                    val x = ((it.time + state.deltaFrame) * frameWidth - scrollX + frameWidth * 0.5f).roundToInt()
                    g.translate(x, pointY)
                    g.rotate(PI * 0.25)
                    g.color = it.color
                    g.fillRect(-3, -3, 6, 6)
                    g.transform = defaultTransform
                    println("draw frame on move! $x   $pointY")
                }
            }
        }

        g.color = backgroundColor
        g.fillRect(0, 0, width, timeHeight)


        val x = (currentFrame * frameWidth - scrollX).roundToInt()
        g.color = currentFrameBackgroundColor
        g.fillRect(x, 0, frameWidth.roundToInt(), timeHeight)
        g.color = currentFrameLineColor
        g.drawRect(x, 0, frameWidth.roundToInt(), timeHeight)


        g.font = font2
        val fontHeight = g.getFontMetrics(font2).height
        for (i in 0 until frameCount) {
            val x = (i * frameWidth - scrollX).roundToInt()
            if (x < 0)
                continue
            if (x > width)
                break
            if ((i + 1) % frameInSeconds == 0) {
                val str = "${(i + 1) / frameInSeconds} s"
                val fontWidth = g.getFontMetrics(font2).stringWidth(str)
                g.color = Color.WHITE
                g.drawString(str, (x + fontWidth * 0.5f).roundToInt(), fontHeight)
            }
            if (i == 0 || (i + 1) % 5 == 0) {
                val str = (i + 1).toString()
                val fontWidth = g.getFontMetrics(font2).stringWidth(str)
                g.color = Color.WHITE
                g.drawString(str, (x + fontWidth * 0.5f).roundToInt(), timeHeight - 6)
            }
            g.color = timeLinesColor
            g.drawLine(x, timeHeight - 3, x, timeHeight)
        }
        g.color = timeLinesSeparatorColor
        g.drawLine(0, timeHeight, width, timeHeight)
    }
}

private fun cycle(from: Int, to: Int) = if (from > to) to..from else from..to
private fun Int.between(from: Int, to: Int) = this >= minOf(from, to) && this <= maxOf(from, to)