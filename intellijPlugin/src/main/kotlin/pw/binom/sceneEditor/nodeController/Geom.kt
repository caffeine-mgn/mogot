package pw.binom.sceneEditor.nodeController

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.vfs.VirtualFile
import mogot.GeomNode
import mogot.Node
import mogot.Spatial
import pw.binom.ExternalFbx
import pw.binom.FbxGeom
import pw.binom.fbx.file.FbxModel2
import pw.binom.loadFbx
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.PositionPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon


object FbxModelNodeCreator : NodeCreator {
    override val name: String
        get() = "Fbx Model"
    override val icon: Icon? = null

    override fun create(view: SceneEditorView): Node? {
        val chooser = TreeFileChooserFactory
                .getInstance(view.editor1.project)
                .createFileChooser(
                        "Select Fbx Model",
                        null,
                        null
                ) {
                    it.virtualFile.extension?.toLowerCase() == "fbx"
                }
        chooser.showDialog()
        val file = chooser.selectedFile?.virtualFile ?: return null
        val fbx = view.engine.resources.loadFbx(file)
        return makeFbxScene(view, fbx)
    }
}

private fun makeFbxScene(view: SceneEditorView, fbx: ExternalFbx): Spatial {
    val root = Spatial()
    val meta = fbx.meta ?: TODO()

    fun buildNode(model: FbxModel2, parent: Node) {
        val node = if (model.geometry != null) {
            val node = GeomNode()
            node.geom.value = fbx.getGeom(model.geometry!!.name)
            node.parent = parent
            node.material.value = view.default3DMaterial
            node
        } else {
            TODO()
        }

        model.childs.forEach {
            buildNode(it, node)
        }

    }

    meta.childs.asSequence().filter { it.geometry != null }.forEach {
        buildNode(it, root)
    }
    return root
}

object GeomService : NodeService {

    private val props = listOf(PositionPropertyFactory, MaterialPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != GeomNode::class.java.name) return null
        val node = GeomNode()
        node.material.value = view.default3DMaterial
        SpatialService.loadSpatial(node, properties)
        val virtualFile = properties["file"]?.let { view.editor1.findFileByRelativePath(it) }
        virtualFile ?: return null
        val fbx = view.engine.resources.loadFbx(virtualFile)
        node.geom.value = properties["geom"]?.let { fbx.getGeom(it) } ?: return null
        node.material.value = view.default3DMaterial
        MaterialNodeUtils.load(view, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is GeomNode) return null
        val out = HashMap<String, String>()
        SpatialService.saveSpatial(node, out)
        val geom = node.geom.value as FbxGeom
        if (geom.fbx.file.isInLocalFileSystem) {
            out["file"] = view.editor1.getRelativePath(geom.fbx.file)
        }
        out["geom"] = geom.name

        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }

    override fun isEditor(node: Node): Boolean = node::class.java === GeomNode::class.java

    override fun delete(view: SceneEditorView, node: Node) {
    }

}