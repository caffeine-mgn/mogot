package pw.binom.glsl.psi

import com.intellij.psi.tree.IElementType
import pw.binom.ShaderLanguage

class GLSLTokenType(debugName: String) : IElementType(debugName, ShaderLanguage) {
    override fun toString(): String {
        return "GLSLTokenType.${super.toString()}"
    }
}