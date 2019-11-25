package mogot

class Engine constructor(val stage: Stage) {
    val gl
        get() = stage.gl
    val resources = Resources(this)
}