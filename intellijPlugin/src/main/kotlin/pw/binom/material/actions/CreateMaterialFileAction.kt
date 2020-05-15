package pw.binom.material.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import pw.binom.material.MaterialFileType
import pw.binom.sceneEditor.SceneFileType

const val TEMPLATE_MATERIAL = "MaterialTemplate"
const val TEMPLATE_MATERIAL_MODULE = "MaterialModuleTemplate"

private const val TEMPILE_TEXT = """@vertex
vec3 vertexPos

@normal
vec3 normalList

@uv
vec2 vertexUV

@projection
mat4 projection

@model
mat4 model
vec3 normal


class Light {
    vec3 position
    vec3 diffuse
    float specular
}

@property(hidden=true)
Light lights[10]

@property(hidden=true)
int lights_len

vec4 vertex(){
    mat3 normalMatrix = mat3(transpose(inverse(model)))
    normal = vec3(normalMatrix * normalList)
    return vec4(projection * model * vec4(vertexPos, 1f))
}

vec4 fragment(vec4 color2){
    return vec4(1f, 1f, 1f, 1f)
}
"""

class CreateMaterialFileAction : CreateFileFromTemplateAction("Material", "Create new Material file", SceneFileType.icon) {
    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Material"
    }

    override fun buildDialog(project: Project?, directory: PsiDirectory?, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("Create new Material file")
        builder.addKind("Material", MaterialFileType.icon, TEMPLATE_MATERIAL)
        builder.addKind("Module", MaterialFileType.icon, TEMPLATE_MATERIAL_MODULE)
    }

    override fun createFile(name: String, templateName: String, dir: PsiDirectory): PsiFile? {
        val module = when (templateName) {
            TEMPLATE_MATERIAL -> false
            TEMPLATE_MATERIAL_MODULE -> true
            else -> return null
        }
        val ext = if (module) "shr" else "mat"
        val body = if (module) "" else TEMPILE_TEXT
        val file = PsiFileFactory.getInstance(dir.project).createFileFromText("$name.$ext", SceneFileType, body)
        dir.add(file)
        val virtualFile = file.virtualFile
        if (virtualFile != null)
            FileEditorManager.getInstance(dir.project).openFile(virtualFile, true)
        else
            if (file.canNavigate())
                file.navigate(true)
        return file
    }

}