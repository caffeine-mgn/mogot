package pw.binom.facet

import com.intellij.facet.Facet
import com.intellij.facet.FacetType

class AssertsFacet(facetType: FacetType<*, *>,
                   module: com.intellij.openapi.module.Module,
                   name: String,
                   configuration: AssertsFacetConfiguration,
                   underlyingFacet: Facet<*>?) : Facet<AssertsFacetConfiguration>(facetType, module, name, configuration, underlyingFacet) {

    override fun initFacet() {
        super.initFacet()
    }
}