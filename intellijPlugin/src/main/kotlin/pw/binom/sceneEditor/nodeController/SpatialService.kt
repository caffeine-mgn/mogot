package pw.binom.sceneEditor.nodeController

import mogot.Engine
import mogot.Field
import mogot.Node
import mogot.Spatial
import mogot.math.*
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory

class PositionField3D(override val node: Spatial) : NodeService.FieldVec3() {
    private var originalValue: Vector3fc? = null
    override val id: Int
        get() = PositionField2D::class.java.hashCode()
    override val groupName: String
        get() = "Transform"
    override var currentValue: Any
        get() = node.position
        set(value) {
            node.position.set(value as Vector3fc)
        }
    override val value: Any
        get() = originalValue ?: currentValue

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "position"
    override val displayName: String
        get() = "Position"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = Vector3f(node.position)
        node.position.set(value as Vector3fc)
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
    override var currentValue: Any
        get() = node.scale
        set(value) {
            node.scale.set(value as Vector3fc)
        }

    override fun clearTempValue() {
        originalValue = null
    }

    override val value: Any
        get() = originalValue ?: currentValue
    override val name: String
        get() = "scale"
    override val displayName: String
        get() = "Scale"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = Vector3f(node.scale)
        node.scale.set(value as Vector3fc)
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.scale.set(originalValue!!)
            originalValue = null
        }
    }
}

class RotateField3D(override val node: Spatial) : NodeService.AbstractField() {
    override var realValue: Any
        get() = node.quaternion
        set(value) {
            node.quaternion.set(value as Quaternionfc)
        }

    override fun cloneRealValue(): Any = Quaternionf(node.quaternion)

    override val groupName: String
        get() = "Transform"
    override val name: String
        get() = "rotate"
    override val displayName: String
        get() = "Rotation"
    override val fieldType: Field.Type
        get() = Field.Type.QUATERNION
}

object SpatialService : NodeService {

    private val props = listOf(Transform3DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false
    override val nodeClass: String
        get() = Spatial::class.java.name

    override fun newInstance(view: SceneEditorView): Node = Spatial()

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

    override fun isEditor(node: Node): Boolean = node::class.java === Spatial::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is Spatial) return null
        val out = Spatial()
        cloneSpatial(node, out)
        return out
    }
}