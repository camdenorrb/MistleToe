package me.camdenorrb.mistletoe.common.gui.struct

import me.camdenorrb.mistletoe.common.gui.base.ElementBase
import me.camdenorrb.mistletoe.common.gui.base.GUIBase
import java.util.concurrent.ConcurrentSkipListSet

abstract class GUIStruct : GUIBase {

    abstract val fps: Int

    // TODO: Layers
    open val elements = ConcurrentSkipListSet<ElementBase>()

    var isInitialized = false
        private set

    var isVisible = false
        private set


    protected open fun onInit() = Unit

    protected open fun onShow() = Unit

    protected open fun onHide() = Unit


    protected abstract fun draw(element: ElementBase)


    final override fun init() {

        check(!isInitialized)

        onInit()
        isInitialized = true
    }

    final override fun show() {

        check(!isVisible)

        onShow()
        isVisible = true
    }

    final override fun hide() {

        check(isVisible)

        onHide()
        isVisible = false
    }


    final override fun add(element: ElementBase) {
        elements += element
    }

    final override fun rem(element: ElementBase) {
        elements -= element
    }

    final override fun contains(element: ElementBase): Boolean {
        return element in elements
    }

}