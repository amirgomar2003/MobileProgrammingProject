pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
//        maven { url = uri("https://maven.aliyun.com/repository/google") }
//        maven { url = uri("https://maven.aliyun.com/repository/central") }
//        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
//        maven { url = uri("https://maven.aliyun.com/repository/public") }
//        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.163.com/maven/repository/maven-public/") }
        maven { url = uri("https://mirrors.ustc.edu.cn/maven/") }   // USTC
        maven { url = uri("https://mirrors.bfsu.edu.cn/maven2/") }  // BFSU
        maven { url = uri("https://repo.fastgit.org/maven-central/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://jcenter.bintray.com") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        maven { url = uri("https://repository.apache.org/content/repositories/releases/") }
        maven { url = uri("https://repository.apache.org/content/repositories/snapshots/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven { url = uri("https://maven.aliyun.com/repository/google") }
//        maven { url = uri("https://maven.aliyun.com/repository/central") }
//        maven { url = uri("https://maven.aliyun.com/repository/public") }
//        maven { url = uri("https://mirrors.tencent.com/nexus/repository/gradle-plugins/") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        maven { url = uri("https://mirrors.ustc.edu.cn/maven/") }   // USTC
        maven { url = uri("https://mirrors.bfsu.edu.cn/maven2/") }  // BFSU
        maven { url = uri("https://repo.fastgit.org/maven-central/") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NoteProject"
include(":app")
