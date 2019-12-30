package pw.binom.facet

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.components.PersistentStateComponent

class AssertsFacetConfiguration : FacetConfiguration, PersistentStateComponent<AssertsFacetState> {
    private var state = AssertsFacetState()
    override fun createEditorTabs(editorContext: FacetEditorContext?, validatorsManager: FacetValidatorsManager?): Array<FacetEditorTab> {
        return emptyArray()
    }

    override fun getState(): AssertsFacetState? = state

    override fun loadState(state: AssertsFacetState) {
        this.state = state
    }

}