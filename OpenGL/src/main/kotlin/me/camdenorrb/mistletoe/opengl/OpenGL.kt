package me.camdenorrb.mistletoe.opengl

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack.stackPush
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
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE)

        window = glfwCreateWindow(300, 300, "Meow", NULL, NULL)

        check(window != NULL) {
            "Failed to init GLFW window"
        }

        /*
        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
        }
        */

        stackPush().use { stack ->

            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)

            glfwGetWindowSize(window, pWidth, pHeight)

            val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

            glfwSetWindowPos(
                    window,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pWidth.get(0)) / 2
            )

            glfwMakeContextCurrent(window)

            glfwSwapInterval(1) // V-Sync

            glfwShowWindow(window)
        }

    }

    fun loop() {

        GL.createCapabilities()

        //GL11.glClearColor(0.5f, 0f, 0.5f, 1.0f)

        // TODO: Don't use a forking while loop, atleast delay or something
        while (!glfwWindowShouldClose(window)) {

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            //GL15C.glGenBuffers()
            //GL15C.glBufferData()

            glfwSwapBuffers(window)
            glfwPollEvents()
        }

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