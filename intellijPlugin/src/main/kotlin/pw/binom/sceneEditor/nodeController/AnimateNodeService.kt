package pw.binom.sceneEditor.nodeController

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector3f
import mogot.math.Vector3fc
import pw.binom.animation.Animation
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.ui.AbstractEditor
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import pw.binom.ui.EditorAnimationSelector
import java.awt.Color
import java.io.StringReader
import java.util.*
import javax.swing.Icon
import kotlin.collections.ArrayList
import kotlin.collections.Iterator
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.emptyList
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.listOf
import kotlin.collections.plusAssign
import kotlin.collections.set
import kotlin.collections.sumBy

object AnimateNodeCreator : NodeCreator {
    override val name: String
        get() = "AnimateNode"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? = EditAnimateNode()

}

object AnimateNodeService : NodeService {
    override fun getClassName(node: Node): String = AnimateNode::class.java.name

    override fun getFields(view: SceneEditorView, node: Node): List<NodeService.Field<Any>> {
        node as EditAnimateNode
        return listOf(node.currentAnimationField as NodeService.Field<Any>)
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != AnimateNode::class.java.name)
            return null
        val node = EditAnimateNode()
        properties.asSequence().filter { it.key.startsWith("files.") }.forEach {
            node.add(it.value.removePrefix("FILE "))
        }
        properties["animationIndex"]?.toIntOrNull()?.also {
            node.currentAnimation = it
        }
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java != EditAnimateNode::class.java)
            return null
        node as EditAnimateNode
        val out = HashMap<String, String>()
        node.files.forEachIndexed { index, s ->
            out["files.$index"] = "FILE $s"
        }
        node.currentAnimation.takeIf { it >= 0 }.also {
            out["animationIndex"] = it.toString()
        }

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
    override fun remove(node: AnimatePropertyView.Node) {
        nodes.remove(node)
    }


    inner class Visitor : Animation.AnimationVisitor {
        override fun start(frameInSecond: Int, frameCount: Int) {
            this@AnimateFile.frameInSeconds = frameInSecond
            this@AnimateFile.frameCount = frameCount
        }

        override fun obj(path: String): Animation.ObjectVisitor? {
            val node = AnimateNode(path)
            nodes += node
            return ObjVisitor(node)
        }
    }

    private inner class ObjVisitor(val node: AnimateNode) : Animation.ObjectVisitor {
        override fun property(display: String, name: String, type: Field.Type): Animation.PropertyVisitor? {
            val animateProperty = AnimateProperty(
                    text = display,
                    name = name,
                    type = type/*when (type) {
                        Field.Type.FLOAT -> NodeService.FieldType.FLOAT
                        Field.Type.INT -> NodeService.FieldType.INT
                        Field.Type.STRING -> NodeService.FieldType.STRING
                        Field.Type.VEC2 -> NodeService.FieldType.VEC2
                        Field.Type.VEC3 -> NodeService.FieldType.VEC3
                    }*/,
                    node = node
            )
            node.properties += animateProperty
            return PropertyVisitor(animateProperty)
        }
    }

    private inner class PropertyVisitor(val property: AnimateProperty) : Animation.PropertyVisitor {
        override fun addFrame(time: Int, value: Any) {
            property.addFrame(time, value)
        }

    }

    init {
        Animation.load(StringReader(doc.text), Visitor())

//        val tree = mapper.readTree(doc.text)
//        frameInSeconds = tree["frameInSecond"].intValue()
//        frameCount = tree["frameCount"].intValue()
//
//        tree["objects"]?.array?.forEach { line ->
//            val path = line.obj["path"].textValue()
//            val animateNode = AnimateNode(path)
//            nodes += animateNode
//            line.obj["properties"].array.forEach { property ->
//                val text = property.obj["text"].textValue()
//                val name = property.obj["name"].textValue()
//                val type = NodeService.FieldType.valueOf(property.obj["type"].textValue())
//                val animateProperty = AnimateProperty(text = text, name = name, type = type, node = animateNode)
//                animateNode.properties += animateProperty
//                property.obj["frames"].array.forEach { frame ->
//                    val time = frame.obj["time"].intValue()
//                    val value = frame.obj["val"]?.textValue()?.let { fromString(type, it) }
//                    animateProperty.addFrame(time, value)
//                }
//            }
//        }
    }

    fun save() {
        val sb = StringBuilder()
        val r = Animation.AnimationJsonVisitor(sb)
        r.start(frameInSeconds, frameCount)
        nodes.forEach {
            val o = r.obj(it.nodePath)
            if (o != null)
                it.properties.forEach {
                    val l = o.property(
                            display = it.text,
                            name = it.name,
                            type = it.type/*when (it.type) {
                                NodeService.FieldType.FLOAT -> Animation.PropertyType.FLOAT
                                NodeService.FieldType.INT -> Animation.PropertyType.INT
                                NodeService.FieldType.STRING -> Animation.PropertyType.STRING
                                NodeService.FieldType.VEC2 -> Animation.PropertyType.VEC2
                                NodeService.FieldType.VEC3 -> Animation.PropertyType.VEC3
                            }*/
                    )
                    if (l != null)
                        it.iterator().forEach { f ->
                            l.addFrame(f.time, f.data)
                        }
                }
        }
        r.end()

        doc.setText(sb.toString())
//        val root = json(
//                "frameInSecond" to frameInSeconds.json(),
//                "frameCount" to frameCount.json(),
//                "objects" to nodes.map { node ->
//                    json(
//                            "path" to node.nodePath.json(),
//                            "properties" to node.properties.map { property ->
//                                json(
//                                        "type" to property.type.name.json(),
//                                        "text" to property.text.json(),
//                                        "name" to property.name.json(),
//                                        "frames" to property.iterator().map { frame ->
//                                            json(
//                                                    "time" to frame.time.json(),
//                                                    "val" to frame.data.let { toString(property.type, it) }?.json()
//                                            )
//                                        }.json()
//                                )
//                            }.json()
//                    )
//                }.json()
//        )
//        doc.setText(mapper.writeValueAsString(root))
    }

    class AnimateProperty(val node: AnimateNode, override val text: String, val name: String, val type: Field.Type) : AnimatePropertyView.Property, AnimateFrameView.FrameLine {
        override var lock: Boolean = false
        private val frames = TreeMap<Int, AnimateFrame>()

        inner class AnimateFrame(time: Int, var data: Any) : AnimateFrameView.Frame {
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

        fun addFrame(time: Int, data: Any) {
            frames[time] = AnimateFrame(time, data)
        }

        fun getFrameFor(time: Int): AnimateFrame? = frames.floorEntry(time)?.value
        fun getNextFrameFor(time: Int): AnimateFrame? = frames.ceilingEntry(time)?.value

        override fun iterator(): Iterator<AnimateFrame> =
                frames.values.iterator()

        val frameCount
            get() = frames.size

        override fun frame(time: Int): AnimateFrameView.Frame? = frames[time]

        override fun floorFrame(time: Int) = frames.floorEntry(time)?.value
        override fun ceilingFrame(time: Int) = frames.ceilingEntry(time)?.value

        override fun remove(frame: AnimateFrameView.Frame) {
            check(frames[frame.time] === frame)
            frames.remove(frame.time)
        }

        override fun remove(time: Int) {
            frames.remove(time)
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
        override fun remove(property: AnimatePropertyView.Property) {
            properties.remove(property)
        }

        override fun iterator(): Iterator<AnimateFrameView.Frame> = emptyList<AnimateFrameView.Frame>().iterator()
        override fun frame(time: Int): AnimateFrameView.Frame? = null
        override fun floorFrame(time: Int): AnimateFrameView.Frame? = null

        override fun ceilingFrame(time: Int): AnimateFrameView.Frame? = null

        override fun remove(frame: AnimateFrameView.Frame) {
            throw IllegalStateException()
        }

        override fun remove(time: Int) {
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

class CurrentAnimationField(override val node: EditAnimateNode) : NodeService.FieldInt() {
    override val id: Int
        get() = ScaleField2D::class.java.hashCode()
    override val groupName: String
        get() = "Animation"
    private var originalValue: Int? = null
    override var currentValue: Int
        get() = node.currentAnimation
        set(value) {
            node.currentAnimation = value
        }

    override fun clearTempValue() {
        originalValue = null
    }

    override val value: Int
        get() = originalValue ?: currentValue
    override val name: String
        get() = "animationIndex"
    override val displayName: String
        get() = "Animation"

    override fun setTempValue(value: Int) {
        if (originalValue == null)
            originalValue = node.currentAnimation
        node.currentAnimation = value
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.currentAnimation = originalValue!!
            originalValue = null
        }
    }

    override fun makeEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field<Int>>): AbstractEditor<Int> {
        return EditorAnimationSelector(sceneEditor, fields)
    }
}

class EditAnimateNode : Node() {
    private val filePaths = ArrayList<String>()
    val fileChangedEvent = EventDispatcher()
    val files: List<String>
        get() = filePaths

    val currentAnimationField = CurrentAnimationField(this)

    var currentAnimation = -1
        set(value) {
            field = value
            currentAnimationField.eventChange.dispatch()
        }

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

fun AnimateFile.AnimateNode.getNode(animation: EditAnimateNode) = animation.findByRelative(this.nodePath)