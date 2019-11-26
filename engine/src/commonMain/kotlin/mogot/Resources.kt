package mogot

expect class Resources internal constructor(engine: Engine) {
    fun createTexture2D(path: String): Texture2D
}