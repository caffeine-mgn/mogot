package pw.binom.sceneEditor.nodeController

import com.intellij.openapi.vfs.VirtualFile
import mogot.CSGBox
import mogot.MaterialNode
import mogot.Node
import mogot.collider.BoxCollider
import mogot.collider.Collider
import mogot.math.AABBm
import mogot.math.Vector4f
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import javax.swing.Icon
import javax.swing.ImageIcon

object CubeNodeCreator : NodeCreator {
    override val name: String
        get() = "CSVBox"
    override val icon: Icon = ImageIcon(this::class.java.classLoader.getResource("/cube-icon-16.png"))

    override fun create(view: SceneEditorView): Node {
        val node = CSGBox(view.engine)
        node.material.value = view.default3DMaterial.instance(Vector4f(1f))
        return node
    }
}

object CubeService : NodeService {
    private val props = listOf(Transform3DPropertyFactory, MaterialPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props
    override fun isEditor(node: Node): Boolean = node::class.java == CSGBox::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is CSGBox) return null
        val out = CSGBox(node.engine)
        out.width = node.width
        out.height = node.height
        out.depth = node.depth
        SpatialService.cloneSpatial(node, out)
        MaterialNodeUtils.clone(node, out)
        return out
    }

    override fun getCollider(node: Node): Collider? {
        node as CSGBox
        val collider = BoxCollider()
        collider.node = node
        collider.size.set(node.width, node.height, node.depth)
        return collider
    }

    override fun getAABB(node: Node, aabb: AABBm): Boolean {
        node as CSGBox
        aabb.position.set(0f)
        aabb.size.set(node.width, node.height, node.depth)
        return true
    }

    override fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node? {
        if (clazz != CSGBox::class.java.name)
            return null
        val node = CSGBox(view.engine)
        SpatialService.loadSpatial(view.engine, node, properties)
        MaterialNodeUtils.load(view, node, properties)
        if (node.material.value == null)
            node.material.value = view.default3DMaterial.instance(Vector4f(1f))
        return node
    }

    override fun save(view: SceneEditorView, node: Node): Map<String, String>? {
        if (node !is CSGBox)
            return null
        val out = HashMap<String, String>()
        SpatialService.saveSpatial(view.engine, node, out)
        MaterialNodeUtils.save(view, node, out)
        return out
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.selected = selected
    }

    override fun hover(node: Node, hover: Boolean) {
        node as MaterialNode
        val m = node.material.value as? MaterialInstance?
        m?.hover = hover
    }
}