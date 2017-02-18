# SmartGo
Activity跳转时传值和取值

* 省去`intent.putExtra()`、`intent.getXXXExtra()`
* 添加`@IntentExtra`后需Rebuild Project, 自动生成SmartGo
* 暂不支持Serializable类型参数

目标Activity

```
public class TargetActivity extends Activity {

    @IntentExtra("name")
    String myName;
    @IntentExtra("age")
    int myAge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target);

        SmartGo.inject(this);

        System.out.println(myName);
        System.out.println(myAge);
    }

    // 如果在onNewIntent里调用
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        SmartGo.inject(this, intent);

        System.out.println(myName);
        System.out.println(myAge);
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
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.0.2'
    annotationProcessor 'com.github.VictorChow.SmartGo:smartgo-compiler:1.0.2'
}
```