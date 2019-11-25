package mogot

abstract class Resource(engine: Engine){
    private var counter:UInt =0u
    fun unlock(){
        counter--
    }

    fun lock(){
        counter++
    }

    internal abstract fun dispose()
}