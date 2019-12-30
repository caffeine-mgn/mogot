package pw.binom

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import pw.binom.sceneEditor.nodeController.*

class Starter : StartupActivity {
    override fun runActivity(project: Project) {
//        project.messageBus.connect().subscribe(ProjectTopics.MODULES, object : ModuleListener() {
//            fun moduleAdded(project: Project, module: Module) {
//                println("->${module}")
//            }
//        })

        Services.reg(CubeNodeCreator)
        Services.reg(OmniNodeCreator)
        Services.reg(OmniLightService)
        Services.reg(CubeService)
        Services.reg(FbxModelNodeCreator)
        Services.reg(SpatialService)
        Services.reg(GeomService)
    }
}