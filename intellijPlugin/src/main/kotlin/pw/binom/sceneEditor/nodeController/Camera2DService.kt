package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.collider.Collider2D
import mogot.collider.Panel2DCollider
import mogot.math.Matrix4fc
import mogot.math.Vector2fProperty
import mogot.math.Vector4f
import mogot.math.set
import mogot.physics.d2.shapes.BoxShape2D
import pw.binom.FloatDataBuffer
import pw.binom.IntDataBuffer
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
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
//        s.size.set(40f, 32f)
//        view.engine.waitFrame {
//            s.internalMaterial = SolidTextureMaterial(view.engine).apply {
//                diffuseColor.set(0f, 0f, 0f, 0f)
//                tex = engine.resources.loadTextureResource("/camera-icon.png").gl
//            }
//        }
//        val b = FlatScreenBehaviour2D(camera)
//        s.behaviour = b
//        s.parent = view.editorRoot
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

object Camera2DService : NodeService {

    private val properties = listOf(Transform2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String =
            Camera2D::class.java.name

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Camera2D::class.java.name)
            return null

        val cam = CameraSprite(view)
        Spatial2DService.load(view.engine, cam, properties)
        Camera2DMeta(cam, view)
        return cam
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is CameraSprite) return null
        val data = HashMap<String, String>()
        Spatial2DService.save(view.engine, node, data)
        return data
    }

    override fun isEditor(node: Node): Boolean = node::class.java == CameraSprite::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        node as CameraSprite
        val cam = CameraSprite(view)
        Spatial2DService.cloneSpatial2D(node, cam)
        Camera2DMeta(cam, view)
        return cam
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        if (node !is CameraSprite) return
        val meta = view.nodesMeta[node] as Camera2DMeta
        meta.hover = hover
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

class CameraSprite(view: SceneEditorView) : VisualInstance2D(view.engine) {

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
}