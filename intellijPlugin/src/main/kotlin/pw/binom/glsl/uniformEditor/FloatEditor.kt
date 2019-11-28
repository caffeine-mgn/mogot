package pw.binom.glsl.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import mogot.Engine
import mogot.gl.Shader
import pw.binom.ShaderEditViewer
import java.awt.BorderLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

object FloatEditorFactory : UniformEditor.EditorFactory {
    override fun create(project: Project, shaderEditViewer: ShaderEditViewer, document: Document, uniform: UniformEditor.Uniform): UniformEditor.Editor? {
        if (uniform.type == "float")
            return FloatEditor(shaderEditViewer, document, uniform)
        return null
    }
}

class FloatEditor(val shaderEditViewer: ShaderEditViewer, document: Document, uniform: UniformEditor.Uniform) : BaseEditor(document, uniform) {
    private val spiner = JSpinner(SpinnerNumberModel(0.0, -100.0, 100.0, 0.1))

    init {
        add(spiner, BorderLayout.CENTER)
        spiner.value = data?.toFloatOrNull() ?: 0.0

        spiner.addChangeListener {
            data = spiner.value.asFloat().toString()
            shaderEditViewer.repaint()
            println("Changed to ${spiner.value as Double}")
        }
    }

    override fun apply(engine: Engine, shader: Shader) {
        shader.uniform(uniform.name, spiner.value.asFloat())
    }
}