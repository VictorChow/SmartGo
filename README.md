# SmartGo
Activity跳转时传值和取值

* 省去`intent.putExtra`、`intent.getXXXExtra`
* 添加`@IntentExtra`后需Rebuild项目生成SmartGo类
* 暂不支持Serializable类型

要跳转到的Activity

```
public class TargetActivity extends Activity {
    @IntentExtra("name")
    String myName; 
    @IntentExtra //不加value时默认为属性名
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

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
跳转

	SmartGo.from(this)
		   .toTargetActivity()
		   .setName("Victor")
		   .setAge(23)
		   .go();

其他设置

	SmartGo.from(this)
	       .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
	       .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
	       .setAnim(R.anim.enterAnim, R.anim.exitAnim)
	       // setXXX()
	       .go(requestCode);

## Gradle

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

```
dependencies {
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.0.6'
    annotationProcessor 'com.github.VictorChow.SmartGo:smartgo-compiler:1.0.6'
}
```