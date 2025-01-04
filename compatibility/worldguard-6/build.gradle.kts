plugins {
    id("miyukistructurepattern.platform-conventions")
}

dependencies {
    compileOnly(libs.paper)

    compileOnly(libs.worldguard.v6)
    implementation(project(":miyukistructurepattern-spigot"))
}