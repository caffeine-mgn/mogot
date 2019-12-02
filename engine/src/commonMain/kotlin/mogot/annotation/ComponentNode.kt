package mogot.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ComponentNode(
        val displayName: String,
        val description: String
)