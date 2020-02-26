package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.math.*
import pw.binom.SolidTextureMaterial
import pw.binom.io.Closeable
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.CameraPropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

private class CamerasManager(val engine: Engine) : Closeable {
    val cameras = HashMap<Camera, FlatScreenBehaviour>()

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
        val cam = Camera()
        createStub(view, cam)
        return cam
    }
}

private fun createStub(view: SceneEditorView, camera: Camera) {
    view.renderThread {
        val s = SpriteFor3D(view)
        s.size.set(40f, 32f)
        s.internalMaterial = SolidTextureMaterial(view.engine).apply {
            diffuseColor.set(0f, 0f, 0f, 0f)
            tex = view.engine.camerasManager.cameraLightTexture.gl
        }
        val b = FlatScreenBehaviour(view.engine, view.editorCamera, camera)
        s.behaviour = b
        s.parent = view.editorRoot
        view.engine.camerasManager.cameras[camera] = b
        camera.resize(800, 600)
        camera.near = 0.3f
        camera.far = 30f
        camera.fieldOfView = 45f
    }
}

object CameraService : NodeService {
    private val props = listOf(Transform3DPropertyFactory, CameraPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Camera::class.java.name)
            return null
        val node = Camera()
        SpatialService.loadSpatial(view.engine, node, properties)
        node.resize(800, 600)
        properties["near"]?.toFloatOrNull()?.let { node.near = it }
        properties["far"]?.toFloatOrNull()?.let { node.far = it }
        properties["fieldOfView"]?.toFloatOrNull()?.let { node.fieldOfView = it }
        createStub(view, node)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is Camera)
            return null
        val out = HashMap<String, String>()
        SpatialService.saveSpatial(view.engine, node, out)
        out["near"] = node.near.toString()
        out["far"] = node.far.toString()
        out["fieldOfView"] = node.fieldOfView.toString()
        return out
    }

    private val frustums = HashMap<Camera, FrustumNode>()

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        val camera = node as Camera
        if (selected) {
            val f = FrustumNode(view.engine, camera)
            f.parent = view.editorRoot
            f.material.value = view.default3DMaterial.instance(Vector4f(1f))
            frustums[camera] = f
        } else {
            frustums.remove(node)?.apply {
                parent = null
                view.engine.waitFrame {
                    close()
                }
            }
        }

        val sprite = view.engine.camerasManager.cameras[node]!!.node
        val material = sprite.internalMaterial as SolidTextureMaterial
        if (selected)
            material.diffuseColor.set(0.5f, 0.5f, 0.5f, 0f)
        else
            material.diffuseColor.set(0f, 0f, 0f, 0f)
    }

    override fun isEditor(node: Node): Boolean = node::class.java == Camera::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is Camera) return null
        val out = Camera()
        out.resize(node.width, node.height)
        out.near = node.near
        out.far = node.far
        out.fieldOfView = node.fieldOfView
        out.id = node.id
        SpatialService.cloneSpatial(node, out)
        return out
    }

    override fun delete(view: SceneEditorView, node: Node) {
        if (node !is Camera) return
        super.delete(view, node)
        view.engine.camerasManager.cameras.remove(node)?.node?.let {
            it.parent = null
            it.close()
        }
    }
}