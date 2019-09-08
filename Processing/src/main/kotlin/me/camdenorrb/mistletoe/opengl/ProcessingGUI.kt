package me.camdenorrb.mistletoe.opengl

import me.camdenorrb.mistletoe.common.gui.base.ElementBase
import me.camdenorrb.mistletoe.common.gui.impl.Shape
import me.camdenorrb.mistletoe.common.gui.struct.GUIStruct
import kotlin.reflect.KClass

class ProcessingGUI(override val fps: Int) : GUIStruct() {

    override val name = "Processing"


    override fun onInit() {
       // TODO: Init Processing App
    }

    override fun onShow() {
        // TODO: Start loop at selected FPS
    }

    override fun onHide() {
        // TODO: Stop loop and hide
    }


    override fun draw(element: ElementBase) {

        // TODO: Add element to a list instead
        val artist = checkNotNull(artists[element::class]) {
            "$name: Artist doesn't exist for ${element::class}"
        }

        artist.invoke(this)
    }


    companion object {

        @PublishedApi
        internal val artists = mutableMapOf<KClass<out ElementBase>, (ProcessingGUI) -> Unit>()


        init {

            addArtist<Shape.Rect> {

            }

        }


        inline fun <reified T : ElementBase> addArtist(noinline block: (ProcessingGUI) -> Unit) {
            artists[T::class] = block
        }

        inline fun <reified T : ElementBase> remArtist() {
            artists.remove(T::class)
        }

    }

}