package me.camdenorrb.mistletoe.common.gui.base

interface Artist {

    interface `2D` : Artist {

        fun drawCircle()

    }

    interface `3D` : Artist {

        fun drawSphere()

    }

}