package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Collider2D
import mogot.collider.Panel2DCollider
import mogot.math.*
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import pw.binom.utils.Vector2fmDelegator
import java.io.Closeable
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.collections.set

object Camera2DCreator : NodeCreator {
    override val name: String
        get() = "Camera2D"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/camera-icon-16.png"))

    override fun create(view: SceneEditorView): Node? {
        val cam = CameraSprite(view)
        Camera2DMeta(cam, view)
        return cam
    }

}

private class Camera2DMeta(val camera: CameraSprite, val view: SceneEditorView) : Closeable {
    val center = CenterNode2D(camera, view)
//    val s = SpriteFor3D(view)

    var hover = false
        set(value) {
            field = value
            camera.hover = value
        }
    var selected = false
        set(value) {
            field = value
            camera.selected = value
            center.visible = value
        }

    init {
        center.parent = view.editorRoot
        view.nodesMeta[camera] = this
        camera.size.set(800f, 600f)
        selected = false
        hover = false
    }

    override fun close() {
//        s.free()
        center.free()
        view.nodesMeta.remove(camera)
    }
}

class ZoomEditableField(override val node: CameraSprite) : NodeService.FieldFloat() {
    override val id: Int
        get() = ZoomEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Camera"
    override var currentValue: Any
        get() = node.zoom
        set(value) {
            node.zoom = value as Float
        }
    private var originalValue: Float? = null
    override val value: Any
        get() = originalValue ?: node.zoom

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "zoom"
    override val displayName: String
        get() = "Zoom"

    override fun setTempValue(value: Any) {
        if (originalValue == null) {
            originalValue = node.zoom
        }
        node.zoom = value as Float
    }

    override fun resetValue() {
        if (originalValue != null) {
            node.zoom = originalValue!!
            originalValue = null
        }
    }
}

object Camera2DService : NodeService {

    private val properties = listOf(Transform2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String =
            Camera2D::class.java.name

    override fun isEditor(node: Node): Boolean = node::class.java == CameraSprite::class.java

//    override fun clone(view: SceneEditorView, node: Node): Node? {
//        node as CameraSprite
//        val cam = CameraSprite(view)
//        Spatial2DService.cloneSpatial2D(node, cam)
//        Camera2DMeta(cam, view)
//        return cam
//    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        if (node !is CameraSprite) return
        val meta = view.nodesMeta[node] as Camera2DMeta
        meta.hover = hover
    }

    override val nodeClass: String
        get() = Camera2D::class.java.name

    override fun newInstance(view: SceneEditorView): Node {
        val cam = CameraSprite(view)
        Camera2DMeta(cam, view)
        return cam
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        if (node !is CameraSprite) return
        val meta = view.nodesMeta[node] as Camera2DMeta
        meta.selected = selected
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        node as CameraSprite
        val c = Panel2DCollider()
        c.node = node
        c.size.set(node.size)
        return c
    }
}

class CameraSprite(view: SceneEditorView) : VisualInstance2D(view.engine), EditableNode {


    val transformField = PositionField2D(this)
    val rotationField = RotationField2D(this)
    val zoomEditableField = ZoomEditableField(this)

    var zoom: Float = 1f
        set(value) {
            field = value
            zoomEditableField.eventChange.dispatch()
        }

    override val position: Vector2fm = Vector2fmDelegator(super.position) {
        transformField.eventChange.dispatch()
    }

    override var rotation: Float
        get() = super.rotation
        set(value) {
            super.rotation = value
            rotationField.eventChange.dispatch()
        }

    private var geom by ResourceHolder<Geom2D>()
    val size = Vector2fProperty()
    private val vertex = FloatDataBuffer.alloc(5 * 2)
    private val index = IntDataBuffer.alloc(5)
    private var mat by ResourceHolder(view.default3DMaterial.instance(Vector4f()))

    init {
        updateColor()
        (0 until 5).forEach {
            index[it] = it
        }
    }

    fun checkGeom() {
        if (geom == null) {
            geom = Geom2D(engine.gl, index, vertex, null, null)
            geom!!.mode = Geometry.RenderMode.LINES_STRIP
        }
        if (size.resetChangeFlag()) {
            vertex[0] = -size.x * 0.5f
            vertex[1] = -size.y * 0.5f

            vertex[2] = -size.x * 0.5f
            vertex[3] = size.y * 0.5f

            vertex[4] = size.x * 0.5f
            vertex[5] = size.y * 0.5f

            vertex[6] = size.x * 0.5f
            vertex[7] = -size.y * 0.5f

            vertex[8] = -size.x * 0.5f
            vertex[9] = -size.y * 0.5f
            geom!!.vertexBuffer.uploadArray(vertex)
        }
    }

    fun updateColor() {
        if (hover || selected)
            mat!!.color.set(1f, 0f, 0f, 1f)
        else
            mat!!.color.set(1f, 0f, 0f, 0.5f)
    }

    var hover = false
        set(value) {
            field = value
            updateColor()
        }

    var selected = false
        set(value) {
            field = value
            updateColor()
        }


    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        checkGeom()
        mat!!.use(model, projection, renderContext)
        geom!!.draw()
        mat!!.unuse()
    }

    override fun close() {
        mat = null
        geom = null
        vertex.close()
        index.close()
        super.close()
    }

    private val fields = listOf(transformField, rotationField)
    override fun getEditableFields(): List<NodeService.Field> = fields
}