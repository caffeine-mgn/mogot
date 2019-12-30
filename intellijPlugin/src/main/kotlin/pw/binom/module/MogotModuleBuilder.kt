package pw.binom.module

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel

class MogotModuleBuilder: ModuleBuilder(){
    override fun getModuleType(): ModuleType<*> = MogotModuleType.instance

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        super.setupRootModel(modifiableRootModel)
    }
}