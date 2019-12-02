package pw.binom.glsl.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import mogot.Engine
import mogot.gl.Shader
import mogot.math.Vector4f
import pw.binom.FlexLayout
import pw.binom.ShaderEditViewer
import pw.binom.appendTo
import java.awt.BorderLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.border.TitledBorder

object Vec4EditorFactory : UniformEditor.EditorFactory {
    override fun create(project: Project, shaderEditViewer: ShaderEditViewer, document: Document, uniform: UniformEditor.Uniform): UniformEditor.Editor? {
        if (uniform.type == "vec4")
            return Vec4Editor(shaderEditViewer, document, uniform)
        return null
    }
}

class Vec4Editor(val shaderEditViewer: ShaderEditViewer, document: Document, uniform: UniformEditor.Uniform) : BaseEditor(document, uniform) {

    private val spinerX = Value("X")
    private val spinerY = Value("Y")
    private val spinerZ = Value("Z")
    private val spinerW = Value("W")

    private class Value(title: String) : JBPanel<ImageEditor>(BorderLayout()) {
        val spiner = JSpinner(SpinnerNumberModel(0.0, -100.0, 100.0, 0.1))

        init {
            border = TitledBorder(title)
            add(spiner, BorderLayout.CENTER)
        }

        var value
            get() = spiner.value.asFloat()
            set(value) {
                spiner.value = value
            }
    }

    private val flex = FlexLayout(this)

    init {
        spinerX.appendTo(flex) {
            margin.top = 15
            margin.bottom = 2
            margin.left = 2
        }
        spinerY.appendTo(flex) {
            this.margin.top = 15
            this.margin.bottom = 2
        }
        spinerZ.appendTo(flex) {
            this.margin.top = 15
            this.margin.bottom = 2
        }
        spinerW.appendTo(flex) {
            this.margin.top = 15
            this.margin.bottom = 2
        }

        data?.split(';')?.also {
            spinerX.value = it[0].toFloatOrNull() ?: 0f
            spinerY.value = it[1].toFloatOrNull() ?: 0f
            spinerZ.value = it[2].toFloatOrNull() ?: 0f
            spinerW.value = it[3].toFloatOrNull() ?: 0f
        }

        spinerX.spiner.addChangeListener { onChange() }
        spinerY.spiner.addChangeListener { onChange() }
        spinerZ.spiner.addChangeListener { onChange() }
        spinerW.spiner.addChangeListener { onChange() }
    }

    private fun onChange() {
        data = "${spinerX.value};${spinerY.value};${spinerZ.value};${spinerW.value}"
        shaderEditViewer.repaint()
    }

    override fun apply(engine: Engine, shader: Shader) {
        shader.uniform(uniform.name, Vector4f(
                spinerX.value,
                spinerY.value,
                spinerZ.value,
                spinerW.value
        ))
    }
}