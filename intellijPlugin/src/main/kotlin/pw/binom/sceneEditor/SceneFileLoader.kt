package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.sceneEditor.nodeController.NodeController2

object SceneFileLoader {
    private val mapper = ObjectMapper()
    fun load(view: SceneEditorView, file: VirtualFile) {
        view.renderThread {
            val node = file.inputStream.use {
                mapper.readTree(it)
            }
            val scene = node.obj["scene"]!!.array

            scene.forEach {
                val node = it!!.obj
                createNode(view, node).parent = view.sceneRoot
            }
        }
    }


    private fun createNode(view: SceneEditorView, node: ObjectNode): Node {
        val className = node["class"]!!.string
        val res = view.services.asSequence().map { it.load(className, node) }.filterNotNull().firstOrNull()
                ?: TODO("Can't find loader for $className")

        node["childs"]?.array?.forEach {
            createNode(view, it!!.obj).parent = res
        }
        return res
    }

    fun save(view: SceneEditorView, file: VirtualFile) {
        val out = JsonNodeFactory.instance.objectNode()
        val scene = JsonNodeFactory.instance.arrayNode()
        out.set("scene", scene)
        view.sceneRoot.childs.forEach {
            scene.add(saveNode(view, it))
        }
        ApplicationManager.getApplication().runWriteAction {
            file.getOutputStream(view).use {
                mapper.writeValue(it, out)
            }
        }
    }

    private fun saveNode(view: SceneEditorView, node: Node): ObjectNode {
        val c = view.services.asSequence().map { it.save(node) }.filterNotNull().firstOrNull()
                ?: TODO("Can't save ${node::class.java.name}")
//        val loader = loaders.find { it.isCanSave(node) } ?: TODO()
//        val c = loader.save(view, node)
        c.set("class", JsonNodeFactory.instance.textNode(node::class.java.name))
        if (node.childs.isNotEmpty()) {
            val childs = JsonNodeFactory.instance.arrayNode()
            c.set("childs", childs)
            node.childs.forEach {
                childs.add(saveNode(view, it))
            }
        }
        return c
    }
}