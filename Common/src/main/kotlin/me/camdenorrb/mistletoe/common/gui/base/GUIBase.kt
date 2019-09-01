package me.camdenorrb.mistletoe.common.gui.base

import me.camdenorrb.kcommons.base.Named

interface GUIBase : Named {

    fun init()

    fun show()

    fun hide()


    fun add(element: ElementBase)

    fun rem(element: ElementBase)

    operator fun contains(element: ElementBase): Boolean

}