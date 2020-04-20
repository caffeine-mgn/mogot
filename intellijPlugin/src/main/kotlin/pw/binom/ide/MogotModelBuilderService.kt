package pw.binom.ide

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.util.Key
import org.gradle.api.Project
import org.gradle.tooling.model.idea.IdeaModule
import org.jetbrains.kotlin.annotation.plugin.ide.*
import org.jetbrains.kotlin.gradle.AbstractKotlinGradleModelBuilder
import org.jetbrains.kotlin.idea.configuration.GradleProjectImportHandler
import org.jetbrains.kotlin.idea.facet.KotlinFacet
import org.jetbrains.plugins.gradle.model.data.GradleSourceSetData
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.tooling.ErrorMessageBuilder
import org.jetbrains.plugins.gradle.util.GradleUtil
import java.io.File
import java.io.Serializable
import java.lang.Exception

class Model : Serializable {
    var assets: File? = null

    constructor(assets: File) {
        this.assets = assets
    }

    override fun toString(): String {
        return "Model(assets=$assets)"
    }


}

class MogotModelBuilderService : AbstractKotlinGradleModelBuilder() {
    override fun buildAll(modelName: String?, project: Project?): Any {
        System.exit(0)
        println("MogotModelBuilderService -> ModelName")
        return Model(File("Hello!"))
    }

    override fun canBuild(modelName: String?): Boolean {
        return false
    }

    override fun getErrorMessageBuilder(project: Project, e: Exception): ErrorMessageBuilder {
        return ErrorMessageBuilder.create(project, "mogot")
    }
}

class MogotGradleProjectImportHandler : GradleProjectImportHandler {
    override fun importByModule(facet: KotlinFacet, moduleNode: DataNode<ModuleData>) {
        println("MogotGradleProjectImportHandler->importByModule")
    }

    override fun importBySourceSet(facet: KotlinFacet, sourceSetNode: DataNode<GradleSourceSetData>) {
        facet.configuration.settings.targetPlatform?.forEach {
            println("BuildFor: ${it.platformName}")
        }
        println("User Data: ${facet.userDataString}")
        println("MogotGradleProjectImportHandler->importBySourceSet")
    }
}

class MogotProjectResolverExtension : AbstractProjectResolverExtension() {
    override fun populateModuleExtraModels(gradleModule: IdeaModule, ideModule: DataNode<ModuleData>) {
        val model = resolverCtx.getExtraProject(gradleModule, Model::class.java)
        println("Model: $model")
    }
}