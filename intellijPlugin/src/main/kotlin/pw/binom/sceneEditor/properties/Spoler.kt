package pw.binom.sceneEditor.properties

import com.intellij.icons.AllIcons
import pw.binom.FlexLayout
import pw.binom.appendTo
import java.awt.Color
import java.awt.Image
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

private val spoler_open = AllIcons.General.ArrowDown//ImageIcon(Spoler::class.java.classLoader.getResource("spoler_open.png")).resize(10, 10)
private val spoler_close = AllIcons.General.ArrowRight//ImageIcon(Spoler::class.java.classLoader.getResource("spoler_close.png")).resize(10, 10)

fun ImageIcon.resize(w: Int, h: Int): ImageIcon {
    val image = this.image
    val newimg = image.getScaledInstance(w, h, Image.SCALE_SMOOTH)
    return ImageIcon(newimg)
}

abstract class Spoler(title: String) : JPanel() {
    private val flex = FlexLayout(this, direction = FlexLayout.Direction.COLUMN)
    private val label = JLabel(title)
    protected val stage = JPanel()

    private fun updateIcon() {
        label.icon = if (stage.isVisible)
            spoler_open
        else
            spoler_close
    }

    init {
        val headerColor = Color(160, 160, 160)
        val bgColor = Color(190, 190, 190)
        stage.background = bgColor
        border = BorderFactory.createLineBorder(headerColor, 2, true)
        label.background = headerColor
        label.isOpaque = true
        label.appendTo(flex, grow = 0)
        stage.appendTo(flex, grow = 0)
        label.addMouseListener(object : MouseListener {
            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseClicked(e: MouseEvent?) {
                stage.isVisible = !stage.isVisible
                updateIcon()
            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent?) {
            }

        })
        updateIcon()
        //UIManager.getColor()
    }
}