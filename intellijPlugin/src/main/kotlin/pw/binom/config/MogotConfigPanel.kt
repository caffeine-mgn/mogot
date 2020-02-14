package pw.binom.config

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class MogotConfigPanel : ConfigurableUi<MogotConfig>, Disposable {
    override fun isModified(settings: MogotConfig): Boolean = false

    override fun apply(settings: MogotConfig) {

    }

    override fun reset(settings: MogotConfig) {

    }

    override fun getComponent(): JComponent =
            panel() {

            }

    override fun dispose() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}