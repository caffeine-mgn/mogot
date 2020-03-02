package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import mogot.Node
import mogot.Spatial2D
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.set
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import kotlin.collections.set

class PositionField2D(override val node: Spatial2D) : NodeService.FieldVec2() {
    private var originalValue: Vector2fc? = null
    override val id: Int
        get() = PositionField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    override var currentValue: Vector2fc
        get() = node.position
        set(value) {
            node.position.set(value)
        }
    override val value: Vector2fc
        get() = originalValue ?: currentValue
    override val name: String
        get() = "transform"
    override val displayName: String
        get() = "Position"

    override fun setTempValue(value: Vector2fc) {
        if (originalValue == null)
            originalValue = Vector2f(node.position)
        node.position.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.position.set(originalValue!!)
            originalValue = null
        }
    }
}

class ScaleField2D(override val node: Spatial2D) : NodeService.FieldVec2() {
    override val id: Int
        get() = ScaleField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    private var originalValue: Vector2fc? = null
    override var currentValue: Vector2fc
        get() = node.scale
        set(value) {
            node.scale.set(value)
        }
    override val value: Vector2fc
        get() = originalValue ?: currentValue
    override val name: String
        get() = "scale"
    override val displayName: String
        get() = "Scale"

    override fun setTempValue(value: Vector2fc) {
        if (originalValue == null)
            originalValue = Vector2f(node.scale)
        node.scale.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.position.set(originalValue!!)
            originalValue = null
        }
    }
}

class RotationField2D(override val node: Spatial2D) : NodeService.FieldFloat() {
    override val id: Int
        get() = RotationField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    private var originalValue: Float? = null
    override var currentValue: Float
        get() = node.rotation
        set(value) {
            node.scale.set(value)
        }
    override val value: Float
        get() = originalValue ?: currentValue
    override val name: String
        get() = "rotation"
    override val displayName: String
        get() = "Rotation"


    override fun setTempValue(value: Float) {
        if (originalValue == null)
            originalValue = node.rotation
        node.scale.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.rotation = originalValue!!
            originalValue = null
        }
    }
}

object Spatial2DService : NodeService {

    private val props = listOf(Transform2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            props

    fun cloneSpatial2D(from: Spatial2D, to: Spatial2D) {
        EmptyNodeService.cloneNode(from, to)
        to.position.set(from.position)
        to.rotation = from.rotation
        to.scale.set(from.scale)
    }

    fun save(engine: Engine, node: Spatial2D, data: MutableMap<String, String>) {
        EmptyNodeService.saveNode(engine, node, data)
        data["position.x"] = node.position.x.toString()
        data["position.y"] = node.position.y.toString()

        data["scale.x"] = node.scale.x.toString()
        data["scale.y"] = node.scale.y.toString()

        data["rotation"] = node.rotation.toString()
    }

    fun load(engine: Engine, node: Spatial2D, data: Map<String, String>) {
        EmptyNodeService.loadNode(engine, node, data)
        node.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f
        )
        node.scale.set(
                data["scale.x"]?.toFloat() ?: 1f,
                data["scale.y"]?.toFloat() ?: 1f
        )
        node.rotation = data["rotation"]?.toFloat() ?: 0f
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Spatial2D::class.java.name)
            return null
        val node = Spatial2D(view.engine)
        load(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java !== Spatial2D::class.java)
            return null
        val out = HashMap<String, String>()
        save(view.engine, node as Spatial2D, out)
        return out
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Spatial2D::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is Spatial2D) return null
        val out = Spatial2D(node.engine)
        cloneSpatial2D(node, out)
        return out
    }
}