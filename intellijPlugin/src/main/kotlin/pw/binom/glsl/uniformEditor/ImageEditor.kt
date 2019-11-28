package pw.binom.glsl.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.ui.components.JBPanel
import mogot.Engine
import mogot.Texture2D
import mogot.gl.Shader
import pw.binom.ShaderEditViewer
import java.awt.BorderLayout
import java.io.File
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.border.TitledBorder

object ImageEditorFactory : UniformEditor.EditorFactory {
    override fun create(project: Project, shaderEditViewer: ShaderEditViewer, document: Document, uniform: UniformEditor.Uniform): UniformEditor.Editor? {
        if (uniform.type == "sampler2D")
            return ImageEditor(project, document, shaderEditViewer, uniform)
        return null
    }

}

class ImageEditor(val project: Project, document: Document, val shaderEditViewer: ShaderEditViewer, uniform: UniformEditor.Uniform) : BaseEditor(document, uniform) {
    val btn = JButton("no image")


    private var file: File? = null
    private var oldFile: File? = null
    private var oldFileTime: Long? = null
    private var texture: Texture2D? = null


    override fun apply(engine: Engine, shader: Shader) {
        val gl = shaderEditViewer.engine.gl
        if (file != null) {
            if (oldFile?.path != file!!.path || file!!.lastModified() != oldFileTime || texture == null) {
                oldFile = file
                oldFileTime = file!!.lastModified()
                texture?.close()
                val tex = engine.resources.syncCreateTexture2D(file!!.path)
                gl.activeTexture(gl.TEXTURE0)
                gl.bindTexture(gl.TEXTURE_2D, tex.gl)
                shader.uniform(uniform.name, 0)
                shaderEditViewer.repaint()
                texture = tex
            } else {
                if (texture != null)
                    gl.bindTexture(gl.TEXTURE_2D, texture!!.gl)
            }
        } else {
            texture?.close()
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, null)
            shader.uniform(uniform.name, 0)
            shaderEditViewer.repaint()
            texture = null
        }


    }


    override fun reinit() {
        texture = null
        oldFile = null
    }

    private fun setFile(file: File?) {
        if (file != null) {
            this.file = File(file.path)
        }
        btn.text = this.file?.path ?: "no image"
        data = file?.path
    }

    init {
        add(btn, BorderLayout.CENTER)


        data?.let {
            val file = File(it).takeIf { it.isFile }
            setFile(file)
        }

        btn.addActionListener {
            val file = FileChooser.chooseFiles(
                    FileChooserDescriptor(
                            true,
                            false,
                            false,
                            false,
                            false,
                            false
                    ).withFileFilter { it.extension?.toLowerCase() == "png" },
                    project,
                    null
            ).let { it.firstOrNull() }
            setFile(file?.let { File(it.path) })
        }
    }
}