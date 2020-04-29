package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Camera
import mogot.Engine
import mogot.Node
import mogot.math.Quaternionfm
import mogot.math.Vector3fm
import mogot.math.Vector4f
import pw.binom.SolidTextureMaterial
import pw.binom.io.Closeable
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.CameraPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.utils.QuaternionfmDelegator
import pw.binom.utils.Vector3fmDelegator
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.collections.set
import kotlin.properties.Delegates

class NearEditableField(override val node: Camera) : NodeService.FieldFloat() {
    override val id: Int
        get() = NearEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Camera"
    override var currentValue: Any
        get() = node.near
        set(value) {
            node.near = value as Float
        }
    private var originalValue: Float? = null
    override val value: Any
        get() = originalValue ?: node.near

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "near"
    override val displayName: String
        get() = "Near"

    override fun setTempValue(value: Any) {
        if (originalValue == null) {
            originalValue = node.near
        }
        node.near = value as Float
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.near = originalValue!!
            originalValue = null
        }
    }
}

class FarEditableField(override val node: Camera) : NodeService.FieldFloat() {
    override val id: Int
        get() = FarEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Camera"
    override var currentValue: Any
        get() = node.far
        set(value) {
            node.far = value as Float
        }
    private var originalValue: Float? = null
    override val value: Any
        get() = originalValue ?: node.far

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "far"
    override val displayName: String
        get() = "Far"

    override fun setTempValue(value: Any) {
        if (originalValue == null) {
            originalValue = node.far
        }
        node.far = value as Float
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.far = originalValue!!
            originalValue = null
        }
    }
}

class FieldOfViewEditableField(override val node: Camera) : NodeService.FieldFloat() {
    override val id: Int
        get() = FieldOfViewEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Camera"
    override var currentValue: Any
        get() = node.fieldOfView
        set(value) {
            node.fieldOfView = value as Float
        }
    private var originalValue: Float? = null
    override val value: Any
        get() = originalValue ?: node.fieldOfView

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "fieldOfView"
    override val displayName: String
        get() = "Field Of View"

    override fun setTempValue(value: Any) {
        if (originalValue == null) {
            originalValue = node.fieldOfView
        }
        node.fieldOfView = value as Float
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.fieldOfView = originalValue!!
            originalValue = null
        }
    }
}

private class CamerasManager(val engine: Engine) : Closeable {

    lateinit var cameraLightTexture: ExternalTexture

    init {
        engine.editor.renderThread {
            cameraLightTexture = engine.resources.loadTextureResource("/camera-icon.png")
            cameraLightTexture.inc()
        }
    }

    override fun close() {
        cameraLightTexture.dec()
    }
}

private val Engine.camerasManager: CamerasManager
    get() = manager("camerasManager") { CamerasManager(this) }

object CameraNodeCreator : NodeCreator {
    override val name: String
        get() = "Camera"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/camera-icon-16.png"))

    override fun create(view: SceneEditorView): Camera {
        val cam = EditableCamera(view)
        return cam
    }
}

class EditableCamera(val view: SceneEditorView) : Camera(view.engine), EditableNode {
    override val position: Vector3fm = Vector3fmDelegator(super.position) {
        positionField.eventChange.dispatch()
    }
    override val quaternion: Quaternionfm = QuaternionfmDelegator(super.quaternion) {
        rotationField.eventChange.dispatch()
    }

    override var near: Float by Delegates.observable(super.near) { _, _, new ->
        nearEditableField.eventChange.dispatch()
        super.near = new
    }

    override var far: Float by Delegates.observable(super.far) { _, _, new ->
        farEditableField.eventChange.dispatch()
        super.far = new
    }

    override var fieldOfView: Float by Delegates.observable(super.fieldOfView) { _, _, new ->
        fieldOfViewEditableField.eventChange.dispatch()
        super.fieldOfView = new
    }


    val positionField = PositionField3D(this)
    val rotationField = RotateField3D(this)
    val nearEditableField = NearEditableField(this)
    val farEditableField = FarEditableField(this)
    val fieldOfViewEditableField = FieldOfViewEditableField(this)
    private val fields = listOf(positionField, rotationField, nearEditableField, farEditableField, fieldOfViewEditableField)
    override fun getEditableFields(): List<NodeService.Field> = fields

    val s = SpriteFor3D(view)
    val b = FlatScreenBehaviour(view.engine, view.editorCamera, this)

    init {
        resize(800, 600)
        near = 0.3f
        far = 30f
        fieldOfView = 45f

        s.size.set(40f, 32f)
        view.engine.editor.renderThread {
            s.internalMaterial = SolidTextureMaterial(view.engine).apply {
                diffuseColor.set(0f, 0f, 0f, 0f)
                tex = view.engine.camerasManager.cameraLightTexture.gl
            }
        }
        s.behaviour = b
        s.parent = view.editorRoot
    }

    private var f: FrustumNode? = null
    var selected: Boolean = false
        set(value) {
            val material = s.internalMaterial as SolidTextureMaterial
            if (value) {
                material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
                val f = FrustumNode(view.engine, this)
                f.parent = view.editorRoot
                f.material.value = view.default3DMaterial.instance(Vector4f(1f))
                this.f = f
            } else {
                material.diffuseColor.set(0f, 0f, 0f, 0f)
                f?.close()
            }
            field = value
        }

    override fun close() {
        f?.free()
        s.free()
        super.close()
    }
}

object CameraService : NodeService {
    private val props = listOf(Transform3DPropertyFactory, CameraPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        val camera = node as EditableCamera


        camera.selected = selected
    }

    override fun isEditor(node: Node): Boolean = node::class.java == EditableCamera::class.java
//    override fun clone(view: SceneEditorView, node: Node): Node? {
//        if (node !is EditableCamera) return null
//        val out = EditableCamera(view)
//        out.resize(node.width, node.height)
//        out.near = node.near
//        out.far = node.far
//        out.fieldOfView = node.fieldOfView
//        out.id = node.id
//        SpatialService.cloneSpatial(node, out)
//        return out
//    }

    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is EditableCamera) return
        super.delete(view, node)
    }

    override fun getClassName(node: Node): String = Camera::class.java.name

    override val nodeClass: String
        get() = Camera::class.java.name

    override fun newInstance(view: SceneEditorView): Node {
        val node = EditableCamera(view)
        return node
    }


}