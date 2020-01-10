package pw.binom.fbx

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.PsiManagerImpl
import pw.binom.FbxLanguage
import pw.binom.FlxFileType

class FbxFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean): FileViewProvider =
            FbxFileViewProvider(manager, file, eventSystemEnabled)
}


class FbxFileViewProvider(manager: PsiManager,
                          virtualFile: VirtualFile,
                          eventSystemEnabled: Boolean) : SingleRootFileViewProvider(manager, virtualFile, eventSystemEnabled, FlxFileType) {
    override fun createFile(project: Project, file: VirtualFile, fileType: FileType): PsiFile? =
            FbxPsiFile(this, manager as PsiManagerImpl, file)

    init {
        println("Create FbxFileViewProvider")
    }

    override fun getLanguages(): MutableSet<Language> =
            mutableSetOf(FbxLanguage)
}