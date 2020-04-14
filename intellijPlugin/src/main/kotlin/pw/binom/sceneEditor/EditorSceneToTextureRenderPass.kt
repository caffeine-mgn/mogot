package pw.binom.sceneEditor
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.math.Matrix4fc
import mogot.rendering.*
import pw.binom.SolidMaterial

class EditorSceneToTextureRenderPass() : SceneToTextureRenderPass() {
    var grid: Grid3D? = null
    //var editorRoot: mogot.Node? = null
    override fun customPreDraw3D(model: Matrix4fc, projection: Matrix4fc, context: Display.Context){
        if(grid!=null) {
            context.update(grid!!)
            context.renderNode3D(grid!!, model, projection, context)
        }
    }

    override fun setup(context: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels) {
        if(grid == null){
            grid = Grid3D(gl)
            val mat = SolidMaterial(gl)
            grid?.material?.value = mat
            mat.diffuseColor.set(1f, 1f, 1f, 0.5f)
        }
        super.setup(context, gl, msaaLevel)
    }
}