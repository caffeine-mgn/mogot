package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.CSGBox
import mogot.Node
import mogot.Spatial
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.PositionPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

object CubeNodeCreator : NodeCreator {
    override val name: String
        get() = "CSVBox"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/cube-icon-16.png"))

    override fun create(view: SceneEditorView): Node {
        val node = CSGBox(view.engine)
        node.material.value = view.default3DMaterial
        return node
    }
}

object CubeService : NodeService {
    private val props = listOf(PositionPropertyFactory, MaterialPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun isEditor(node: Node): Boolean = node::class.java == CSGBox::class.java
    override fun delete(view: SceneEditorView, node: Node) {
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != CSGBox::class.java.name)
            return null
        val node = CSGBox(view.engine)
        SpatialService.loadTransform(node, properties)
        MaterialNodeUtils.load(view, node, properties)
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is CSGBox)
            return null
        val out = HashMap<String, String>()
        SpatialService.saveTransform(node, out)
        MaterialNodeUtils.save(view, node, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node) {
    }

    override fun unselected(view: SceneEditorView, node: Node) {
    }
}