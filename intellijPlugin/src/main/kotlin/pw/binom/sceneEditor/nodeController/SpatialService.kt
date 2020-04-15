package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import mogot.Node
import mogot.Spatial
import mogot.math.*
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.utils.Vector3mDegrees

class PositionField3D(override val node: Spatial) : NodeService.FieldVec3() {
    private var originalValue: Vector3fc? = null
    override val id: Int
        get() = PositionField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    override var currentValue: Vector3fc
        get() = node.position
        set(value) {
            node.position.set(value)
        }
    override val value: Vector3fc
        get() = originalValue ?: currentValue

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "position"
    override val displayName: String
        get() = "Position"

    override fun setTempValue(value: Vector3fc) {
        if (originalValue == null)
            originalValue = Vector3f(node.position)
        node.position.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.position.set(originalValue!!)
            originalValue = null
        }
    }
}

class ScaleField3D(override val node: Spatial) : NodeService.FieldVec3() {
    override val id: Int
        get() = ScaleField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    private var originalValue: Vector3fc? = null
    override var currentValue: Vector3fc
        get() = node.scale
        set(value) {
            node.scale.set(value)
        }

    override fun clearTempValue() {
        originalValue = null
    }

    override val value: Vector3fc
        get() = originalValue ?: currentValue
    override val name: String
        get() = "scale"
    override val displayName: String
        get() = "Scale"

    override fun setTempValue(value: Vector3fc) {
        if (originalValue == null)
            originalValue = Vector3f(node.scale)
        node.scale.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.scale.set(originalValue!!)
            originalValue = null
        }
    }
}

class RotateField3D(override val node: Spatial) : NodeService.FieldVec3() {
    override val id: Int
        get() = RotateField3D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    private var originalValue: Vector3fc? = null
    private val internalValue = Vector3mDegrees(RotationVector(node.quaternion))
    override var currentValue: Vector3fc
        get() = internalValue
        set(value) {
            internalValue.set(value)
        }

    override fun clearTempValue() {
        originalValue = null
    }

    override val value: Vector3fc
        get() = originalValue ?: currentValue
    override val name: String
        get() = "rotate"
    override val displayName: String
        get() = "Rotation"

    override fun setTempValue(value: Vector3fc) {
        if (originalValue == null)
            originalValue = Vector3f(internalValue)
        internalValue.set(value)
    }

    override fun resetValue() {
        if (originalValue != null) {
            internalValue.set(originalValue!!)
            originalValue = null
        }
    }
}

object SpatialService : NodeService {

    private val props = listOf(Transform3DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false

    fun saveSpatial(engine: Engine, spatial: Spatial, output: MutableMap<String, String>) {
        EmptyNodeService.saveNode(engine, spatial, output)
        output["position.x"] = spatial.position.x.toString()
        output["position.y"] = spatial.position.y.toString()
        output["position.z"] = spatial.position.z.toString()

        output["scale.x"] = spatial.scale.x.toString()
        output["scale.y"] = spatial.scale.y.toString()
        output["scale.z"] = spatial.scale.z.toString()

        val rot = RotationVector(spatial.quaternion)
        output["rotation.x"] = rot.x.toString()
        output["rotation.y"] = rot.y.toString()
        output["rotation.z"] = rot.z.toString()
    }

    fun cloneSpatial(from: Spatial, to: Spatial) {
        EmptyNodeService.cloneNode(from, to)
        to.position.set(from.position)
        to.scale.set(from.scale)
        to.quaternion.set(from.quaternion)
    }

    fun loadSpatial(engine: Engine, spatial: Spatial, data: Map<String, String>) {
        EmptyNodeService.loadNode(engine, spatial, data)
        spatial.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f,
                data["position.z"]?.toFloat() ?: 0f
        )

        spatial.scale.set(
                data["scale.x"]?.toFloat() ?: 1f,
                data["scale.y"]?.toFloat() ?: 1f,
                data["scale.z"]?.toFloat() ?: 1f
        )

        val rot = RotationVector(spatial.quaternion)
        rot.set(
                data["rotation.x"]?.toFloat() ?: 0f,
                data["rotation.y"]?.toFloat() ?: 0f,
                data["rotation.z"]?.toFloat() ?: 0f
        )
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Spatial::class.java.name)
            return null
        val node = Spatial()
        loadSpatial(view.engine, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java !== Spatial::class.java)
            return null
        val out = HashMap<String, String>()
        saveSpatial(view.engine, node as Spatial, out)
        return out
    }

    override fun isEditor(node: Node): Boolean = node::class.java === Spatial::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is Spatial) return null
        val out = Spatial()
        cloneSpatial(node, out)
        return out
    }
}