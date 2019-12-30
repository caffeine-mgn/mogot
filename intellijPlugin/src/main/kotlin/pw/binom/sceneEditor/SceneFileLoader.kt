package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.*
import pw.binom.scene.Scene

object SceneFileLoader {
    private val mapper = ObjectMapper()
    private val services by Services.byClassSequence(NodeService::class.java)


    fun load(view: SceneEditorView, file: VirtualFile) {

        fun loadNode(node: Scene.Node): Node? {
            val out = services.map { it.load(view, file, node.className, node.properties) }.filterNotNull().firstOrNull()
            node.childs.forEach {
                loadNode(it)?.parent = out
            }
            return out
        }

        view.renderThread {
            val scene = file.inputStream.use {
                Scene.load(it)
            }
            scene.childs.forEach {
                loadNode(it)?.parent = view.sceneRoot
            }
        }
    }

    fun save(view: SceneEditorView, file: VirtualFile) {

        fun saveNode(node: Node): Scene.Node? {
            val properties = services.asSequence().mapNotNull { it.save(view, node) }.firstOrNull() ?: return null
            val childs = node.childs.mapNotNull {
                saveNode(it)
            }
            return Scene.Node(className = node::class.java.name, properties = properties, childs = childs)
        }

        ApplicationManager.getApplication().runWriteAction {
            file.getOutputStream(view).use { stream ->
                Scene(
                        view.sceneRoot.childs.map {
                            saveNode(it)
                        }.filterNotNull()).save(stream)
            }
        }
    }
}