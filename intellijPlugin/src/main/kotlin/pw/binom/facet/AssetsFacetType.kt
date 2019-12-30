package pw.binom.facet

import com.intellij.facet.Facet
import com.intellij.facet.FacetType
import com.intellij.facet.FacetTypeId
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType

private const val FACET_ID = "MOGOT_ASSERTS_FACET"
private const val FACET_NAME = "Asserts Facet"
val DEMO_FACET_TYPE_ID: FacetTypeId<AssertsFacet> = FacetTypeId(FACET_ID)

class AssetsFacetType : FacetType<AssertsFacet, AssertsFacetConfiguration>(DEMO_FACET_TYPE_ID, FACET_ID, FACET_NAME) {
    override fun createFacet(module: Module, name: String, configuration: AssertsFacetConfiguration, underlyingFacet: Facet<*>?): AssertsFacet =
            AssertsFacet(this, module, name, configuration, underlyingFacet)

    override fun createDefaultConfiguration(): AssertsFacetConfiguration =
            AssertsFacetConfiguration()

    override fun isSuitableModuleType(moduleType: ModuleType<*>?): Boolean {
        println("moduleType=${moduleType}")
        return true
    }

}