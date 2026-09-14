pluginManagement {
  repositories {
    maven { url = java.net.URI("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    google()
    gradlePluginPortal()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven { url = java.net.URI("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
    google()
    mavenCentral()
  }
}

rootProject.name = "Car Hisab"

include(":app")
