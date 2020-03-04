package pw.binom.sceneEditor.animate

import com.intellij.icons.AllIcons
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import pw.binom.sceneEditor.AnimationFileType
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.AnimateFile
import pw.binom.sceneEditor.nodeController.EditAnimateNode
import pw.binom.sceneEditor.nodeController.getField
import pw.binom.sceneEditor.properties.Panel
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import pw.binom.ui.NodeFieldDataFlavor
import pw.binom.utils.relativePath
import java.awt.BorderLayout
import java.awt.dnd.*
import java.io.Closeable
import javax.swing.ComboBoxModel
import javax.swing.event.ListDataEvent
import javax.swing.event.ListDataListener

private fun <T> emptyModel2() = object : ComboBoxModel<T> {
    override fun setSelectedItem(anItem: Any?) {
    }

    override fun getElementAt(index: Int): T? = null

    override fun getSelectedItem(): Any? = null

    override fun getSize(): Int = 0

    override fun addListDataListener(l: ListDataListener?) {
    }

    override fun removeListDataListener(l: ListDataListener?) {
    }

}

private val emptyModel = object : AnimatePropertyView.Model, AnimateFrameView.Model {
    override val nodes: List<AnimatePropertyView.Node>
        get() = emptyList()
    override val frameCount: Int
        get() = 100
    override val frameInSeconds: Int
        get() = 60
    override val lineCount: Int
        get() = 0

    override fun line(index: Int): AnimateFrameView.FrameLine {
        throw IllegalArgumentException()
    }

}

private class AddFrameLineDropListener(val tab: AnimateTab) : DropTargetListener {
    override fun dropActionChanged(dtde: DropTargetDragEvent) {

    }

    override fun drop(dtde: DropTargetDropEvent) {
        val model = tab.animateModel ?: return
        val fields = dtde.transferable.getTransferData(NodeFieldDataFlavor) as List<NodeService.Field<*>>
        val animNode = tab.editor.viewer.view.animateNode!!
        fields.forEach { field ->
            val relativePath = animNode.relativePath(field.node) ?: return@forEach
            val line = model.nodes.find { it.nodePath == relativePath }
                    ?: AnimateFile.AnimateNode(relativePath).also { model.nodes.add(it) }
            val property = line.properties.find { it.name == field.name }
                    ?: AnimateFile.AnimateProperty(text = field.displayName, name = field.name, type = field.fieldType, node = line)
                            .also { line.properties.add(it) }
            tab.repaint()
        }
        model.save()
        println("Fields: ${fields.map { it.displayName }}")
    }

    override fun dragOver(dtde: DropTargetDragEvent) {
    }

    override fun dragExit(dte: DropTargetEvent) {
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        if (dtde.transferable.isDataFlavorSupported(NodeFieldDataFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY)
        } else {
            dtde.rejectDrag()
        }
    }

}

class AnimationComboBoxModel(val editor: SceneEditor, val node: EditAnimateNode) : ComboBoxModel<String>, Closeable {
    override fun close() {
        listener.close()
    }

    private val listener = node.fileChangedEvent.on {
        refresh()
    }

    private fun refresh() {
        listeners.forEach {
            it.contentsChanged(ListDataEvent(this, 0, 0, size - 1))
        }
    }

    private var selected: Any? = null

    override fun setSelectedItem(anItem: Any?) {
        selected = anItem
    }

    override fun getElementAt(index: Int): String = node.files[index]

    override fun getSelectedItem(): Any? = selected

    override fun getSize(): Int = node.files.size

    private val listeners = ArrayList<ListDataListener>()

    override fun addListDataListener(l: ListDataListener) {
        listeners += l
    }

    override fun removeListDataListener(l: ListDataListener) {
        listeners -= l
    }

}

class AnimateTab(val editor: SceneEditor) : Panel() {
    private val splitter = JBSplitter(false, 0.3f)

    val propertyView = AnimatePropertyView()
    val frameView = AnimateFrameView()
    private val scroll = JBScrollPane(frameView)
    private val dropListener = AddFrameLineDropListener(this)
    private val configPanel = Panel()
    private val animationSelector = ComboBox<String>(100)
    private val node: EditAnimateNode?
        get() = editor.viewer.view.animateNode

    private val onChangeFrame = frameView.currentFrameChangeEvent.on {
        refreshFrameAnimation()
    }

    private fun refreshFrameAnimation() {
        val model = animateModel ?: return
        val node = node ?: return
        model.nodes.forEach {
            it.properties.forEach {
                val before = it.getFrameFor(frameView.currentFrame)
                val after = it.getNextFrameFor(frameView.currentFrame)
                val field = it.getField(editor.viewer.view, node) as NodeService.Field<Any?>? ?: return@forEach
                when {
                    before == null && after != null -> field.setTempValue(after.data)
                    before != null && after == null -> field.setTempValue(before.data)
                    before != null && after != null -> field.setTempValue(before.data)
                }
            }
        }
    }

    private val removeAnimationAction = object : AnAction(null, null, AllIcons.General.Remove) {
        override fun isTransparentUpdate(): Boolean = true
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = animationSelector.selectedItem != null && node != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            node!!.removeFile(animationSelector.selectedItem as String)
        }

    }

    private val addAnimationAction = object : AnAction(null, null, AllIcons.General.Add) {
        override fun isTransparentUpdate(): Boolean = true
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = node != null
        }

        override fun actionPerformed(e: AnActionEvent) {
            val chooser = TreeFileChooserFactory
                    .getInstance(editor.project)
                    .createFileChooser(
                            "Select Animation",
                            null,
                            null
                    ) {
                        it.virtualFile.extension?.toLowerCase() == AnimationFileType.defaultExtension
                    }
            chooser.showDialog()
            val file = chooser.selectedFile
            if (file != null) {
                val relativePath = VfsUtilCore.findRelativePath(editor.file!!, file.virtualFile, '/')
                node!!.add(relativePath ?: file.virtualFile.path)
            }
        }
    }
    val group = object : ActionGroup() {
        override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(addAnimationAction, removeAnimationAction)

    }
    val actions = ActionToolbarImpl("", group, true)


    //private val addAnimationButton = ActionButton(addAnimationAction, Presentation(""), ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)

    fun enterAnimation() {
        val animation = editor.viewer.view.animateNode ?: TODO()
        animation.files.map {
            editor.file!!.findFileByRelativePath(it)
        }
        animationSelector.isEnabled = true
        animationSelector.model = AnimationComboBoxModel(editor, node!!)
    }

    var animateModel: AnimateFile? = null
        private set

    init {
        animationSelector.addActionListener {
            val animationFile = animationSelector.selectedItem as String? ?: return@addActionListener
            val file = editor.file!!.parent.findFileByRelativePath(animationFile) ?: return@addActionListener
            animateModel = AnimateFile(file)
            frameView.model = animateModel
            propertyView.model = animateModel
        }
    }

    fun leaveAnimation() {
        animationSelector.isEnabled = false
        (animationSelector.model as? AnimationComboBoxModel?)?.close()
        animationSelector.model = emptyModel2()

        animateModel = null
        frameView.model = null
        propertyView.model = null
    }

    init {
        animationSelector.isEnabled = false

        configPanel.add(animationSelector)
        configPanel.add(actions)
        layout = BorderLayout()
        add(configPanel, BorderLayout.NORTH)
        splitter.firstComponent = propertyView
        splitter.secondComponent = scroll
        add(splitter, BorderLayout.CENTER)

        propertyView.model = emptyModel
        frameView.model = emptyModel

        dropTarget = DropTarget(this, DnDConstants.ACTION_MOVE, dropListener, true)
    }
}