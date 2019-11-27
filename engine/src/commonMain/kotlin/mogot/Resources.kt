package mogot

expect class Resources internal constructor(engine: Engine) {
    suspend fun createTexture2D(path: String): Texture2D
    fun createEmptyTexture2D(): Texture2D
}