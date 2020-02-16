package pw.binom.ui

import java.awt.Component
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout


fun <T : Component> T.appendTo(container: Container): T {
    container.add(this)
    return this
}

class GridBagLayoutWrapper(val container: Container) {
    val layout = GridBagLayout()

    init {
        container.layout = layout
    }
}

fun Container.gridBagLayout() = GridBagLayoutWrapper(this)

fun <T : Component> T.appendTo(layout: GridBagLayoutWrapper, x: Int, y: Int, width: Int = 1): T {
    val c = GridBagConstraints()
    c.gridx = x
    c.gridy = y
    c.weightx = 1.0
    c.gridwidth = width
    c.fill = GridBagConstraints.HORIZONTAL
    layout.container.add(this, c)
    return this
}