# flutter_mp_plugin

A new Flutter plugin project for multi holistic tracking.

## Getting Started

Include this plugin in pubspec.yaml
```
flutter_mp_plugin:
    git:
      url: https://github.com/Richard8Yang/flutter_mp_plugin.git
      ref: master
```
Refer to example for detailed usage.

## Important Note For Android Release Build
Following code MUST be added to build.gradle of your app to disable resource shrinking and minifying.
```
android {
    buildTypes {
        release {
            shrinkResources false
            minifyEnabled false
        }
    }
}
```
If not added, landmarks will fail to be parsed successfully. In this case, the parsing error will look like:
```
Field landmark_ for k2.e not found. Known fields are [private m2.i0$i k2.e.i, private static final k2.e k2.e.j, private static volatile m2.q1 k2.e.k]
```
