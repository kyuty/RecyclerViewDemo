1. AndroidManifest.xml pkg: me.xiazdong.recyclerviewdemo
2. build.gradle

```
android {
    compileSdkVersion 24
    defaultConfig {
        minSdkVersion 20
        targetSdkVersion 24
    }
}
dependencies {
    implementation 'com.android.support:appcompat-v7:24.0.0'
    implementation 'com.android.support:recyclerview-v7:24.2.1'
    implementation 'com.android.support:design:24.2.1'
    implementation 'com.squareup.picasso:picasso:2.5.2'
    implementation 'com.daimajia.numberprogressbar:library:1.2@aar'
    implementation 'jp.wasabeef:recyclerview-animators:2.2.4'
}
```