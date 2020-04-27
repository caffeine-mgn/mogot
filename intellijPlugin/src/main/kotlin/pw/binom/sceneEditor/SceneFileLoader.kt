package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.Services
import pw.binom.scene.Scene
import pw.binom.sceneEditor.nodeController.EditableNode

object SceneFileLoader {
    private val mapper = ObjectMapper()
    private val services by Services.byClassSequence(NodeService::class.java)

    fun loadNode(view: SceneEditorView, file: VirtualFile, node: Scene.Node): Node? {
//        val out = services.map { it.load(view, file, node.className, node.properties) }
        val out = services.filter { it.nodeClass == node.className }
                .map { it.newInstance(view) }
                .firstOrNull()
                ?: run {
                    println("Can't load ${node.className}")
                    return null
                }
        out.id = node.id
        node.properties.forEach { (k, v) ->
            println("${node.className} -> $k")
            out as EditableNode
            val field = out.getEditableFields().find { it.name == k } ?: return@forEach
            if (v.isNotEmpty()) {
                field.currentValue = mogot.Field.Type.fromText(v)
            }
        }
        if (view.getService(out)?.isInternalChilds(out) != true)
            node.childs.forEach {
                loadNode(view, file, it)?.parent = out
            }
        out as EditableNode
        out.afterInit()
        return out
    }

    fun load(view: SceneEditorView, file: VirtualFile) {
        val scene = file.inputStream.use {
            Scene.load(it)
        }
        scene.childs.forEach {
            loadNode(view, file, it)?.parent = view.sceneRoot
        }
    }

    fun save(view: SceneEditorView, file: VirtualFile) {

        fun saveNode(node: Node): Scene.Node? {
            val editable = node as EditableNode
            val properties = HashMap<String, String>();
            editable.getEditableFields().forEach {
                properties[it.name] = it.fieldType.toString(it.value)
                it.getSubFields().forEach { subField ->
                    properties["${it.name}>${subField.name}"] = subField.fieldType.toString(subField.value)
                }
            }
            val childs = if (view.getService(node)?.isInternalChilds(node) != true) {
                node.childs.mapNotNull {
                    saveNode(it)
                }
            } else {
                emptyList()
            }
            val className = view.getService(node)?.getClassName(node) ?: node::class.java.name
            return Scene.Node(id = node.id, className = className, properties = properties, childs = childs)
        }

        ApplicationManager.getApplication().runWriteAction {
            file.getOutputStream(view).use { stream ->
                Scene(
                        view.sceneRoot.childs.map {
                            saveNode(it)
                        }.filterNotNull(), Scene.Type.D3).save(stream)
            }
        }
    }
}