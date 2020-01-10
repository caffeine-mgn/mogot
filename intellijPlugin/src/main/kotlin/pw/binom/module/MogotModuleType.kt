package pw.binom.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private const val MODULE_ID = "MOGOT_MODULE"

class MogotModuleType : ModuleType<MogotModuleBuilder>(MODULE_ID) {
    val ICON = IconLoader.getIcon("/fbx.png")

    companion object {
        val instance
            get() = ModuleTypeManager.getInstance().findByID(MODULE_ID) as MogotModuleType
    }

    override fun createModuleBuilder(): MogotModuleBuilder = MogotModuleBuilder()

    override fun getName(): String = "Mogot Module Type"

    override fun getDescription(): String = "Mogot Engine Module"

    override fun getNodeIcon(isOpened: Boolean): Icon = ICON

}