package pw.binom

import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

interface ComponentListenerImpl: ComponentListener{
    override fun componentMoved(e: ComponentEvent) {
    }

    override fun componentResized(e: ComponentEvent) {
    }

    override fun componentHidden(e: ComponentEvent) {
    }

    override fun componentShown(e: ComponentEvent) {
    }
}