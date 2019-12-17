package pw.binom

import mogot.*
import mogot.gl.GL
import mogot.gl.GLView
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.*
import pw.binom.material.compiler.Compiler
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.material.psi.Parser
import java.awt.Dimension
import java.io.StringReader
import javax.swing.JFrame

class FullScreenSprite(engine: Engine) {
    var material: Material? = null
    private val rect = Rect2D(engine.gl, Vector2f(16f, 16f))
    private val projection = Matrix4f().ortho2D(0f, 16f, 16f, 0f)

    fun draw(renderContext: RenderContext) {
        val mat = material ?: return
        mat.use(MATRIX4_ONE, this.projection, renderContext)
        rect.draw()
        mat.unuse()
    }
}

class DDShaderMaterial(gl: GL, vp: String, fp: String) : MaterialGLSL(gl) {
    override val shader: Shader = Shader(gl, vp, fp)

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)

    override fun close() {
        shader.close()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}

class DDD : GLView() {

    private var closed = false
    private var inited = false
    override val root: Node = Node()
    var cam = Camera()

    override var camera: Camera? = cam

    override fun init() {
        super.init()
        inited = true

        cam.parent = root
        val box = CSGBox(engine)
        box.parent = root
        cam.position.set(3f, 3f, 3f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
//        box.material = SimpleMaterial(engine.gl)

        val shader = """
            @vertex
            vec3 vertexPos
            
            @normal
            vec3 normalList
            
            @uv
            vec2 vertexUV
            
            @projection
            mat4 projection
            
            @model
            mat4 model
            
            vec3 normal
            
            vec4 vertex(){
                mat3 normalMatrix = mat3(transpose(inverse(model)))
                normal = vec3(normalMatrix * normalList)
                return vec4(projection * model * vec4(vertexPos, 1f))
            }
            
            vec4 fragment(vec4 color2){
                return vec4(1f,1f,1f,1f)
            }
        """
        val p = Parser(StringReader(shader))
        val compiler = Compiler(p)
        val vv = GLES300Generator(compiler)

        println("VP:")
        vv.vp.lineSequence().forEachIndexed { index, s ->
            println("${index+1}: $s")
        }

        println("\n\nFP:")
        vv.fp.lineSequence().forEachIndexed { index, s ->
            println("${index+1}: $s")
        }


        val fp = """#version 300 es
            precision mediump float;
            out vec4 color;
            void main() {color=vec4(1.0f,1.0f,1.0f,1.0f);}
        """

        val vp="""#version 300 es
           precision mediump float;
           uniform mat4 projection;
           uniform mat4 model;
           layout(location = 0) in vec3 vertexPos;
           
           void main() {
               gl_Position = projection * model * vec4(vertexPos, 1.0);
           }
        """

        box.material=DDShaderMaterial(engine.gl, vv.vp, vv.fp)
    }

    override fun dispose() {
        closed = true
        super.dispose()
    }

    fun startRender() {
        while (!inited) {
            Thread.sleep(1)
        }
        while (!closed) {
            display()
        }
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        //val view3d = FbxViewer(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\main\\resources\\untitled.fbx").inputStream().readAllBytes())
        val view3d = DDD()
        val f = JFrame()
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.size = Dimension(800, 600)
        f.add(view3d)
        f.isVisible = true
        view3d.startRender()
    }
}