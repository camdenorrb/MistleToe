package me.camdenorrb.mistletoe.common.gui.impl

import me.camdenorrb.mistletoe.common.data.Pos
import me.camdenorrb.mistletoe.common.gui.base.ElementBase

sealed class Image(override val name: String) : ElementBase {

    abstract val pos: Pos

    abstract val imagePath: String


    data class GIF(override val pos: Pos, override val imagePath: String) : Image("GIF")

    data class JPG(override val pos: Pos, override val imagePath: String) : Image("JPG")

    data class PNG(override val pos: Pos, override val imagePath: String) : Image("PNG")

}