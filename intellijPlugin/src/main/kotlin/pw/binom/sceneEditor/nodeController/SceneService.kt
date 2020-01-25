package pw.binom.sceneEditor.nodeController

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import mogot.Node
import mogot.math.AABBm
import mogot.walk
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import javax.swing.Icon
import kotlin.collections.set
import mogot.scene.*

object InjectSceneCreator : NodeCreator {
    override val name: String
        get() = "Scene"
    override val icon: Icon?
        get() = SceneFileType.icon

    override fun create(view: SceneEditorView): Node? {
        val chooser = TreeFileChooserFactory
                .getInstance(view.editor1.project)
                .createFileChooser(
                        "Select Scene",
                        null,
                        null
                ) {
                    it.fileType == SceneFileType
                }
        chooser.showDialog()
        val file = chooser.selectedFile?.virtualFile ?: return null
        return loadInjectedScene(view, chooser.selectedFile!!)
    }
}

object InjectSceneNodeService : NodeService {

    override fun getClassName(node: Node): String = InjectSceneLoader.nodeClass

    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            when (node) {
                is InjectedScene2D -> listOf(Transform2DPropertyFactory)
                is InjectedScene3D -> listOf(Transform3DPropertyFactory)
                else -> emptyList()
            }

    override fun isInternalChilds(node: Node): Boolean = node is InjectedScene

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != InjectSceneLoader.nodeClass) {
            return null
        }
        val sceneFile = properties[InjectSceneLoader.FILE_PROPERTY] ?: run { println("properties file not found");return null }
        val virtualSceneFile = view.editor1.findFileByRelativePath(sceneFile)
                ?: run { println("Can't find relative file");return null }
        val psiSceneFile = PsiManager.getInstance(view.project).findFile(virtualSceneFile)
                ?: run { println("Can't get PSI file");return null }
        println("Try to load scene...")
        val node = loadInjectedScene(view, psiSceneFile)
        when (node) {
            is InjectedScene2D -> Spatial2DService.load(view.engine, node, properties)
            is InjectedScene3D -> SpatialService.loadSpatial(view.engine, node, properties)
        }
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        val scene = node as? InjectedScene ?: return null
        val filePath = view.editor1.getRelativePath(scene.sceneFile.virtualFile)
        val out = HashMap<String, String>()
        out[InjectSceneLoader.FILE_PROPERTY] = filePath
        when (node) {
            is InjectedScene2D -> Spatial2DService.save(view.engine, node, out)
            is InjectedScene3D -> SpatialService.saveSpatial(view.engine, node, out)
        }
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
        node.childs.forEach {
            it.walk {
                val service = view.getService(it)
                service?.selected(view, it)
                true
            }
        }
    }

    override fun unselected(view: SceneEditorView, node: Node) {
        node.childs.forEach {
            it.walk {
                val service = view.getService(it)
                service?.unselected(view, it)
                true
            }
        }
    }

    override fun isEditor(node: Node): Boolean =
            node::class.java.name == InjectedScene2D::class.java.name
                    || node::class.java.name == InjectedScene3D::class.java.name

    override fun clone(node: Node): Node? =
            when (node) {
                is InjectedScene2D -> {
                    val out = InjectedScene2D(node.view, node.sceneFile)
                    Spatial2DService.cloneSpatial2D(node, out)
                    out
                }
                is InjectedScene3D -> {
                    val out = InjectedScene3D(node.view, node.sceneFile)
                    SpatialService.cloneSpatial(node, out)
                    out
                }
                else -> null
            }

    override fun delete(view: SceneEditorView, node: Node) {

        fun deepDelete(node: Node) {
            node.childs.forEach {
                deepDelete(it)
            }
            val service = view.getService(node)
            if (service != null)
                service.delete(view, node)
            else
                EmptyNodeService.nodeDeleted(view.engine, node)
        }
        node.childs.forEach {
            deepDelete(it)
        }
        EmptyNodeService.nodeDeleted(view.engine, node)
    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean {
        return false
    }

    override fun hover(node: Node, hover: Boolean) {
        val view = (node as? InjectedScene2D)?.view
                ?: (node as? InjectedScene3D)?.view
                ?: return
        node.childs.forEach {
            it.walk {
                val service = view.getService(it)
                service?.hover(it, hover)
                true
            }
        }
    }
}