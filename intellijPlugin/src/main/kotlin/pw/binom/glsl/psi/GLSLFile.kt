package pw.binom.glsl.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import pw.binom.material.MaterialFileType
import pw.binom.material.MaterialLanguage
import javax.swing.Icon

class GLSLFile(viewProvider: FileViewProvider): PsiFileBase(viewProvider, MaterialLanguage){
    override fun getFileType(): FileType = MaterialFileType

    override fun getIcon(flags: Int): Icon? {
        return super.getIcon(flags)
    }

    override fun toString(): String = "GLSL File"
}