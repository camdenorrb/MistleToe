import org.gradle.internal.os.OperatingSystem

val lwjglVersion = "3.2.2"

val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX   -> "natives-linux"
    OperatingSystem.MAC_OS  -> "natives-macos"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported Operating system. Please set \"lwjglNatives\" manually")
}

dependencies {

    implementation(project(":Common"))

    // Vulkan
    implementation("org.lwjgl", "lwjgl", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-assimp", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-glfw", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-openal", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-stb", lwjglVersion)
    implementation("org.lwjgl", "lwjgl-vulkan", lwjglVersion)
    runtimeOnly("org.lwjgl", "lwjgl", lwjglVersion, classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-assimp", lwjglVersion, classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", lwjglVersion, classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", lwjglVersion, classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", lwjglVersion, classifier = lwjglNatives)
    if (lwjglNatives == "natives-macos") runtimeOnly("org.lwjgl", "lwjgl-vulkan", lwjglVersion, classifier = lwjglNatives)
}