package pw.binom.gradle

import java.io.File

open class MogotExtension {
    var assets: File? = null
    fun assets(file: File) {
        this.assets = file
    }
}