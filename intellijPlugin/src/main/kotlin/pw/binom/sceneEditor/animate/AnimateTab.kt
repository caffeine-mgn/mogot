package pw.binom.sceneEditor.animate

import com.intellij.icons.AllIcons
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.internal.performance.currentLatencyRecordKey
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.impl.ActionToolbarImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.components.JBScrollPane
import mogot.Field
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector3f
import mogot.math.Vector3fc
import pw.binom.MouseListenerImpl
import pw.binom.sceneEditor.AnimationFileType
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.nodeController.AnimateFile
import pw.binom.sceneEditor.nodeController.EditAnimateNode
import pw.binom.sceneEditor.nodeController.getField
import pw.binom.sceneEditor.nodeController.getNode
import pw.binom.sceneEditor.properties.Panel
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import pw.binom.ui.InlineIntegerEditor
import pw.binom.ui.NodeFieldDataFlavor
import pw.binom.utils.relativePath
import java.awt.BorderLayout
import java.awt.dnd.*
import java.awt.event.MouseEvent
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

    override fun remove(node: AnimatePropertyView.Node) {
        throw IllegalArgumentException()
    }

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

class AnimateTab(val editor: SceneEditor, val node: EditAnimateNode) : Panel(), Closeable {
    private val splitter = OnePixelSplitter(false, 0.3f)

    val propertyView = AnimatePropertyView()
    val frameView = AnimateFrameView()
    private val scroll = JBScrollPane(frameView)
    private val dropListener = AddFrameLineDropListener(this)
    private val configPanel = Panel()
    private val animationSelector = ComboBox<String>(100)

    //    private val frameCount = JBIntSpinner(0, 0, Int.MAX_VALUE, 1)
    private val frameInSecond = InlineIntegerEditor(initValue = 1, minValue = 1, suffix = "Frame in Second")
    private val frameCount = InlineIntegerEditor(initValue = 0, minValue = 0, suffix = "Frame Count")
//    private val node: EditAnimateNode?
//        get() = editor.viewer.view.animateNode

    private val onChangeFrame = frameView.currentFrameChangeEvent.on {
        refreshFrameAnimation()
    }

    private val changeSelected = frameView.selectedFrameChangeEvent.on {
        val model = animateModel ?: return@on
        propertyView.clearSelect()
        frameView.selectedLines().forEach {
            propertyView.addSelect(it)
        }
        repaint()
    }

    private val changeSelected2 = propertyView.selectedPropertyChangeEvent.on {
        val model = animateModel ?: return@on
        frameView.clearSelect()
        propertyView.selected.forEach {
            frameView.selectAllFrameLine(it)
        }
        repaint()
    }

    private fun interpolationBetween(currentFrame: Int, frameA: AnimateFile.AnimateProperty.AnimateFrame, frameB: AnimateFile.AnimateProperty.AnimateFrame): Any {
        require(frameA.property.type === frameB.property.type)
        require(frameA.property === frameB.property)
        require(frameA.time < frameB.time) { "frameA.time=${frameA.time} frameB.time=${frameB.time}" }
        require(currentFrame >= frameA.time && currentFrame <= frameB.time)
        val cof =
                when {
                    currentFrame == frameA.time -> 0f
                    currentFrame == frameB.time -> 1f
                    else -> (currentFrame - frameA.time).toFloat() / (frameB.time - frameA.time).toFloat()
                }
        return when (frameA.property.type) {
            Field.Type.VEC2 -> {
                (frameA.data as Vector2fc).lerp(frameB.data as Vector2fc, cof, Vector2f())
            }
            Field.Type.VEC3 -> {
                (frameA.data as Vector3fc).lerp(frameB.data as Vector3fc, cof, Vector3f())
            }
            Field.Type.FLOAT -> {
                val a = frameA.data as Float
                val b = frameB.data as Float
                a + (b - a) * cof
            }
            else -> TODO()
        }
    }

    private fun refreshFrameAnimation() {
        val model = animateModel ?: return
        val node = node
        model.nodes.forEach {
            it.properties.forEach {
                val before = it.getFrameFor(frameView.currentFrame)
                val after = it.getNextFrameFor(frameView.currentFrame + 1)
                val field = it.getField(editor.viewer.view, node) as NodeService.Field<Any>? ?: return@forEach
                when {
                    before == null && after != null -> field.setTempValue(after.data)
                    before != null && after == null -> field.setTempValue(before.data)
                    before != null && after != null -> field.setTempValue(interpolationBetween(frameView.currentFrame, before, after))//field.setTempValue(before.data)
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

    var animateModel: AnimateFile? = null
        private set

    private fun leaveAnimation() {
//        frameCount.isEnabled = false
        frameInSecond.isEnabled = false
        frameCount.isEnabled = false
        frameInSecond.invalid = true
        frameCount.invalid = true

        ApplicationManager.getApplication().runWriteAction {
            animateModel?.save()
        }

        animateModel = null
        frameView.model = null
        propertyView.model = null
        refreshSelectedNodes()
    }


    init {

        propertyView.addMouseListener(object : MouseListenerImpl {
            override fun mouseReleased(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    val menu = AnimatePopupMenu.create(animateModel!!, propertyView.selectedLines)
                    menu.show(e.component, e.x, e.y)
                }
            }
        })
        node.files.map {
            editor.file!!.findFileByRelativePath(it)
        }
        animationSelector.model = AnimationComboBoxModel(editor, node)
        animationSelector.addActionListener {
            val animationFile = (animationSelector.selectedItem as String?)?.takeIf { it.isNotBlank() }
            if (animationFile == null) {
                if (animateModel != null) {
                    leaveAnimation()
                }
                return@addActionListener
            }
            val file = editor.file!!.parent.findFileByRelativePath(animationFile) ?: return@addActionListener
            animateModel = AnimateFile(file)
            frameView.model = animateModel
            propertyView.model = animateModel
            frameInSecond.value = animateModel!!.frameInSeconds
            frameCount.value = animateModel!!.frameCount
            frameCount.isEnabled = true
            frameInSecond.isEnabled = true
            frameCount.invalid = false
            frameInSecond.invalid = false
            refreshSelectedNodes()
        }

        frameInSecond.changeEvent.on {
            val model = animateModel ?: return@on
            model.frameInSeconds = frameInSecond.value
        }

        frameCount.changeEvent.on {
            val model = animateModel ?: return@on
            model.frameCount = frameCount.value
        }
//        frameCount.isEnabled = false
        frameInSecond.isEnabled = false
        frameCount.isEnabled = false
        frameCount.invalid = true
        frameInSecond.invalid = true

        configPanel.add(animationSelector)
        configPanel.add(actions)
        configPanel.add(frameInSecond)
        configPanel.add(frameCount)
        layout = BorderLayout()
        add(configPanel, BorderLayout.NORTH)
        splitter.firstComponent = propertyView
        splitter.secondComponent = scroll
        add(splitter, BorderLayout.CENTER)

        propertyView.model = emptyModel
        frameView.model = emptyModel

        dropTarget = DropTarget(this, DnDConstants.ACTION_MOVE, dropListener, true)
        scroll.verticalScrollBar.addAdjustmentListener {
            propertyView.scrollY = maxOf(0, scroll.verticalScrollBar.value)
        }

        if (node.currentAnimation >= 0 && node.currentAnimation < node.files.size)
            animationSelector.selectedIndex = node.currentAnimation
    }

    private val eventSelectChangedEvent = editor.viewer.view.eventSelectChanged.on {
        refreshSelectedNodes()
    }

    private fun refreshSelectedNodes() {
        val model = animateModel ?: return
        val selected = model.nodes.asSequence()
                .mapNotNull {
                    val node = it.getNode(node) ?: return@mapNotNull null
                    it to node
                }
                .filter { it.second in editor.viewer.view.selected }
                .map { it.first }

        propertyView.backlightNodes.clear()
        frameView.backlightNodes.clear()
        propertyView.backlightNodes.addAll(selected)
        frameView.backlightNodes.addAll(selected)
        repaint()
    }

    override fun close() {
        eventSelectChangedEvent.close()
        if (animateModel != null)
            leaveAnimation()
    }
}