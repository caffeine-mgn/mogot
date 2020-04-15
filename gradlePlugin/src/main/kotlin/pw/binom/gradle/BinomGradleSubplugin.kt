package pw.binom.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.*

private const val PREFIX = "BinomGradleSubplugin: "

class BinomGradleSubplugin : KotlinGradleSubplugin<AbstractCompile> {

    companion object {
        const val ASSERT_PATH = "asserts"
        const val PLUGIN_ID = "pw.binom.mogot.gradle-plugin"
    }

    init {
        println("Init subplugin!")
    }

    override fun apply(project: Project, kotlinCompile: AbstractCompile, javaCompile: AbstractCompile?, variantData: Any?, androidProjectHandler: Any?, kotlinCompilation: KotlinCompilation<KotlinCommonOptions>?): List<SubpluginOption> {
        println("${MogotPlugin.isEnabled(project)} for ${project}")
        if (!MogotPlugin.isEnabled(project))
            return emptyList()
        val mogotExtension = project.extensions.findByType(MogotExtension::class.java) ?: return emptyList()
        println("Mogot Asserts Path: ${mogotExtension.assets?.absolutePath}")
        return listOf(SubpluginOption(ASSERT_PATH, mogotExtension.assets?.absolutePath ?: ""))
    }

    override fun getCompilerPluginId(): String = PLUGIN_ID

    private val artifact = SubpluginArtifact("pw.binom.mogot", "gradle-plugin", "0.1")

    override fun getPluginArtifact(): SubpluginArtifact = artifact

    override fun isApplicable(project: Project, task: AbstractCompile): Boolean = true
}