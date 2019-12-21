package pw.binom.sceneEditor.properties

import mogot.Node
import pw.binom.sceneEditor.SceneEditorView
import java.io.Closeable
import javax.swing.JComponent

interface PropertyFactory {
    fun create(view: SceneEditorView): Property
}

interface Property : Closeable {
    fun setNodes(nodes: List<Node>)
    val component: JComponent
}