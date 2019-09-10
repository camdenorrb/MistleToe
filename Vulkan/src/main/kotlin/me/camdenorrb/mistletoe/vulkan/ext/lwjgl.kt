package me.camdenorrb.mistletoe.vulkan.ext

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.system.NativeResource
import org.lwjgl.system.Struct
import java.nio.Buffer

// USE

inline fun <T : Struct, R : Any> T.use(block: (T) -> R): R {
    try {
        return block(this)
    }
    finally {
        free()
    }
}

inline fun <T : NativeResource, R : Any> T.use(block: (T) -> R): R {
    try {
        return block(this)
    }
    finally {
        free()
    }
}

inline fun <T : Buffer, R : Any> T.use(block: (T) -> R): R {
    try {
        return block(this)
    }
    finally {
        free()
    }
}

inline fun <T : PointerBuffer, R : Any> T.use(block: (T) -> R): R {
    try {
        return block(this)
    }
    finally {
        free()
    }
}


// FREE

fun Buffer.free() {
    memFree(this)
}