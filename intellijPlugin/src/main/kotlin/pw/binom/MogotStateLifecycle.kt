package pw.binom

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import pw.binom.config.MogotConfig

interface MogotStateLifecycle {
    companion object {
        val topic = Topic.create("mogot_state_lifecycle_topic", MogotStateLifecycle::class.java)
        val publisher by lazy { ApplicationManager.getApplication().messageBus.syncPublisher(topic) }
    }

    fun update(config: MogotConfig.State) {

    }
}