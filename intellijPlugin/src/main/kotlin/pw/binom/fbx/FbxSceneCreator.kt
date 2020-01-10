package pw.binom.fbx

import com.intellij.openapi.vfs.VirtualFile
import mogot.Engine
import pw.binom.loadFbx

object FbxSceneCreator {
    fun create(engine: Engine, file: VirtualFile) {
        val fbx = engine.resources.loadFbx(file)
    }

}