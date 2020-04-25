package pw.binom.ui

import com.intellij.openapi.ui.ComboBox
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.EditAnimateNode
import pw.binom.utils.common
import pw.binom.io.Closeable

class EditorAnimationSelector(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {
    private val nodes = fields.asSequence().map { it.node }.mapNotNull { it as? EditAnimateNode }.toList()
    private var common: String? = null
    private lateinit var animations: List<String>
    private val initValues = nodes.map { it.currentAnimation }

    private fun refresh() {
        enableEvents = false
        common = nodes.asSequence().mapNotNull {
            if (it.currentAnimation >= 0 && it.currentAnimation < it.files.size)
                it.files[it.currentAnimation]
            else
                null
        }.common
        animations = nodes.asSequence().flatMap { it.files.asSequence() }.distinct().toList().let {
            if (common == null)
                listOf("Undefined") + it
            else
                it
        }
        combobox.removeAllItems()
        animations.forEach {
            combobox.addItem(it)
        }
        if (common == null)
            combobox.selectedIndex = 0
        else
            combobox.selectedIndex = animations.indexOf(common!!)
        enableEvents = true
    }

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val combobox = ComboBox(emptyArray<String>()).appendTo(layout, 1, 0)
    private var enableEvents = true
    private val closables = ArrayList<Closeable>()

    init {
        combobox.addActionListener {
            if (!enableEvents)
                return@addActionListener
            if (common == null && combobox.selectedIndex == 0) {
                nodes.forEachIndexed { index, editAnimateNode ->
                    editAnimateNode.currentAnimation = initValues[index]
                }
            } else {
                nodes.forEach {
                    it.currentAnimation = it.files.indexOf(combobox.selectedItem as String)
                }
            }
        }
        refresh()

        fields.forEach {
            closables += it.eventChange.on {
                refresh()
            }
        }
    }

    override fun close() {
        closables.forEach {
            it.close()
        }
    }
}