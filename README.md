# Incandescent Lib
A light-weight library providing API for entity & player animations with included utility

## Features
IncandescentLib provides following the set of features:
- Item interface for simple keyframe entity animation implementation
- Item interface for automated description collection
- Item interface for gradient-coloured name
- Item interface for overriding attribute colours
- Item interface for easy way to provide custom swing animation
- etc.

Some features are not mentioned, and that is because they are too unstable for general usage.

## Documentation
Refer to comments in code until better documentation is provided

## How to start
To use Incandescent Lib for your project, include the following line in your build.gradle:\
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
    implementation fg.deobf("curse.maven:incandescent_lib-971520:6062864")
}
```
