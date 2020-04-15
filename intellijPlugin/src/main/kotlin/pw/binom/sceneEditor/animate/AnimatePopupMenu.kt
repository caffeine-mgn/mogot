package pw.binom.sceneEditor.animate

import pw.binom.sceneEditor.nodeController.AnimateFile
import pw.binom.ui.AnimatePropertyView
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

class AnimatePopupMenu private constructor(val model: AnimateFile, val properties: Collection<AnimatePropertyView.Line>?) : JPopupMenu() {
    companion object {
        fun create(model: AnimateFile, properties: Collection<AnimatePropertyView.Line>) =
                AnimatePopupMenu(model, properties)
    }

    init {
        if (properties != null) {
            val menu = JMenuItem("Delete")
            add(menu)
            menu.addActionListener {
                val properties = ArrayList(properties)
                properties.forEach {
                    if (it is AnimateFile.AnimateProperty) {
                        it.node.remove(it)
                    }
                }

                properties.forEach {
                    if (it is AnimateFile.AnimateNode) {
                        model.remove(it)
                    }
                }
            }
        }
    }
}