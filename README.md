# SmartGo  [![](https://jitpack.io/v/VictorChow/SmartGo.svg)](https://jitpack.io/#VictorChow/SmartGo)

#### Activity跳转时传值和取值

* 省去`intent.putExtra()`、`intent.getXXXExtra()`
* 添加`@IntentExtra`后需Rebuild项目生成SmartGo类
* 支持Java、Kotlin
* 暂不支持Serializable

#### 要跳转到的Activity

##### Java

```java
public class TargetActivity extends Activity {
    @IntentExtra("name")
    String myName; 
    @IntentExtra //不加value时默认为属性名
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SmartGo.inject(this);
      
        Log.i("name", myName);
        Log.i("age", age);
    }

    // 若需在onNewIntent里调用
    @Override
    protected void onNewIntent(Intent intent) {
        SmartGo.inject(this, intent);
      
        Log.i("name", myName);
        Log.i("age", age);
    }
}
```
##### Kotlin

```kotlin
class TargetActivity : Activity() {
    @IntentExtra("name") lateinit var myName: String
    @IntentExtra var age = 0    //不加value时默认为属性名

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SmartGo.inject(this)

        myName.log("name")
        age.log("age")
    }

    // 若需在onNewIntent里调用
    override fun onNewIntent(intent: Intent) {
        SmartGo.inject(this, intent)

        myName.log("name")
        age.log("age")
    }

    private fun Any.log(tag: String) {
        Log.i(tag, this.toString())
    }
}
```

#### 跳转

```java
SmartGo.from(this)
	   .toTargetActivity()
	   .setName("Victor")
	   .setAge(23)
	   .go();
```

#### 其他

```java
SmartGo.from(this)
       .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
       .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
       .setAnim(R.anim.enterAnim, R.anim.exitAnim)
       // setXXX()
       .go(requestCode);
```

## Gradle

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

##### Java

```groovy
dependencies {
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.1.0'
    annotationProcessor 'com.github.VictorChow.SmartGo:smartgo-compiler:1.1.0'
}
```
##### Kotlin

```groovy
dependencies {
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.1.0'
    kapt 'com.github.VictorChow.SmartGo:smartgo-compiler-kt:1.1.0'
}

kapt {
    generateStubs = true
}
```

