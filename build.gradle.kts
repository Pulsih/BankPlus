plugins { java }

group = "me.pulsi_"
version = "6.5-Alpha3"

/*tasks.withType<Jar> { // To simplify the testing, set the path directly into the plugins folder.
    destinationDirectory.set(File("C:\\Users\\faste\\Desktop\\Test Servers\\1.21.4\\plugins"))
}*/

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "essentialsx-releases"
        url = uri("https://repo.essentialsx.net/releases/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

// group:artifact:version
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar")
            )
        )
    ) // CMI-Api: 9.7.4.1 (https://github.com/Zrips/CMI-API/releases)

    implementation("net.essentialsx:EssentialsX:2.21.0") {
        // It is requesting spigot api, but it goes in conflict with paper api.
        exclude("org.spigotmc")
    }

    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
}

val targetJavaVersion = 21
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}