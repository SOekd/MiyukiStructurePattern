plugins {
    id("miyukistructurepattern.platform-conventions")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":miyukistructurepattern-spigot"))
    implementation(platform(libs.plotsquared.bom))
    compileOnly(libs.plotsquared.core)
    compileOnly(libs.plotsquared.bukkit) {
        isTransitive = false
    }
}