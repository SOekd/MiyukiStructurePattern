plugins {
    id("miyukistructurepattern.platform-conventions")
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":miyukistructurepattern-spigot"))
    compileOnly(libs.griefprevention)
}