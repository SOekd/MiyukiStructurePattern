plugins {
    id("miyukistructurepattern.platform-conventions")
}

repositories {
    maven("https://repo.glaremasters.me/repository/bloodshot")
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":miyukistructurepattern-spigot"))
    compileOnly(libs.griefdefender)
}