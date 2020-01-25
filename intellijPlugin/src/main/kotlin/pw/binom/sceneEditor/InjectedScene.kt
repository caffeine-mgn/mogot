package pw.binom.sceneEditor

import com.intellij.psi.PsiFile
import mogot.Engine
import mogot.Node
import mogot.Spatial
import mogot.Spatial2D
import pw.binom.scene.Scene
import java.io.ByteArrayInputStream

interface InjectedScene {
    val sceneFile: PsiFile
    fun update()
}

class InjectedSceneImpl(val view: SceneEditorView, override val sceneFile: PsiFile) : InjectedScene {
    val changeTime = sceneFile.modificationStamp
    val isChanged
        get() = changeTime < sceneFile.modificationStamp

    override fun update() {
        if (!isChanged)
            return
        TODO()
        //createScene(view,sceneFile)
    }
}

class InjectedScene2D(val view: SceneEditorView, sceneFile: PsiFile) : Spatial2D(view.engine), InjectedScene by InjectedSceneImpl(view, sceneFile) {

}

class InjectedScene3D(val view: SceneEditorView, sceneFile: PsiFile) : Spatial(), InjectedScene by InjectedSceneImpl(view, sceneFile) {

}

fun loadInjectedScene(view: SceneEditorView, file: PsiFile): Node {
    val scene = file.virtualFile.inputStream.use {
        Scene.load(it)
    }

    val root: Node = when (scene.type) {
        Scene.Type.D3 -> InjectedScene3D(view, file)
        Scene.Type.D2 -> InjectedScene2D(view, file)
    }
    scene.childs.forEach {
        SceneFileLoader.loadNode(view, file.virtualFile, it)?.parent = root
    }
    return root
}