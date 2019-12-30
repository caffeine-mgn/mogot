package pw.binom.sceneEditor

import mogot.Node
import javax.swing.Icon

interface NodeCreator {
    val name:String
    val icon:Icon?
    fun create(view: SceneEditorView):Node?
}