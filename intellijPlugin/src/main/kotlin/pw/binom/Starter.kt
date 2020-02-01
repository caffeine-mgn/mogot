package pw.binom

import com.intellij.ide.ApplicationLoadListener
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import pw.binom.sceneEditor.editors.*
import pw.binom.sceneEditor.nodeController.*

class Starter : ApplicationLoadListener {
    override fun beforeApplicationLoaded(application: Application, configPath: String) {
        reg()
    }

    fun reg() {
        Services.reg(PolygonShape2DCreator)
        Services.reg(PolygonShape2DService)
        Services.reg(Body2DCreator)
        Services.reg(Body2DService)
        Services.reg(BoxShape2DCreator)
        Services.reg(BoxShape2DService)
        Services.reg(InjectSceneCreator)
        Services.reg(InjectSceneNodeService)
        Services.reg(EditorView2DFactory)
        Services.reg(EditRotate2DFactory)
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