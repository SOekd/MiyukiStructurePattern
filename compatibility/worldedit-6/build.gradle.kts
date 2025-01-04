plugins {
    id("miyukistructurepattern.platform-conventions")
}

dependencies {
    compileOnly(libs.paper)
    implementation(libs.xseries)

    implementation(project(":miyukistructurepattern-spigot"))
    compileOnly(libs.worldedit.v6)
}