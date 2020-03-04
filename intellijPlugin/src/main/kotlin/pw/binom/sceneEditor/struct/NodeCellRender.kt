package pw.binom.sceneEditor.struct

import mogot.Node
import mogot.PointLight
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer

class NodeCellRender : TreeCellRenderer {
    private val label = JLabel()
    private val lightIcon = ImageIcon(this::class.java.classLoader.getResource("/light-icon-16.png"))
    override fun getTreeCellRendererComponent(tree: JTree?, value: Any, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
        value as Node
        label.text = value.id ?: value::class.java.simpleName
        label.icon = when (value) {
            is PointLight -> lightIcon
            else -> null
        }
        return label
    }
}