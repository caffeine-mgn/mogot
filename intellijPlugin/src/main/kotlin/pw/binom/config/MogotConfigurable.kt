package pw.binom.config

import com.intellij.openapi.options.ConfigurableBase

class MogotConfigurable : ConfigurableBase<GrazieSettingsPanel, GrazieConfig>("reference.settingsdialog.project.grazie",
        GraziePlugin.name, null) {
    private lateinit var ui: GrazieSettingsPanel

    override fun getSettings(): GrazieConfig = ServiceManager.getService(GrazieConfig::class.java)

    override fun createUi(): GrazieSettingsPanel = GrazieSettingsPanel().also { ui = it }

    override fun enableSearch(option: String?) = ui.showOption(option)
}