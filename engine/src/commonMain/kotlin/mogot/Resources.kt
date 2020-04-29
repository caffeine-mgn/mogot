package mogot

import pw.binom.io.FileSystem

val ANIMATION_MAGIC_BYTES = byteArrayOf(33, 32, 66, 87)

expect class Resources internal constructor(engine: Engine, fileSystem: FileSystem<Unit>) {
    val fileSystem: FileSystem<Unit>
    suspend fun createTexture2D(path: String): Texture2D
    fun createEmptyTexture2D(): Texture2D
    val engine: Engine
}