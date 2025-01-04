plugins {
    id("miyukistructurepattern.platform-conventions")
}

dependencies {
    compileOnly(libs.paper)

    compileOnly(libs.worldguard.v7)
    implementation(project(":miyukistructurepattern-spigot"))
}