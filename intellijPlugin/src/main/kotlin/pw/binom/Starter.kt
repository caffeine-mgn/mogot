package pw.binom

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import pw.binom.sceneEditor.editors.*
import pw.binom.sceneEditor.nodeController.*

class Starter : StartupActivity {
    override fun runActivity(project: Project) {
//        project.messageBus.connect().subscribe(ProjectTopics.MODULES, object : ModuleListener() {
//            fun moduleAdded(project: Project, module: Module) {
//                println("->${module}")
//            }
//        })

        Services.reg(EditorView2DFactory)
        Services.reg(EditMovementFactory3D)
        Services.reg(EditRotate3DFactory)
        Services.reg(FpsCamEditorFactory)
        Services.reg(EditMovementFactory2D)
        Services.reg(CubeNodeCreator)
        Services.reg(OmniNodeCreator)
        Services.reg(OmniLightService)
        Services.reg(CubeService)
        Services.reg(FbxModelNodeCreator)
        Services.reg(SpatialService)
        Services.reg(GeomService)
        Services.reg(CameraService)
        Services.reg(CameraNodeCreator)
        Services.reg(Sprite2DCreator)
        Services.reg(Sprite2DService)
    }
}