package pw.binom.sceneEditor.editors

import pw.binom.sceneEditor.SceneEditorView
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

interface EditActionFactory {
    fun mouseDown(view: SceneEditorView, e: MouseEvent) {}
    fun keyDown(view: SceneEditorView, e: KeyEvent) {}
}