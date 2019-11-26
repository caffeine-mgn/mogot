package mogot

actual class SourceImage(engine: Engine, val png: PNGDecoder) : Resource(engine) {
    override fun dispose() {
    }
}