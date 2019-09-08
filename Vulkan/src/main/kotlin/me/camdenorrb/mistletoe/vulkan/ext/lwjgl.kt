package me.camdenorrb.mistletoe.vulkan.ext

import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil.memFree
import org.lwjgl.system.NativeResource
import org.lwjgl.system.Struct
import java.nio.Buffer

// USE

// Not inlined to avoid potential returning from within

fun <T : Struct, R : Any> T.use(block: (T) -> R): R {
    return block(this).also { free() }
}

fun <T : NativeResource, R : Any> T.use(block: (T) -> R): R {
    return block(this).also { free() }
}

fun <T : Buffer, R : Any> T.use(block: (T) -> R): R {
    return block(this).also { free() }
}

fun <T : PointerBuffer, R : Any> T.use(block: (T) -> R): R {
    return block(this).also { free() }
}


// FREE

fun Buffer.free() {
    memFree(this)
}