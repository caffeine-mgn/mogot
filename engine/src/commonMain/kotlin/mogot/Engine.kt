package mogot

expect class Engine {
    constructor(stage: Stage)
}

actual class Engine actual constructor(val stage: Stage) {
    val gl
        get() = stage.gl
}