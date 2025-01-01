plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://repo.codemc.io/repository/maven-releases/")
}

dependencies {
    implementation(libs.shadow)
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}