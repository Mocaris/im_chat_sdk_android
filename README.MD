# IM SDK For Android


###
* AGP 版本 >= 8.9.1
* minSdk 23
* targetSdk 36
* jdk >=17 
* kotlin plugin 2.2.0

## 使用方式
1. 添加依赖
```groovy
dependencies {
    implementation("com.github.Mocaris:im_chat_sdk_android:{version}")
}
```
2. 初始化  
```kotlin
LbeMainActivity.start(context: Context, args: SDKInitConfig)
```