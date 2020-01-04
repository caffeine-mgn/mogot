package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import mogot.Spatial
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView

object SpatialService : NodeService {
    fun saveSpatial(spatial: Spatial, output: MutableMap<String, String>) {
        EmptyNodeService.saveNode(spatial, output)
        output["position.x"] = spatial.position.x.toString()
        output["position.y"] = spatial.position.y.toString()
        output["position.z"] = spatial.position.z.toString()
    }

    fun loadSpatial(spatial: Spatial, data: Map<String, String>) {
        EmptyNodeService.loadNode(spatial, data)
        spatial.position.set(
                data["position.x"]?.toFloat() ?: 0f,
                data["position.y"]?.toFloat() ?: 0f,
                data["position.z"]?.toFloat() ?: 0f
        )
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != Spatial::class.java.name)
            return null
        val node = Spatial()
        loadSpatial(node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node::class.java !== Spatial::class.java)
            return null
        val out = HashMap<String, String>()
        saveSpatial(node as Spatial, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }

    override fun isEditor(node: Node): Boolean = node::class.java === Spatial::class.java

    override fun delete(view: SceneEditorView, node: Node) {
    }

}