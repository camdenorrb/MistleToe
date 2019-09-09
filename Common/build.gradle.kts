import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "me.camdenorrb.mistletoe"
version = "1.0.0"

repositories {

    mavenCentral()

    maven("https://jitpack.io") {
        name = "Jitpack"
    }
}

dependencies {
    //compile("com.github.camdenorrb:KCommons:V1.0.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}