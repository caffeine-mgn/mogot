package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import java.awt.Color
import java.util.*
import javax.swing.Icon
import kotlin.collections.ArrayList

object AnimateNodeCreator : NodeCreator {
    override val name: String
        get() = "AnimateNode"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? = EditAnimateNode()

}

object AnimateNodeService : NodeService {
    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        return null
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        return null
    }

    override fun isEditor(node: Node): Boolean = node::class.java == EditAnimateNode::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        return null
    }

}

class AnimateFile : AnimatePropertyView.Model, AnimateFrameView.Model {
    class AnimateProperty(override val text: String, val name: String, val type: NodeService.FieldType) : AnimatePropertyView.Property, AnimateFrameView.FrameLine {
        override var lock: Boolean = false
        private val frames = TreeMap<Int, AnimateFrame>()

        inner class AnimateFrame(time: Int, var data: Any?) : AnimateFrameView.Frame {
            override val color: Color = Color.BLACK
            override var time: Int = time
                set(value) {
                    if (value == field)
                        return
                    field = value
                }
        }

        override fun iterator(): Iterator<AnimateFrame> =
                frames.values.iterator()

        override fun frame(time: Int): AnimateFrameView.Frame? = frames[time]

        override fun remove(frame: AnimateFrameView.Frame) {
            check(frames[frame.time] === frame)
            frames.remove(frame.time)
        }
    }

    class AnimateNode(var nodePath: String) : AnimatePropertyView.Node, AnimateFrameView.FrameLine {
        override val icon: Icon?
            get() = null
        override val text: String
            get() = nodePath
        override var visible: Boolean = true
        override var lock: Boolean = false
        override val properties = ArrayList<AnimateProperty>()
        override fun iterator(): Iterator<AnimateFrameView.Frame> = emptyList<AnimateFrameView.Frame>().iterator()
        override fun frame(time: Int): AnimateFrameView.Frame? = null

        override fun remove(frame: AnimateFrameView.Frame) {
            throw IllegalStateException()
        }
    }

    override val nodes = ArrayList<AnimateNode>()
    override var frameCount: Int = 0
    override var frameInSeconds: Int = 0
    override val lineCount: Int
        get() = nodes.sumBy { it.properties.size + 1 }

    override fun line(index: Int): AnimateFrameView.FrameLine {
        var i = 0
        nodes.forEach {
            if (i == index)
                return it
            i++
            it.properties.forEach {
                if (i == index)
                    return it
                i++
            }
        }
        throw IllegalArgumentException()
    }
}

class EditAnimateNode : Node() {


}