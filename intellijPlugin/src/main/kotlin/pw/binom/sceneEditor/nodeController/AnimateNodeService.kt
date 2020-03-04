package pw.binom.sceneEditor.nodeController

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.AnimateNode
import mogot.EventDispatcher
import mogot.Node
import pw.binom.array
import mogot.math.*
import pw.binom.obj
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import pw.binom.utils.findByRelative
import pw.binom.utils.json
import pw.binom.utils.map
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
    override fun getClassName(node: Node): String = AnimateNode::class.java.name

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != AnimateNode::class.java.name)
            return null
        val node = EditAnimateNode()
        properties["files"]?.split('|')?.forEach {
            node.add(it)
        }
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java != EditAnimateNode::class.java)
            return null
        node as EditAnimateNode
        val out = HashMap<String, String>()
        out["files"] = node.files.joinToString("|")
        return out
    }

    override fun isEditor(node: Node): Boolean = node::class.java == EditAnimateNode::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        node as EditAnimateNode
        val out = EditAnimateNode()
        node.files.forEach {
            out.add(it)
        }
        return out
    }

}

class AnimateFile(val file: VirtualFile) : AnimatePropertyView.Model, AnimateFrameView.Model {
    private val doc = FileDocumentManager.getInstance().getDocument(file)!!
    private val mapper = ObjectMapper()
    override var frameCount: Int = 0
    override var frameInSeconds: Int = 0
    override val nodes = ArrayList<AnimateNode>()

    init {
        val tree = mapper.readTree(doc.text)

        frameInSeconds = tree["frameInSecond"].intValue()
        frameCount = tree["frameCount"].intValue()

        tree["objects"]?.array?.forEach { line ->
            val path = line.obj["path"].textValue()
            val animateNode = AnimateNode(path)
            nodes += animateNode
            line.obj["properties"].array.forEach { property ->
                val text = property.obj["text"].textValue()
                val name = property.obj["name"].textValue()
                val type = NodeService.FieldType.valueOf(property.obj["type"].textValue())
                val animateProperty = AnimateProperty(text = text, name = name, type = type, node = animateNode)
                animateNode.properties += animateProperty
                property.obj["frames"].array.forEach { frame ->
                    val time = frame.obj["time"].intValue()
                    val value = frame.obj["val"]?.textValue()?.let { fromString(type, it) }
                    animateProperty.addFrame(time, value)
                }
            }
        }
    }

    private fun fromString(type: NodeService.FieldType, value: String) =
            when (type) {
                NodeService.FieldType.FLOAT -> value.toFloatOrNull() ?: 0f
                NodeService.FieldType.VEC2 -> value.split(';').let { Vector2f(it[0].toFloat(), it[1].toFloat()) }
                NodeService.FieldType.VEC3 -> value.split(';').let { Vector3f(it[0].toFloat(), it[1].toFloat(), it[2].toFloat()) }
            }

    private fun toString(type: NodeService.FieldType, value: Any?): String? =
            when (type) {
                NodeService.FieldType.FLOAT -> (value as Float?)?.toString()
                NodeService.FieldType.VEC2 -> (value as Vector2fc?)?.let { "${it.x};${it.y}" }
                NodeService.FieldType.VEC3 -> (value as Vector3fc?)?.let { "${it.x};${it.y};${it.z}" }
            }

    fun save() {
        val root = json(
                "frameInSecond" to frameInSeconds.json(),
                "frameCount" to frameCount.json(),
                "objects" to nodes.map { node ->
                    json(
                            "path" to node.nodePath.json(),
                            "properties" to node.properties.map { property ->
                                json(
                                        "type" to property.type.name.json(),
                                        "text" to property.name.json(),
                                        "name" to property.name.json(),
                                        "frames" to property.iterator().map { frame ->
                                            json(
                                                    "time" to frame.time.json(),
                                                    "val" to frame.data.let { toString(property.type, it) }?.json()
                                            )
                                        }.json()
                                )
                            }.json()
                    )
                }.json()
        )
        doc.setText(mapper.writeValueAsString(root))
    }

    class AnimateProperty(val node: AnimateNode, override val text: String, val name: String, val type: NodeService.FieldType) : AnimatePropertyView.Property, AnimateFrameView.FrameLine {
        override var lock: Boolean = false
        private val frames = TreeMap<Int, AnimateFrame>()

        inner class AnimateFrame(time: Int, var data: Any?) : AnimateFrameView.Frame {
            val property
                get() = this@AnimateProperty
            override val color: Color = Color.BLACK
            override var time: Int = time
                set(value) {
                    if (value == field)
                        return
                    frames.remove(field)
                    field = value
                    frames[value] = this
                }
        }

        fun addFrame(time: Int, data: Any?) {
            frames[time] = AnimateFrame(time, data)
        }

        fun getFrameFor(time: Int): AnimateFrame? = frames.floorEntry(time)?.value
        fun getNextFrameFor(time: Int): AnimateFrame? = frames.ceilingEntry(time)?.value

        override fun iterator(): Iterator<AnimateFrame> =
                frames.values.iterator()

        val frameCount
            get() = frames.size

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
    private val filePaths = ArrayList<String>()
    val fileChangedEvent = EventDispatcher()
    val files: List<String>
        get() = filePaths

    fun add(file: String) {
        if (file in filePaths)
            return
        filePaths += file
        fileChangedEvent.dispatch()
    }

    fun removeFile(file: String) {
        if (filePaths.remove(file))
            fileChangedEvent.dispatch()
    }
}

fun AnimateFile.AnimateProperty.getField(view: SceneEditorView, node: EditAnimateNode): NodeService.Field<*>? {
    val currentNode = node.findByRelative(this.node.nodePath) ?: return null
    val service = view.getService(currentNode) ?: return null
    return service.getFields(view, currentNode)
            .find { it.name == this.name }
            ?: return null
}