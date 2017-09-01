# SmartGo  [![](https://jitpack.io/v/VictorChow/SmartGo.svg)](https://jitpack.io/#VictorChow/SmartGo)

### 通过编译时注解生成文件，简化Activity跳转时传值及取值

* 支持 Java、Kotlin
* 自动生成传值取值方法，省去`intent.putExtra()`、`intent.getXXXExtra()`
* 属性添加`@IntentExtra`后需要**Make Project**，自动生成SmartGo类
* 支持`intent.putExtra()`除Serializable以外的其他类型

### 要跳转到的Activity

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
        //注入
        SmartGo.inject(this);
    }

    //若需在onNewIntent里调用
    @Override
    protected void onNewIntent(Intent intent) {
        //注入
        SmartGo.inject(this, intent);
    }
}
```
#### Kotlin

```kotlin
class TargetActivity : Activity() {
    @IntentExtra("name") 
    lateinit var myName: String

    @IntentExtra 
    var age = 0    //不加value时默认为属性名

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //注入
        SmartGo.inject(this)
    }

    //若需在onNewIntent里调用
    override fun onNewIntent(intent: Intent) {
      	//注入	
        SmartGo.inject(this, intent)
    }
}
```

### 跳转
##### 自动生成ToXXXActivity()、setXXX（）方法

```java
SmartGo.from(context)
       .toTargetActivity()
       .setName("Victor")
       .setAge(23)
       .go();
```

### 其他

```java
SmartGo.from(context)
       .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
       .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
       .setAnim(R.anim.enterAnim, R.anim.exitAnim)
       // .setXXX()
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

#### Java

```groovy
dependencies {
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.2.0'
    annotationProcessor 'com.github.VictorChow.SmartGo:smartgo-compiler:1.2.0'
}
```
#### Kotlin

```groovy
apply plugin: 'kotlin-kapt'

dependencies {
    compile 'com.github.VictorChow.SmartGo:smartgo-annotation:1.2.0'
    kapt 'com.github.VictorChow.SmartGo:smartgo-compiler-kt:1.2.0'
}
```

## 混淆

```
-keep class * implements pers.victor.smartgo.SmartGoInjector
```

## 测试

支持类型

```java
@IntentExtra("boolean")
boolean myBoolean;
@IntentExtra("booleans")
boolean[] myBooleans;
@IntentExtra("byte")
byte myByte;
@IntentExtra("bytes")
byte[] myBytes;
@IntentExtra("short")
short myShort;
@IntentExtra("shorts")
short[] myShorts;
@IntentExtra("int")
int myInt;
@IntentExtra("ints")
int[] myInts;
@IntentExtra("float")
float myFloat;
@IntentExtra("floats")
float[] myFloats;
@IntentExtra("double")
double myDouble;
@IntentExtra("doubles")
double[] myDoubles;
@IntentExtra("long")
long myLong;
@IntentExtra("longs")
long[] myLongs;
@IntentExtra("char")
char myChar;
@IntentExtra("chars")
char[] myChars;
@IntentExtra("charSequence")
CharSequence myCharSequence;
@IntentExtra("charSequences")
CharSequence[] myCharSequences;
@IntentExtra("string")
String myString;
@IntentExtra("strings")
String[] myStrings;
@IntentExtra("parcelable")
TestBean testBean;
@IntentExtra("parcelables")
TestBean[] testBeans;
@IntentExtra("bundle")
Bundle myBundle;
@IntentExtra("intList")
ArrayList<Integer> myIntList;
@IntentExtra("stringList")
ArrayList<String> myStringList;
@IntentExtra("charSequenceList")
ArrayList<CharSequence> myCharSequenceList;
@IntentExtra("parcelableList")
ArrayList<TestBean> myParcelableList;
```

传值

```java
SmartGo.from(this)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        .setAnim(R.anim.enter, R.anim.exit)
        .toTestActivity()
        .setBoolean(false)
        .setBooleans(new boolean[]{false, true, false})
        .setByte((byte) 1)
        .setBytes(new byte[]{(byte) 2, (byte) 3, (byte) 4})
        .setShort((short) 5)
        .setShorts(new short[]{(short) 6, (short) 7, (short) 8})
        .setInt(9)
        .setInts(new int[]{10, 11, 12})
        .setIntList(integers)
        .setFloat(13f)
        .setFloats(new float[]{14f, 15f, 16f})
        .setDouble(17)
        .setDoubles(new double[]{18, 19, 20})
        .setLong(21L)
        .setLongs(new long[]{22L, 23L, 24L})
        .setChar('A')
        .setChars(new char[]{'B', 'C', 'D'})
        .setCharSequence("CharSequence1")
        .setCharSequences(new CharSequence[]{"CharSequence2", "CharSequence3", "CharSequence4"})
        .setCharSequenceList(charSequences)
        .setString("String1")
        .setStrings(new String[]{"String2", "String3", "String4"})
        .setStringList(strings)
        .setParcelable(new TestBean(25))
        .setParcelables(new TestBean[]{new TestBean(26), new TestBean(27), new TestBean(28)})
        .setParcelableList(testBeen)
        .setBundle(bundle)
        .go();
```
