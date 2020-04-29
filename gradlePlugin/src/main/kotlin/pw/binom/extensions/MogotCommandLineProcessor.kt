package pw.binom.extensions

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import pw.binom.gradle.BinomGradleSubplugin

object MogotConfigurationKeys {
    val ASSERTS: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("asserts path")
}

class MogotCommandLineProcessor : CommandLineProcessor {

    val ASSERTS_OPTION = CliOption(BinomGradleSubplugin.ASSERT_PATH, "<path>", "Path to Assets",
            required = true, allowMultipleOccurrences = true)

    init {
        println("init MogotCommandLineProcessor")
    }

    override val pluginId: String
        get() = BinomGradleSubplugin.PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption>
        get() = listOf(ASSERTS_OPTION)

    override fun processOption(option: AbstractCliOption, value: String, configuration: CompilerConfiguration) {
        when (option) {
            ASSERTS_OPTION -> configuration.appendList(MogotConfigurationKeys.ASSERTS, value)
        }
    }
}