package pw.binom.material

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import pw.binom.ModuleHolder
import pw.binom.Starter
import pw.binom.material.compiler.*
import pw.binom.material.lex.Parser
import pw.binom.material.lex.Type
import java.io.StringReader
import java.util.*
import kotlin.collections.HashSet
import com.intellij.openapi.module.Module as IdeModule

class IdeModuleResolver(val ideModule: IdeModule, val holder: ModuleHolder.Holder) : ModuleResolver {

    class ExternalModule(val holder: ModuleHolder.Holder, val sourceFile: VirtualFile) : Module {
        private var document: Document? = null

        private var psiResolved = false

        fun resolvePsi() {
            synchronized(this) {
                if (psiResolved)
                    return
                println("Try to resolve...")
                resolve(PsiManager.getInstance(holder.module.project).findFile(sourceFile)!!, compile())
                psiResolved = true
                println("Resolved")
            }
        }

        init {
            println("Start read thread $sourceFile")
            Starter.application.runReadAction {
                val doc = FileDocumentManager.getInstance().getDocument(sourceFile)!!
                println("Try to get document $sourceFile")
                doc.addDocumentListener(object : DocumentListener {
                    override fun documentChanged(event: DocumentEvent) {
                        psiResolved = false
                        lastException = null
                        lastModule = null
                    }
                })
                document = doc
                println("Document ready! $sourceFile")
            }
        }

        private var lastModule: SourceModule? = null
        private var lastException: Throwable? = null

        @Synchronized
        private fun compile(): SourceModule {
            if (lastException != null)
                throw lastException!!
            if (lastModule != null) {
                return lastModule!!
            }
            try {
                val sourceModule = SourceModule(holder.getRelativePath(sourceFile))

                val source = document?.text ?: sourceFile.inputStream.bufferedReader().use {
                    it.readText()
                }
                val parser = Parser(sourceModule, StringReader(source))
                Compiler(parser, sourceModule, holder.resolver)


                lastModule = sourceModule
                return sourceModule
            } catch (e: Throwable) {
                lastException = e
                throw e
            }
        }

        fun checkValid() =
                compile()

        override val parentScope: Scope?
            get() = compile().parentScope

        override fun findMethod(name: String, args: List<TypeDesc>): MethodDesc? = compile().findMethod(name, args)
        fun findAllMethodsByName(name: String): Set<MethodDesc> {
            val vv = HashSet<MethodDesc>()

            fun search(module: Module) {
                when (module) {
                    RootModule -> {
                        (module as RootModule).globalMethods.forEach {
                            if (it.name == name)
                                vv += it
                        }
                    }
                    is SourceModule -> {
                        module.globalMethods.forEach {
                            if (it.name == name)
                                vv += it
                            module.moduls.forEach {
                                search(it)
                            }
                        }
                    }
                }
            }
            search(RootModule)
            search(compile())
            return vv
        }

        override fun findField(name: String): FieldDesc? = compile().findField(name)
        override fun findType(type: Type): TypeDesc? = compile().findType(type)

        override fun findType(clazz: ClassDesc, array: List<Int>): TypeDesc? = compile().findType(clazz, array)
    }

    interface SourceHolder {
        val compiler: Compiler
    }

    private val modules = WeakHashMap<VirtualFile, ExternalModule>()

    fun getModule(file: VirtualFile) = modules.getOrPut(file) { ExternalModule(holder, file) }.also {
        it.checkValid()
    }

    override fun getModule(currentPath: String, path: String): Module? {
        val vv = ModuleRootManager.getInstance(ideModule)
        val currentFile = holder.findFileByRelativePath(currentPath)
        if (currentFile == null) {
            println("Can't find path to current file $currentFile")
            return null
        }
        val parent = currentFile.parent
        val moduleFile = parent.findFileByRelativePath(path)
        if (moduleFile == null) {
            println("Can't find \"$path\" in \"$parent\"")
            return null
        }
        val mod = getModule(moduleFile)
        println("File $moduleFile founded!")
        return mod
//        val moduleSource = SourceModule()
//        moduleFile.inputStream.bufferedReader().use {
//            val parser = Parser(it)
//            Compiler(parser, holder.getRelativePath(moduleFile), moduleSource, this)
//        }
//        println("Current $currentPath, path: ${holder.getRelativePath(moduleFile)}")
//        return moduleSource
    }
}