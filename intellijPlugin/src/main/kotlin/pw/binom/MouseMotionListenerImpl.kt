package pw.binom

import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener

interface MouseMotionListenerImpl : MouseMotionListener {
    override fun mouseMoved(e: MouseEvent) {
    }

    override fun mouseDragged(e: MouseEvent) {
    }
}