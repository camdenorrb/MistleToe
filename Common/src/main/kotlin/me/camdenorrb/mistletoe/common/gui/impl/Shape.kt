package me.camdenorrb.mistletoe.common.gui.impl;

import me.camdenorrb.mistletoe.common.data.Pos
import me.camdenorrb.mistletoe.common.gui.base.ElementBase
import java.awt.Color

sealed class Shape(override val name: String) : ElementBase {

    open var color = Color.BLACK

    open var filled = true

        
    data class Text(val pos: Pos, var data: String) : Shape("Text")


    data class Poly(val points: MutableList<Pos>) : Shape("Polygon")

    data class Oval(val pos: Pos, var width: Double, var height: Double) : Shape("Oval")

    data class Rect(val pos: Pos, var width: Double, var height: Double) : Shape("Rectangle")

}