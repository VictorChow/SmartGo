# SmartGo  [![](https://jitpack.io/v/VictorChow/SmartGo.svg)](https://jitpack.io/#VictorChow/SmartGo)

#### 简化Activity传值，跨模块跳转、获取实例

## Gradle配置

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

#### Java

```groovy
android {
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [ module : project.name ]
            }
        }
    }
}

dependencies {
    implementation 'com.github.VictorChow.SmartGo:core:2.0.0-rc3'
    annotationProcessor 'com.github.VictorChow.SmartGo:compiler:2.0.0-rc3'
}
```
#### Kotlin

```groovy
apply plugin: 'kotlin-kapt'

dependencies {
    implementation 'com.github.VictorChow.SmartGo:core:2.0.0-rc3'
    kapt 'com.github.VictorChow.SmartGo:compiler:2.0.0-rc3'
}

kapt {
    arguments {
        arg("module", project.name)
    }
}
```

## 混淆

```
-keep class * implements pers.victor.smartgo.SmartGoInjector
-keep class * implements pers.victor.smartgo.SmartPathInjector
-keep class * implements pers.victor.smartgo.InstanceInjector
```

## 同模块

### 目标Activity

- 属性添加`@IntentExtra`后需要**Make Project**生成SmartGo类
- 不支持Serializable

#### Java

```java
public class TargetActivity extends Activity {
    @IntentExtra("name")
    String myName; 
    @IntentExtra //不加value时默认为属性名
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartGo.inject(this); //注入
    }

    //若需要在onNewIntent里调用时
    @Override
    protected void onNewIntent(Intent intent) {
        SmartGo.inject(this, intent); //注入
    }
}
```

#### Kotlin

类似Java，**非`lateinit`属性需要添加`@JvmField`注解**

### 跳转

##### 自动生成ToXXXActivity()、setXXX()

```java
SmartGo.from(context)
       .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)    //需要时使用，非必需
       .setAnim(R.anim.enterAnim, R.anim.exitAnim) //需要时使用，非必需
       .toTargetActivity()
       .setName("Victor")
       .setAge(23)
       .go(); //可填入requestCode
```

## 跨模块跳转

### module1

```java
@Path("module1_activity")
public class Module1Activity extends Activity {
    //内部配置类似单模块
}
```

### module2

```java
SmartPath.from(context)
         .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)    //需要时使用，非必需
         .setAnim(R.anim.enterAnim, R.anim.exitAnim) //需要时使用，非必需
         .toPath("module1_activity")
         .putString("name", "victor")
         .putInt("age", 23)
         .go(); //可填入requestCode
```

## 跨模块获取实例

### module1

```java
@Instance("test_fragment")
public class TestFragment extends Fragment {
}

@Instance("test_invoke")
public class TestInvokable implements Invokable {
    @Override
    public Object invoke(Object... key) {
        return "TestInvokable";
    }
}
```

### module2

```java
 Fragment fragment = SmartPath.createInstance("test_fragment");
 Invokable invokable = SmartPath.createInstance("test_invoke");
```
