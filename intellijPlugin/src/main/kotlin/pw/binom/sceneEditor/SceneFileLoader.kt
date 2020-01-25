package pw.binom.sceneEditor

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import mogot.Node
import pw.binom.Services
import pw.binom.scene.Scene

object SceneFileLoader {
    private val mapper = ObjectMapper()
    private val services by Services.byClassSequence(NodeService::class.java)

    fun loadNode(view: SceneEditorView, file: VirtualFile, node: Scene.Node): Node? {
        val out = services.map { it.load(view, file, node.className, node.properties) }
                .filterNotNull()
                .firstOrNull()
                ?: run {
                    println("Can't load ${node.className}")
                    return null
                }
        if (view.getService(out)?.isInternalChilds(out) != true)
            node.childs.forEach {
                loadNode(view, file, it)?.parent = out
            }
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
            val properties = services.asSequence().mapNotNull { it.save(view, node) }.firstOrNull() ?: return null
            val childs = if (view.getService(node)?.isInternalChilds(node) != true) {
                node.childs.mapNotNull {
                    saveNode(it)
                }
            } else {
                emptyList()
            }
            val className = view.getService(node)?.getClassName(node) ?: node::class.java.name
            return Scene.Node(className = className, properties = properties, childs = childs)
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