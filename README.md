# Incandescent Lib
A light-weight library providing vast API for reducing boilerplate of common Forge code, along with
providing general utility, convenient data animation format, numerous interfaces, etc., etc.

## How to use
To use Incandescent Lib for your project, include the following lines in your `build.gradle`:
```groovy
repositories {
    maven {
        url "https://cursemaven.com"
        content {
            includeGroup "curse.maven"
        }
    }
}
dependancies {
    implementation fg.deobf("curse.maven:incandescent_lib-971520:${incandescent_lib_artifact_version}")
}
```