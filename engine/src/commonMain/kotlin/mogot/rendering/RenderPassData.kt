package mogot.rendering

class RenderPassData{
    companion object{
        const val RENDER_TARGET_TEXTURE = "texture"
        const val MSAA = "msaa"
    }
    val values = HashMap<String,Any>()
}