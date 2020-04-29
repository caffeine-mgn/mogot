package pw.binom.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import pw.binom.DesktopAssertTask

open class MogotPlugin /*@Inject internal constructor*/(/*private val registry: ToolingModelBuilderRegistry*/) : Plugin<Project> {
    companion object {
        fun isEnabled(project: Project) = project.plugins.findPlugin(MogotPlugin::class.java) != null
        fun getMogotExtension(project: Project): MogotExtension? =
                project.extensions.getByType(MogotExtension::class.java)
    }

    override fun apply(target: Project) {
        target.extensions.create("mogot", MogotExtension::class.java)
        val assertTask = target.tasks.create("mogotResources", DesktopAssertTask::class.javaObjectType)

    }

}