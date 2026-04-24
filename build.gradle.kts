import org.jetbrains.intellij.platform.gradle.TestFrameworkType

version = "1.0.0"
group = "gugli.anto"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.1")
        testFramework(TestFrameworkType.Platform)
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.modules.json")
    }

    implementation("tools.jackson.core:jackson-databind:3.1.2")
}

changelog {
    version.set(project.version.toString())
    path.set(file("CHANGELOG.md").canonicalPath)
}

intellijPlatform {
    pluginConfiguration {
        vendor {
            name.set("AntoGugli")
        }

        description.set("""
            Java2JSON converts your Java POJOs into formatted JSON strings instantly.
            <br><br>
            <b>Features:</b>
            <ul>
                <li>One-click serialization from the Tool Window</li>
                <li>Support for Enums, Collections, and Maps</li>
                <li>Native search (Ctrl/Cmd + F) within the generated JSON</li>
                <li>Recursive resolution of custom objects and cycle detection</li>
            </ul>
            <br>
            <i>Licensed under Apache License 2.0.</i>
        """.trimIndent())
    }

    signing {}
}

tasks {

    patchPluginXml {
        version = project.version.toString()

        sinceBuild = "232"

        changeNotes = provider {
            changelog.renderItem(
                changelog.get(project.version.toString())
                    .withHeader(false),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        }
    }
}
