package pw.binom.fbx

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.lang.FileASTNode
import com.intellij.lang.Language
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.source.resolve.FileContextUtil
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.search.SearchScope
import pw.binom.FbxLanguage
import pw.binom.FlxFileType
import javax.swing.Icon

class FbxPsiFile(val fileViewProvider: FileViewProvider, val psiManagerImpl: PsiManagerImpl, val file: VirtualFile) :
//PsiBinaryFileImpl(psiManagerImpl, fileViewProvider)
        PsiFile {
    override fun canNavigate(): Boolean = PsiNavigationSupport.getInstance().canNavigate(this)

    override fun isEquivalentTo(another: PsiElement?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canNavigateToSource(): Boolean = canNavigate()

    override fun addBefore(element: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun copy(): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getText(): String = ""

    override fun isDirectory(): Boolean = false
    override fun addAfter(element: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement?, place: PsiElement): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStartOffsetInParent(): Int = 0
    override fun getContainingDirectory(): PsiDirectory? {
        val file = viewProvider.virtualFile
        val parentFile = file.parent ?: return null
        if (!parentFile.isValid) {
            return null
        }
        return manager.findDirectory(parentFile)!!
    }

    override fun getPrevSibling(): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> putUserData(key: Key<T>, value: T?) {

    }

    override fun replace(newElement: PsiElement): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContainingFile(): PsiFile = this

    override fun getViewProvider(): FileViewProvider = fileViewProvider

    override fun getReferences(): Array<PsiReference> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkAdd(element: PsiElement) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLanguage(): Language = FbxLanguage
    override fun addRangeAfter(first: PsiElement?, last: PsiElement?, anchor: PsiElement?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUseScope(): SearchScope {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getResolveScope(): GlobalSearchScope {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProject(): Project = psiManagerImpl.project

    override fun addRange(first: PsiElement?, last: PsiElement?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getContext(): PsiElement? = FileContextUtil.getFileContext(this)

    override fun processChildren(processor: PsiElementProcessor<PsiFileSystemItem>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun accept(visitor: PsiElementVisitor) {
        println("FbxPsiFile.accept")
    }

    override fun getNextSibling(): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFirstChild(): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getName(): String = file.name
    override fun getTextLength(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPsiRoots(): Array<PsiFile> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun textMatches(text: CharSequence): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun textMatches(element: PsiElement): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTextOffset(): Int = 0

    override fun textToCharArray(): CharArray = CharArray(0)
    override fun add(element: PsiElement): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement?): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPhysical(): Boolean = true

    override fun findReferenceAt(offset: Int): PsiReference? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNode(): FileASTNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getManager(): PsiManager = psiManagerImpl

    override fun isValid(): Boolean = true

    override fun delete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIcon(flags: Int): Icon = FbxLanguage.ICON

    override fun deleteChildRange(first: PsiElement?, last: PsiElement?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getParent(): PsiDirectory? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getModificationStamp(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTextRange(): TextRange {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> putCopyableUserData(key: Key<T>?, value: T?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOriginalElement(): PsiElement = this

    override fun checkDelete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNavigationElement(): PsiElement = this

    override fun getFileType(): FileType = FlxFileType

    override fun subtreeChanged() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isWritable(): Boolean = false

    override fun checkSetName(name: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> getUserData(key: Key<T>): T? = null

    override fun navigate(requestFocus: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLastChild(): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setName(name: String): PsiElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOriginalFile(): PsiFile = this

    override fun textContains(c: Char): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getVirtualFile(): VirtualFile = file

    override fun <T : Any?> getCopyableUserData(key: Key<T>?): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPresentation(): ItemPresentation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findElementAt(offset: Int): PsiElement? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getReference(): PsiReference? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChildren(): Array<PsiElement> {
        println("FbxPsiFile.getChildren")
        return TODO()
    }

    override fun acceptChildren(visitor: PsiElementVisitor) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    init {
        println("Create FbxPsiFile")
    }
}