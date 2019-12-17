package pw.binom


import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import kotlin.math.roundToInt
import kotlin.reflect.KProperty


class FlexLayout(val componentPlace: Container, direction: Direction = Direction.ROW, justifyContent: JustifyContent = JustifyContent.START) : LayoutManager {

    init {
        componentPlace.layout = this
    }

    class BorderParams(var top: Int = 0, var bottom: Int = 0, var left: Int = 0, var right: Int = 0)

    private var container by ThreadLocal<Container>()

    class Setting(var basis: Int? = null, var grow: Float? = null, val padding: BorderParams = BorderParams(), val margin: BorderParams = BorderParams())

    private val settings = HashMap<Component, Setting>()

    fun <T : Component> settingFor(component: T): Setting = settings.getOrPut(component) { Setting() }

    fun setBasis(component: Component, basis: Int?) {
        settingFor(component).basis = basis
    }

    fun setPadding(component: Component, padding: BorderParams) {
        settingFor(component).padding.also {
            it.top = padding.top
            it.right = padding.right
            it.bottom = padding.bottom
            it.left = padding.left
        }
    }

    fun setGrow(component: Component, grow: Float?) {
        settingFor(component).grow = grow
    }

    private class Coord(val component: Component, val value: Int, val length: Int)
    private class Data(
            val component: Component,
            val preferredSize: Int,
            val setting: Setting?,
            val paddingStart: Int,
            val marginStart: Int,
            val paddingEnd: Int,
            val marginEnd: Int
    )

    private fun calcAxis(components: List<Data>, size: Int): List<Coord> {
        val minSize = components.sumBy {
            it.setting?.basis ?: it.preferredSize
        } + componentPadding * components.size

        var calcedSize = emptyMap<Data, Int>()
        if (size - minSize > 0) {
            val constSize = components.filter { it.setting?.grow ?: 0 == 0 }.sumBy {
                it.setting?.basis ?: it.preferredSize
            }
            val growComponents = components.filter { it.setting?.grow ?: 0f > 0f }
            val forGrow = size - constSize
            val growPersent = forGrow / growComponents.sumByFloat { it.setting!!.grow!! }

            calcedSize = growComponents.associate { it to (it.setting!!.grow!! * growPersent).roundToInt() }
        }

        if (justifyContent == JustifyContent.START) {
            var currentY = 0
            return components.map {
                val blockSize = calcedSize[it] ?: (it.setting?.basis ?: it.preferredSize)
                val c = Coord(component = it.component, value = currentY + it.paddingStart + it.marginStart, length = blockSize - it.paddingStart - it.paddingEnd + it.marginStart + it.marginEnd)
                currentY += componentPadding
                currentY += blockSize
                c
            }
        }

        if (justifyContent == JustifyContent.END) {
            var currentX = 0
            return components.reversed().map {
                var blockSize = calcedSize[it] ?: (it.setting?.basis ?: it.preferredSize)
                val c = Coord(component = it.component, value = size - currentX - blockSize + it.paddingStart + it.marginStart, length = blockSize - it.paddingStart - it.paddingEnd + it.marginStart + it.marginEnd)
                currentX += componentPadding
                currentX += blockSize
                c
            }
        }
        TODO()
    }

    private fun calcCrossAxis(components: List<Data>, size: Int): List<Coord> {
        return components.map {
            Coord(component = it.component, value = it.paddingStart + it.marginStart, length = size - it.paddingStart - it.paddingEnd - it.marginStart - it.marginEnd)
        }
    }

    fun getSize(component: Component) = settings[component]?.basis

    var direction = direction
        set(value) {
            field = value
            container?.repaint()
        }
    var justifyContent = justifyContent
        set(value) {
            field = value
            container?.repaint()
        }
//    var crossStart = 0
//        set(value) {
//            field = value
//            container?.repaint()
//        }
//    var crossEnd = 0
//        set(value) {
//            field = value
//            container?.repaint()
//        }
//    var mainStart = 0
//        set(value) {
//            field = value
//            container?.repaint()
//        }
//    var mainEnd = 0
//        set(value) {
//            field = value
//            container?.repaint()
//        }

    enum class Direction {
        ROW,
        COLUMN
    }

    enum class JustifyContent {
        START,
        END,
        CENTER,
        SPACEBETWEEN,
        SPACEAROUND
    }

    // Следующие два метода не используются
    override fun addLayoutComponent(name: String, comp: Component) {}

    override fun removeLayoutComponent(comp: Component) {}

    // Метод определения минимального размера для контейнера
    override fun minimumLayoutSize(c: Container): Dimension {
        return calculateBestSize(c)
    }

    // Метод определения предпочтительного размера для контейнера
    override fun preferredLayoutSize(c: Container): Dimension {
        return calculateBestSize(c)
    }

    var componentPadding = 0
    // Метод расположения компонентов в контейнере
    override fun layoutContainer(c: Container) {
        container = c
        when (direction) {
            Direction.ROW -> {
                val mainAxisData = container.components.map {
                    Data(
                            component = it,
                            preferredSize = it.preferredSize.width,
                            setting = settings[it],
                            paddingStart = settings[it]?.padding?.left ?: 0,
                            paddingEnd = settings[it]?.padding?.right ?: 0,
                            marginStart = settings[it]?.margin?.left ?: 0,
                            marginEnd = settings[it]?.margin?.right ?: 0)
                }
                val crossAxisData = container.components.map {
                    Data(
                            component = it,
                            preferredSize = it.preferredSize.height,
                            setting = settings[it],
                            paddingStart = settings[it]?.padding?.top ?: 0,
                            paddingEnd = settings[it]?.padding?.bottom ?: 0,
                            marginStart = settings[it]?.margin?.top ?: 0,
                            marginEnd = settings[it]?.margin?.bottom ?: 0)
                }
                val mainAxis = calcAxis(mainAxisData, container.width)
                val crossAxis = calcCrossAxis(crossAxisData, container.height)

                container.components.forEach { com ->
                    val main = mainAxis.find { it.component == com }!!
                    val cross = crossAxis.find { it.component == com }!!
                    com.setBounds(main.value, cross.value, main.length, cross.length)
                }
            }
            Direction.COLUMN -> {
                val mainAxisData = container.components.map {
                    Data(
                            component = it,
                            preferredSize = it.preferredSize.height,
                            setting = settings[it],
                            paddingStart = settings[it]?.padding?.top ?: 0,
                            paddingEnd = settings[it]?.padding?.bottom ?: 0,
                            marginStart = settings[it]?.margin?.top ?: 0,
                            marginEnd = settings[it]?.margin?.bottom ?: 0)
                }
                val crossAxisData = container.components.map {
                    Data(
                            component = it,
                            preferredSize = it.preferredSize.width,
                            setting = settings[it],
                            paddingStart = settings[it]?.padding?.left ?: 0,
                            paddingEnd = settings[it]?.padding?.right ?: 0,
                            marginStart = settings[it]?.margin?.left ?: 0,
                            marginEnd = settings[it]?.margin?.right ?: 0)
                }
                val mainAxis = calcAxis(mainAxisData, container.height)
                val crossAxis = calcCrossAxis(crossAxisData, container.width)
                container.components.forEach { com ->
                    val main = mainAxis.find { it.component == com }!!
                    val cross = crossAxis.find { it.component == com }!!
                    com.setBounds(cross.value, main.value, cross.length, main.length)
                }
            }
        }
    }

    // Метод вычисления оптимального размера контейнера
    private fun calculateBestSize(c: Container): Dimension {
        container = c
        if (direction == Direction.COLUMN) {
            return Dimension(
                    (c.components.map {
                        val setting = settings[it]
                        val margin = (setting?.margin?.left ?: 0) + (setting?.margin?.right ?: 0)
                        it.preferredSize.width + margin
                    }.max() ?: 0),
                    c.components.map {
                        val setting = settings[it]
                        val margin = (setting?.margin?.top ?: 0) + (setting?.margin?.bottom ?: 0)
                        it.preferredSize.height + componentPadding + margin
                    }.sum()
            )
        }
        if (direction == Direction.ROW) {
            return Dimension(
                    c.components.map {
                        val setting = settings[it]
                        val margin = (setting?.margin?.left ?: 0) + (setting?.margin?.right ?: 0)
                        it.preferredSize.width + componentPadding + margin
                    }.sum(),
                    (c.components.map {
                        val setting = settings[it]
                        val margin = (setting?.margin?.top ?: 0) + (setting?.margin?.bottom ?: 0)
                        it.preferredSize.height + margin
                    }.max() ?: 0)
            )
        }

        TODO()
    }

    fun remove(component: Component) {
        componentPlace.remove(component)
        settings.remove(component)
    }
}

fun <T : Component> T.appendTo(layout: FlexLayout, f: (FlexLayout.Setting.() -> Unit)? = null): T {
    if (f != null) {
        layout.settingFor(this).f()
    }
    layout.componentPlace.add(this)
    return this
}

private operator fun <T : Any?> ThreadLocal<T>.setValue(self: Any, property: KProperty<*>, value: T) {
    set(value)
}

private operator fun <T : Any?> ThreadLocal<T>.getValue(self: Any, property: KProperty<*>): T = get()

private fun <T> Collection<T>.sumByFloat(func: (T) -> Float): Float {
    var out = 0f
    forEach { out += func(it) }
    return out
}