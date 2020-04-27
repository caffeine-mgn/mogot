package pw.binom.sceneEditor.nodeController

import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.openapi.vfs.VirtualFile
import mogot.GeomNode
import mogot.Node
import mogot.Spatial
import mogot.math.AABBm
import mogot.math.Quaternionfm
import mogot.math.Vector3fm
import mogot.math.Vector4f
import pw.binom.ExternalFbx
import pw.binom.FbxGeom
import pw.binom.fbx.file.FbxModel2
import pw.binom.loadFbx
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.MaterialPropertyFactory
import pw.binom.sceneEditor.properties.Transform3DPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.utils.QuaternionfmDelegator
import pw.binom.utils.Vector3fmDelegator
import javax.swing.Icon

class FbxModelEditableField(var view: SceneEditorView, override val node: EditableGeomNode) : NodeService.FieldFile() {
    override val id: Int
        get() = FbxModelEditableField::class.java.hashCode()
    override val groupName: String
        get() = "Geometry"
    override var currentValue: Any
        get() = file
        set(value) {
            file = value as String
        }
    var file = ""
        set(value) {
            if (value.isEmpty()) {
                field = value
                return
            }
            val items = value.split(mogot.Field.Type.INTERNAL_SPLITOR)
            val filePath = items[0]
            val internal = items[1]
            val file = view.editor1.findFileByRelativePath(filePath)
            if (file == null) {
                field = ""
                node.geom.value = null
                return
            }
            field = value
            val fbx = view.engine.resources.loadFbx(file)
            node.geom.value = fbx.getGeom(internal)
            fbx.dec()
        }
    private var originalValue: String? = null
    override val value: Any
        get() = originalValue ?: file

    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "file"
    override val displayName: String
        get() = "Model"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = file
        file = value as String
    }

    override fun resetValue() {
        if (originalValue != null) {
            file = originalValue!!
            originalValue = null
        }
    }
}

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

class EditableGeomNode(view: SceneEditorView) : GeomNode(), EditableNode {
    val file = FbxModelEditableField(view, this)
    override val position: Vector3fm = Vector3fmDelegator(super.position) {
        positionField.eventChange.dispatch()
    }
    override val scale: Vector3fm = Vector3fmDelegator(super.scale) {
        positionField.eventChange.dispatch()
    }
    override val quaternion: Quaternionfm = QuaternionfmDelegator(super.quaternion) {
        rotationField.eventChange.dispatch()
    }
    val positionField = PositionField3D(this)
    val scaleField = ScaleField3D(this)
    val rotationField = RotateField3D(this)
    val materialField = MaterialField(view, this)
    val fields = listOf(positionField, rotationField, scaleField, materialField, file)
    override fun getEditableFields(): List<NodeService.Field> = fields
}

private fun makeFbxScene(view: SceneEditorView, fbx: ExternalFbx): Spatial {
    val root = Spatial()
    val meta = fbx.meta ?: TODO()

    fun buildNode(model: FbxModel2, parent: Node) {
        val node = if (model.geometry != null) {
            val node = EditableGeomNode(view)
            node.geom.value = fbx.getGeom(model.geometry!!.name)
            node.parent = parent
            node.material.value = view.default3DMaterial.instance(Vector4f(1f))
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

    private val props = listOf(Transform3DPropertyFactory, MaterialPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = props

    override fun getAABB(node: Node, aabb: AABBm): Boolean = false
    override val nodeClass: String
        get() = GeomNode::class.java.name

    override fun newInstance(view: SceneEditorView): Node = EditableGeomNode(view)

//    override fun clone(view: SceneEditorView, node: Node): Node? {
//        if (node !is EditableGeomNode) return null
//        val out = EditableGeomNode(view)
//        out.geom.value = node.geom.value
//        SpatialService.cloneSpatial(node, out)
//        MaterialNodeUtils.clone(node, out)
//        return out
//    }

    override fun isEditor(node: Node): Boolean = node::class.java === EditableGeomNode::class.java
}