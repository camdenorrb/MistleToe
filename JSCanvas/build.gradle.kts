// TODO: Make the multi project setup support this by not forcing depedencies

plugins {
    kotlin("js") version "1.3.50"
}

group = "me.camdenorrb.mistletoe"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin.target.browser { }