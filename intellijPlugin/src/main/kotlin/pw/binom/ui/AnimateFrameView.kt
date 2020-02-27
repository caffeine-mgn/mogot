package pw.binom.ui

import mogot.math.Math.PI
import pw.binom.MouseListenerImpl
import pw.binom.MouseMotionListenerImpl
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
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

    var frameLineHight: Float = 20f
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

    private fun getFrame(x: Int, y: Int): Point? {
        val model = model ?: return null
        val frameNum = floor((x + scrollX) / frameWidth).roundToInt()
        val line = floor((y + scrollY) / frameLineHight).roundToInt()
        if (line >= model.lineCount)
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

    init {

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

    override fun paint(g: Graphics) {
        g as Graphics2D
        g.color = backgroundColor
        g.fillRect(0, 0, width, height)
        val offset = scrollX / frameWidth
        val model = model ?: return
        val hight = (model.lineCount * frameLineHight).roundToInt()
        val startFrame = floor(offset).toInt()


        for (i in 0 until frameCount) {
            val x = (i * frameWidth - scrollX).roundToInt()
            if (x < 0)
                continue
            if (x > width)
                break
            if (i % 5 == 0) {
                g.color = backgroundOddColor
                g.fillRect(x, 0, frameWidth.roundToInt(), hight)
            }
            for (row in 0 until model.lineCount) {
                val y = (row * frameLineHight - scrollY).roundToInt()

                if (state is SelectByDrag) {
                    val state = state as SelectByDrag
                    if (i.between(state.startFrame, state.currentFrame) && row.between(state.startLine, state.currentLine)) {
                        g.color = preSelectedColor
                        g.fillRect(x, y, frameWidth.roundToInt(), frameLineHight.roundToInt())
                    }
                }

                val selectRow = selected[row]
                if (selectRow != null)
                    if (i in selectRow) {
                        g.color = selectedColor
                        g.fillRect(x, y, frameWidth.roundToInt(), frameLineHight.roundToInt())
                    }
            }
            g.color = frameLineColor
            g.drawLine(x, 0, x, hight)
        }

        val defaultTransform = g.transform
        for (i in 0 until model.lineCount) {
            val y = (i * frameLineHight + frameLineHight - scrollY).roundToInt()
            g.color = frameLineColor
            g.drawLine(0, y, width, y)
            val line = model.line(i)
            val it = line.iterator(0f)

            it.forEach {
                val pointY = (y - frameLineHight * 0.5f).roundToInt()
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

                val y = (row * frameLineHight - scrollY).roundToInt()
                frames.forEach FRAMES@{ f ->
                    val x = ((f + state.deltaFrame) * frameWidth - scrollX).roundToInt()
                    g.color = preSelectedColor
                    g.fillRect(x, y, frameWidth.roundToInt(), frameLineHight.roundToInt())
                }
            }
            selected.forEach { y, frames ->
                val row = model.line(y)
                frames.forEach FRAMES@{ f ->
                    val y = (y * frameLineHight + frameLineHight - scrollY).roundToInt()
                    val it = row.frame(f) ?: return@FRAMES
                    val pointY = (y - frameLineHight * 0.5f).roundToInt()

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
    }
}

private fun cycle(from: Int, to: Int) = if (from > to) to..from else from..to
private fun Int.between(from: Int, to: Int) = this >= minOf(from, to) && this <= maxOf(from, to)