plugins {
    id("miyukistructurepattern.platform-conventions")
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.paper)
    implementation(libs.xseries)

    implementation(project(":miyukistructurepattern-spigot"))
    compileOnly(libs.worldedit.v6)
}