package pw.binom.extensions

import mogot.Behaviour
import mogot.annotations.Property
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.*
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.psi.JavaPsiFacade
import org.jetbrains.kotlin.com.intellij.psi.PsiClass
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.psi.PsiReference
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.referencedProperty
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getSuperNames
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCallWithAssert
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.resolve.jvm.diagnostics.OtherOrigin
import org.jetbrains.kotlin.resolve.jvm.jvmSignature.JvmMethodSignature
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.asFlexibleType
import org.jetbrains.kotlin.types.asSimpleType
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.org.objectweb.asm.Type

private const val PREFIX = "BinomComponentRegistrar: "

private lateinit var messageCollector: MessageCollector

object Log {
    fun log(text: String) {
        messageCollector.report(CompilerMessageSeverity.WARNING, text)
    }
}

class BinomComponentRegistrar : ComponentRegistrar {
    init {
        println("$PREFIX Create Registrator")
    }

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val exampleValue = configuration.get(MogotConfigurationKeys.ASSERTS)
        messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        messageCollector.report(CompilerMessageSeverity.WARNING, "BINOM: Project component registration: $exampleValue")
        registerExtensions(project)
    }

    companion object {
        fun registerExtensions(project: MockProject) {
            ExpressionCodegenExtension.registerExtension(project, MogotExpressionCodegenExtension())
            SyntheticResolveExtension.registerExtension(project, SSS())
            IrGenerationExtension.registerExtension(project, SerializationLoweringExtension())
            StorageComponentContainerContributor.registerExtension(project, MogotStorageComponentContainerContributor())
//            val allScope = GlobalSearchScope.allScope(project)
//            val behaviourClass = JavaPsiFacade.getInstance(project)
//                    .findClass(Behaviour::class.java.name, allScope)
//            Log.log("->${behaviourClass}")
        }
    }
}

fun ClassDescriptor.getSuperClassNotAny(): ClassDescriptor? {
    for (supertype in defaultType.constructor.supertypes) {
        if (!KotlinBuiltIns.isAnyOrNullableAny(supertype)) {
            val superClassifier = supertype.constructor.declarationDescriptor
            if (DescriptorUtils.isClassOrEnumClass(superClassifier)) {
                return superClassifier as ClassDescriptor
            }
        }
    }
    return null
}

class MogotExpressionCodegenExtension : ExpressionCodegenExtension {
    override val shouldGenerateClassSyntheticPartsInLightClassesMode: Boolean
        get() = false

    override fun applyFunction(receiver: StackValue, resolvedCall: ResolvedCall<*>, c: ExpressionCodegenExtension.Context): StackValue? {
        Log.log("applyFunction")
        return super.applyFunction(receiver, resolvedCall, c)
    }

    override fun applyProperty(receiver: StackValue, resolvedCall: ResolvedCall<*>, c: ExpressionCodegenExtension.Context): StackValue? {
        //            val allScope = GlobalSearchScope.allScope(project)
//            val behaviourClass = JavaPsiFacade.getInstance(project)
//                    .findClass(Behaviour::class.java.name, allScope)
        Log.log("applyProperty")
        return super.applyProperty(receiver, resolvedCall, c)
    }

    private var behaviourClass: PsiClass? = null

    override fun generateClassSyntheticParts(codegen: ImplementationBodyCodegen) {

        if (codegen.descriptor.getSuperClassNotAny()?.fqNameSafe?.asString() != Behaviour::class.java.name)
            return
        Log.log("Name: ${codegen.descriptor}")
        codegen.descriptor.unsubstitutedMemberScope.getContributedDescriptors(DescriptorKindFilter.VARIABLES)
                .mapNotNull { it as? PropertyDescriptor }
                .forEach {
                    val property = it.annotations.findAnnotation(FqName(Property::class.java.name))
                    if (property != null) {
                        Log.log("-->${it.name} ${it.type.asmType(codegen.typeMapper)}")
                    }
                }

        val dest = SimpleFunctionDescriptorImpl.create(
                codegen.descriptor,
                Annotations.create(emptyList()),
                Name.identifier("getField"),
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                codegen.descriptor.source
        )
        codegen.functionCodegen.generateMethod(OtherOrigin(codegen.myClass.psiOrParent, dest), dest, object : FunctionGenerationStrategy.CodegenBased(codegen.state) {
            override fun doGenerateBody(p0: ExpressionCodegen, p1: JvmMethodSignature) {
                p0.v.getstatic("java/lang/System", "out", "Ljava/io/PrintStream;")
                p0.v.load(0, Type.getType(String::class.java))
                p0.v.invokestatic("java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false)
                p0.v.aconst(null)
                p0.v.areturn(Type.getType(mogot.Field::class.java))
            }

        })
        return


        Log.log("generateClassSyntheticParts")
        val clazz = codegen.myClass as? KtClass

        if (clazz != null) {
            Log.log("Class ${codegen.descriptor.name.asString()} -> ${codegen.descriptor.getSuperClassNotAny()?.fqNameSafe?.asString()}")

            val psiManager = PsiManager.getInstance(clazz.project)
            if (behaviourClass == null) {
                val allScope = GlobalSearchScope.allScope(clazz.project)
                behaviourClass = JavaPsiFacade.getInstance(clazz.project)
                        .findClass(Behaviour::class.java.name, allScope)
            }
            Log.log("behaviourClass=$behaviourClass")
            Log.log("Class name: ${clazz.fqName} ${clazz.getSuperNames()}")
/*
            Log.log("Imports:")
            clazz.containingKtFile.importList?.imports?.forEach {
                val ref = it.importedReference as? KtDotQualifiedExpression
                val ref2 = it.importedReference as? KtNameReferenceExpression
                Log.log("alias->${it.aliasName}  [${ref?.reference}] [${ref2?.reference}] ${it.importPath}")
            }
*/
//            clazz.getSuperTypeList()
            val superTypes = clazz.getSuperTypeList()?.entries?.map { it.typeAsUserType?.referencedName }
                    ?.filterNotNull()
                    ?.mapNotNull {
                        clazz.containingKtFile.findImportByAlias(it)
                    }
//                    ?.map {
//                        it.importedReference
//                    }
                    ?.filterNotNull()
                    ?.map {
                        "$it -> ${it::class.java.name}"
                    }


            val properties = clazz.getProperties()

            clazz.body?.properties?.forEach {

                val annons = it.annotationEntries.map {
                    val ref = it.typeReference as? KtTypeReference
                    val userType = ref?.typeElement as? KtUserType
                    val ss = userType?.references?.map { it.resolve() }
                    ss
                }
                Log.log("Field: ${clazz.fqName}->${it.name} [$annons]")
            }
        }
        super.generateClassSyntheticParts(codegen)
    }
}

class SSS : SyntheticResolveExtension {
    init {
        Log.log("$PREFIX Create SSS")
    }


}

class SerializationLoweringExtension : IrGenerationExtension {

    init {
        Log.log("$PREFIX SerializationLoweringExtension")
    }


    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        Log.log("$PREFIX moduleFragment.files--->")
        moduleFragment.files.forEach {
            Log.log("File: $it")
        }
        Log.log("List is done!")
    }
}

class MogotStorageComponentContainerContributor : StorageComponentContainerContributor {
    override fun registerModuleComponents(container: StorageComponentContainer, platform: TargetPlatform, moduleDescriptor: ModuleDescriptor) {
//        container.useInstance(CliNoArgDeclarationChecker())
        Log.log("MogotStorageComponentContainerContributor.registerModuleComponents ${moduleDescriptor}")
        super.registerModuleComponents(container, platform, moduleDescriptor)
    }
}