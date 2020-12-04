> 这是一个简单易用的变量管理库，类似[EnvironmentSwitcher](https://github.com/CodeXiaoMai/EnvironmentSwitcher)
> 知之非艰,行之惟艰。

[![](https://jitpack.io/v/hcanyz/android-environment-variable.svg)](https://jitpack.io/#hcanyz/android-environment-variable)

欢迎star、pr、issues

### 功能

- 支持多个配置文件，对组件化友好

- 配合build variant，配置默认值可跟随打包变化

- 支持隐藏非当前build variant配置，不暴露私有配置数据

- 自带配置切换ui实现，并支持自定义输入配置项值


### 如何使用

```groovy
// 根目录build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

// 要使用的模块build.gradle添加:

// 可选，用于跟随打包环境
android {
    buildTypes {
        debug {
            buildConfigField("String", "EV_VARIANT", "\"dev\"")
        }
        release {
            buildConfigField("String", "EV_VARIANT", "\"release\"")
        }
    }
}

// 添加依赖
dependencies {
    implementation 'com.github.hcanyz.android-environment-variable:environment-variable-setting:${version}'
    //不需要设置页面 implementation 'com.github.hcanyz:android-environment-variable:${version}'

    kapt 'com.github.hcanyz.android-environment-variable:compiler:${version}'
    // java annotationProcessor 'com.github.hcanyz.android-environment-variable:compiler:${version}'
}
```

编写配置类(支持java、kotlin)
```kotlin
@EvGroup(defaultVariant = BuildConfig.EV_VARIANT)
class EvCoreConfig {
    @EvItem
    class ServerUrl {
        @EvVariant(desc = "dev server url")
        val dev: String = "https://dev.hcanyz.com"

        @EvVariant(desc = "uat server url")
        val uat: String = "https://uat.hcanyz.com"

        @EvVariant(desc = "release server url")
        val release: String = "https://hcanyz.com"

        // 用于初期开发api mock
        @EvVariant(desc = "mock url")
        val _customize_: String = "http://localhost:8080"
    }

    @EvItem
    class H5BaseUrl {

        @EvVariant(desc = "h5 dev server url")
        val dev: String = "https://h5-dev.hcanyz.com"

        @EvVariant(desc = "h5 uat server url")
        val uat: String = "https://h5-uat.hcanyz.com"

        @EvVariant(desc = "h5 release server url")
        val release: String = "https://h5.hcanyz.com"

        @EvVariant(desc = "h5 tmp server url")
        val tmp: String = "https://h5-tmp.hcanyz.com"
    }
}
```

完成编译后会生成一个 ```com.hcanyz.android_kit.widget.core.EvCoreConfigManager```

获取 ```ServerUrl``` 值
```kotlin
EvCoreConfigManager.getSingleton(appContext)
    .getEvItemCurrentValue(EvCoreConfigManager.EV_ITEM_SERVERURL)
```

跳转到设置页面
```kotlin
EvSwitchActivity.skip(context, arrayListOf(EvCoreConfigManager::class.java))
```

<br/>
<br/>
<br/>
---
[android-kit 致力于为android关键场景提供解决方案。  ](https://github.com/zialone/android-kit)