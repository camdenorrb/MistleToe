package me.camdenorrb.mistletoe.opengl

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.system.MemoryUtil.NULL

object OpenGL {

    private var window = NULL


    @JvmStatic
    fun main(args: Array<String>) {

        println("Hello LWJGL ${Version.getVersion()}")

        init()
        loop()

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    fun init() {

        GLFWErrorCallback.createPrint(System.err).set()

        check(glfwInit()) {
            "Unable to initialize GLFW"
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(300, 300, "Meow", NULL, NULL)

        check(window != NULL) {
            "Failed to init GLFW window"
        }





    }

    fun loop() {
        gl
    }



    /*
    @JvmStatic
    fun main(args: Array<String>) {

        val programID = glCreateProgram()

    }


    fun attachVertexShader(programID: Int, name: String) {

        val shaderCode = Resources[name].readText()
        val shaderID = glCreateShader(GL_VERTEX_SHADER)

        glShaderSource(shaderID, shaderCode)
        glCompileShader(shaderID)

        assert(glGetShaderi(shaderID, GL_COMPILE_STATUS) != GL_FALSE) {
            "Error creating vertex shader: ${glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH))}"
        }

        glAttachShader(programID, shaderID)
    }

    fun link(programID: Int) {

        glLinkProgram(programID)

        assert(glGetProgrami(programID, GL_LINK_STATUS) != GL_FALSE) {
            "Unable to link shader program"
        }
    }

    fun dispose() {
    }
    */



}