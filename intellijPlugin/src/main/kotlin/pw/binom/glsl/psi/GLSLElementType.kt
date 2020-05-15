package pw.binom.glsl.psi

import com.intellij.psi.tree.IElementType
import pw.binom.material.MaterialLanguage

class GLSLElementType(debugName: String) : IElementType(debugName, MaterialLanguage) {
    override fun toString(): String {
        return "GLSLElementType(${super.toString()})"
    }
}