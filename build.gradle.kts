plugins {
    kotlin("jvm") version "2.2.20-Beta2"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") } // Paper
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib")) // Shadowで同梱
    // MiniMessage を使うなら以下（Paper本体とは別モジュールなので必要な場合は入れる）
    // implementation("net.kyori:adventure-text-minimessage:4.22.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    // ★ 追加: plugin.yml の ${version} を Gradle の project.version で置換
    processResources {
        // resources ディレクトリ直下の plugin.yml を対象に展開
        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "version" to project.version    // ← gradle.properties の version が入る
                )
            )
            filteringCharset = "UTF-8"
        }
    }

    // 依存を同梱した実行用JARを生成（分類子なし = そのまま配布用に）
    shadowJar {
        archiveClassifier.set("")
        minimize() // 使っていない依存をできるだけ省く（任意）
    }
    // 通常の `build` で shadowJar を作る
    build { dependsOn(shadowJar) }
}