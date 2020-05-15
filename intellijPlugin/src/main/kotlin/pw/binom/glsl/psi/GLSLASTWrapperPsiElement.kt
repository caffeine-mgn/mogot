package pw.binom.glsl.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import pw.binom.ModuleHolder

abstract class GLSLASTWrapperPsiElement(node: ASTNode) : ASTWrapperPsiElement(node) {
    var internalReference: Array<PsiReference>? = null

    override fun getReference(): PsiReference? {
        this.containingFile.virtualFile
        if (this is GLSLCallMethodExp) {
            val holder = ModuleHolder.getInstance(this.getContainingFile().virtualFile, getProject())
            holder.resolver.getModule(this.getContainingFile().virtualFile).resolvePsi()
        }
        return internalReference?.get(0)
    }

    override fun getReferences(): Array<PsiReference> {
        if (this is GLSLCallMethodExp) {
            val holder = ModuleHolder.getInstance(this.getContainingFile().virtualFile, getProject())
            holder.resolver.getModule(this.getContainingFile().virtualFile).resolvePsi()
        }
        return internalReference ?: super.getReferences()
    }
}