package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.sceneEditor.nodeLoader.NodeLoader

private val JsonNode.obj: ObjectNode
    get() = this as ObjectNode

private val JsonNode.array: ArrayNode
    get() = this as ArrayNode

private val JsonNode.string: String
    get() = this.textValue()!!

object SceneFileLoader {
    private val mapper = ObjectMapper()
    fun load(view: SceneEditorView, loaders: List<NodeLoader>, file: VirtualFile) {
        val node = file.inputStream.use {
            mapper.readTree(it)
        }
        val scene = node.obj["scene"]!!.array

        scene.forEach {
            val node = it!!.obj
            createNode(view, loaders, node).parent = view.sceneRoot
        }
    }


    private fun createNode(view: SceneEditorView, loaders: List<NodeLoader>, node: ObjectNode): Node {
        val className = node["class"]!!.string
        val loader = loaders.find { it.isCanLoad(className) } ?: TODO()
        val res = loader.load(view, node)

        node["childs"]?.array?.forEach {
            createNode(view, loaders, it!!.obj).parent = res
        }
        return res
    }

    fun save(view: SceneEditorView, loaders: List<NodeLoader>, file: VirtualFile) {
        val out = JsonNodeFactory.instance.objectNode()
        val scene = JsonNodeFactory.instance.arrayNode()
        out.set("scene", scene)
        view.sceneRoot.childs.forEach {
            scene.add(saveNode(view, loaders, it))
        }
        ApplicationManager.getApplication().runWriteAction {
            file.getOutputStream(view).use {
                mapper.writeValue(it, out)
            }
        }
    }

    private fun saveNode(view: SceneEditorView, loaders: List<NodeLoader>, node: Node): ObjectNode {
        val loader = loaders.find { it.isCanSave(node) } ?: TODO()
        val c = loader.save(view, node)
        c.set("class", JsonNodeFactory.instance.textNode(node::class.java.name))
        if (node.childs.isNotEmpty()) {
            val childs = JsonNodeFactory.instance.arrayNode()
            c.set("childs", childs)
            node.childs.forEach {
                childs.add(saveNode(view, loaders, it))
            }
        }
        return c
    }
}