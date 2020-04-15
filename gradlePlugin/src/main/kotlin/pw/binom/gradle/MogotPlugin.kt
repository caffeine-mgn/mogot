package pw.binom.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry
import javax.inject.Inject

open class MogotPlugin /*@Inject internal constructor*/(/*private val registry: ToolingModelBuilderRegistry*/) : Plugin<Project> {
    companion object {
        fun isEnabled(project: Project) = project.plugins.findPlugin(MogotPlugin::class.java) != null
        fun getNoArgExtension(project: Project): MogotExtension? =
                project.extensions.getByType(MogotExtension::class.java)
    }

    override fun apply(target: Project) {
        target.extensions.create("mogot", MogotExtension::class.java)
    }

}