package pw.binom.glsl.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import pw.binom.ShaderFileType
import pw.binom.ShaderLanguage
import javax.swing.Icon

class GLSLFile(viewProvider: FileViewProvider): PsiFileBase(viewProvider,ShaderLanguage){
    override fun getFileType(): FileType = ShaderFileType

    override fun getIcon(flags: Int): Icon? {
        return super.getIcon(flags)
    }

    override fun toString(): String = "GLSL File"
}