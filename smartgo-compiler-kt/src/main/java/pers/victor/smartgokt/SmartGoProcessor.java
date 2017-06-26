package pers.victor.smartgokt;

import com.google.auto.service.AutoService;
import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.KotlinFile;
import com.squareup.kotlinpoet.ParameterizedTypeName;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.TypeName;
import com.squareup.kotlinpoet.TypeNameKt;
import com.squareup.kotlinpoet.TypeSpec;
import com.squareup.kotlinpoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import pers.victor.smartgo.IntentExtra;
import pers.victor.smartgo.SmartGoInjector;

/**
 * Created by Victor on 2017/2/3. (ง •̀_•́)ง
 */

@AutoService(Processor.class)
public class SmartGoProcessor extends AbstractProcessor {
    private static final String PACKAGE_NAME = "pers.victor.smartgokt";
    private Filer filer;
    private Map<String, SmartGoEntity> map;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        map = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(IntentExtra.class)) {
            if (!(element instanceof VariableElement)) {
                return false;
            }
            getEachVariableElement(element);
        }
        try {
            createSmartGo();
            createInjectors();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getEachVariableElement(Element element) {
        VariableElement variableElement = (VariableElement) element;
        String packageName = processingEnv.getElementUtils().getPackageOf(variableElement).getQualifiedName().toString();
        String fieldName = variableElement.getSimpleName().toString();
        String fieldType = variableElement.asType().toString();
        String className = variableElement.getEnclosingElement().getSimpleName().toString();
        IntentExtra annotation = element.getAnnotation(IntentExtra.class);
        String fieldValue = annotation.value().isEmpty() ? fieldName : annotation.value();
        String canonicalClassName = packageName + "." + className;
        SmartGoEntity smartGoEntity;
        if (map.get(canonicalClassName) == null) {
            smartGoEntity = new SmartGoEntity();
            smartGoEntity.packageName = packageName;
            smartGoEntity.className = className;
            map.put(canonicalClassName, smartGoEntity);
        } else {
            smartGoEntity = map.get(canonicalClassName);
        }
        if (fieldType.contains("<") && fieldType.contains(">")) {
            int startIndex = fieldType.indexOf("<");
            int endIndex = fieldType.indexOf(">");
            String class1 = fieldType.substring(0, startIndex);
            String class2 = fieldType.substring(startIndex + 1, endIndex);
            SmartGoEntity.FieldEntity entity = new SmartGoEntity.FieldEntity();
            entity.fieldName = fieldName;
            entity.fieldValue = fieldValue;
            entity.fieldType = class1;
            entity.fieldParam = class2;
            smartGoEntity.fields.add(entity);
        } else {
            String[] typeArray = {
                    "boolean", "boolean[]",
                    "byte", "byte[]",
                    "short", "short[]",
                    "int", "int[]",
                    "long", "long[]",
                    "double", "double[]",
                    "float", "float[]",
                    "char", "char[]",
                    "java.lang.CharSequence", "java.lang.CharSequence[]",
                    "java.lang.String", "java.lang.String[]",
                    "android.os.Bundle"
            };
            if (Arrays.asList(typeArray).contains(fieldType)) {
                smartGoEntity.fields.add(new SmartGoEntity.FieldEntity(fieldName, fieldType, fieldValue));
            } else {
                String type = fieldType.contains("[]") ? "android.os.Parcelable[]" : "android.os.Parcelable";
                SmartGoEntity.FieldEntity entity = new SmartGoEntity.FieldEntity(fieldName, type, fieldValue);
                entity.originalType = fieldType.replace("[]", "");
                smartGoEntity.fields.add(entity);
            }
        }
    }

    private void createSmartGo() throws Exception {
        //SmartGo里GoToXXXActivity类列表
        List<TypeSpec> targetActivitiesClassList = new LinkedList<>();
        //SmartGo里GoToActivity类里的方法列表
        List<FunSpec> goToActivitiesMethodList = new LinkedList<>();
        for (Map.Entry<String, SmartGoEntity> entry : map.entrySet()) {
            String className = entry.getValue().className;
            String fullClassName = entry.getKey();
            //SmartGo里GoToXXXActivity类里方法列表
            List<FunSpec> targetActivitiesMethodList = new LinkedList<>();
            for (SmartGoEntity.FieldEntity field : entry.getValue().fields) {
                String methodName = "set" + field.fieldValue.substring(0, 1).toUpperCase() + field.fieldValue.substring(1, field.fieldValue.length());
                //ArrayListExtra里的泛型
                String paramName = "";
                if (!field.fieldParam.isEmpty()) {
                    String paramSimpleName = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                    switch (paramSimpleName) {
                        case "Integer":
                            paramName = "IntegerArrayList";
                            break;
                        case "String":
                            paramName = "StringArrayList";
                            break;
                        case "CharSequence":
                            paramName = "CharSequenceArrayList";
                            break;
                        default:
                            paramName = "ParcelableArrayList";
                            break;
                    }
                }
                FunSpec method = FunSpec.builder(methodName)
                        .addParameter(field.fieldValue + "Extra", getFieldType(field))
                        .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                        .addStatement("intent!!.put%LExtra(%S, %L)", paramName, field.fieldValue, field.fieldValue + "Extra")
                        .addStatement("return this")
                        .build();
                targetActivitiesMethodList.add(method);
            }
            //SmartGo里GoToXXXActivity类的go()
            FunSpec go = FunSpec.builder("go")
                    .addStatement("gokt(%T::class.java)", ClassName.bestGuess(fullClassName))
                    .build();
            FunSpec goForResult = FunSpec.builder("go")
                    .addParameter("requestCode", TypeNameKt.INT)
                    .addStatement("gokt(%T::class.java, requestCode)", ClassName.bestGuess(fullClassName))
                    .build();
            FunSpec title = FunSpec.builder("setTitle")
                    .addParameter("titleExtra", ClassName.get("kotlin", "String"))
                    .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .addStatement("intent!!.putExtra(%S, %L)", "title", "titleExtra")
                    .addStatement("return this")
                    .build();
            //私有构造方法
            //SmartGo里GoToXXXActivity类
            TypeSpec type = TypeSpec.classBuilder("To" + className)
                    .addFun(title)
                    .addFunctions(targetActivitiesMethodList)
                    .addFun(go)
                    .addFun(goForResult)
                    .build();
            //SmartGo里GoToActivity类里的方法
            FunSpec method = FunSpec.builder("to" + className)
                    .addKdoc("@see %T ←方便跳转\n", ClassName.bestGuess(fullClassName))
                    .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .addStatement("return %T()", ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .build();
            targetActivitiesClassList.add(type);
            goToActivitiesMethodList.add(method);
        }
        //SmartGo里的GoToActivity类里的addFlags()
        FunSpec addFlags = FunSpec.builder("addFlags")
                .addParameter("flags", TypeNameKt.INT)
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("intent!!.addFlags(flags)")
                .addStatement("return this")
                .build();
        //SmartGo里的GoToActivity类里的setAnim()
        FunSpec setAnim = FunSpec.builder("setAnim")
                .addParameter("enterAnimId", TypeNameKt.INT)
                .addParameter("exitAnimId", TypeNameKt.INT)
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("enterAnim = enterAnimId")
                .addStatement("exitAnim = exitAnimId")
                .addStatement("return this")
                .build();
        //SmartGo里的GoToActivity类
        TypeSpec smartGoToActivity = TypeSpec.classBuilder("ToActivity")
                .addFun(setAnim)
                .addFun(addFlags)
                .addFunctions(goToActivitiesMethodList)
                .build();
        //SmartGo类里inject(activity)
        FunSpec inject = FunSpec.builder("inject")
                .addParameter("activity", ClassName.bestGuess("android.app.Activity"))
                .addStatement("inject(activity, null)")
                .build();
        //SmartGo类里inject(activity)
        FunSpec inject2 = FunSpec.builder("inject")
                .addParameter("activity", ClassName.bestGuess("android.app.Activity"))
                .addParameter("intent", ClassName.bestGuess("android.content.Intent").asNullable())
                .addCode("try {\n" +
                        "  val injectorName = activity::class.java.getCanonicalName() + \"_SmartGo\"\n" +
                        "  (Class.forName(injectorName).newInstance() as %T<%T>).inject(activity, intent)\n" +
                        "} catch (e: Exception) {\n" +
                        "  e.printStackTrace()\n" +
                        "}\n", SmartGoInjector.class, ClassName.bestGuess("android.app.Activity"))
                .build();
        //SmartGo类里from()
        FunSpec from = FunSpec.builder("from")
                .addParameter("ctx", ClassName.bestGuess("android.content.Context"))
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("context = ctx")
                .addStatement("intent = Intent()")
                .addStatement("return %T()", ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .build();
        //SmartGo类里go()
        FunSpec go = FunSpec.builder("gokt")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("clazz", ParameterizedTypeName.get(ClassName.bestGuess(Class.class.getCanonicalName()), TypeVariableName.get("*")))
                .addStatement("intent!!.setClass(context, clazz)")
                .addStatement("context!!.startActivity(intent)")
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build();
        //SmartGo类里goForResult()
        FunSpec goForResult = FunSpec.builder("gokt")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("clazz", ParameterizedTypeName.get(ClassName.bestGuess(Class.class.getCanonicalName()), TypeVariableName.get("*")))
                .addParameter("requestCode", TypeNameKt.INT)
                .addStatement("intent!!.setClass(context, clazz)")
                .addCode("if(enterAnim < 0 || exitAnim < 0){\n" +
                        "  return\n" +
                        "}\n" +
                        "if (context !is %T) {\n" +
                        "  throw %T(\"非Activity的Context，不能startActivityForResult\")\n" +
                        "} else {\n" +
                        "  (context as Activity).startActivityForResult(intent, requestCode)\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("kotlin.IllegalArgumentException"))
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build();
        //SmartGo类里reset()
        FunSpec reset = FunSpec.builder("reset")
                .addModifiers(KModifier.PRIVATE)
                .addStatement("intent = null")
                .addStatement("context = null")
                .addStatement("enterAnim = -1")
                .addStatement("exitAnim = -1")
                .build();
        //SmartGo类里setTransition()
        FunSpec setTransition = FunSpec.builder("setTransition")
                .addModifiers(KModifier.PRIVATE)
                .addCode("if(enterAnim < 0 || exitAnim < 0){\n" +
                        "  return\n" +
                        "}\n" +
                        "if (context !is %T) {\n" +
                        "  throw %T(\"非Activity的Context，不能overridePendingTransition\")\n" +
                        "} else {\n" +
                        "  (context as Activity).overridePendingTransition(enterAnim, exitAnim)\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("kotlin.IllegalArgumentException"))
                .build();
        //SmartGo类里enterAnim
        PropertySpec enterAnim = PropertySpec.builder("enterAnim", TypeNameKt.INT, KModifier.PRIVATE)
                .mutable(true)
                .initializer("-1")
                .build();
        //SmartGo类里exitAnim
        PropertySpec exitAnim = PropertySpec.builder("exitAnim", TypeNameKt.INT, KModifier.PRIVATE)
                .mutable(true)
                .initializer("-1")
                .build();
        //SmartGo类
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(PropertySpec.builder("context", ClassName.bestGuess("android.content.Context").asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build());
        propertySpecs.add(PropertySpec.builder("intent", ClassName.bestGuess("android.content.Intent").asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build());
        propertySpecs.add(enterAnim);
        propertySpecs.add(exitAnim);
        TypeSpec smartGo = TypeSpec.objectBuilder("SmartGo")
                .addProperties(propertySpecs)
                .addFun(inject)
                .addFun(inject2)
                .addFun(from)
                .addFun(go)
                .addFun(goForResult)
                .addFun(setTransition)
                .addFun(reset)
                .addType(smartGoToActivity)
                .addTypes(targetActivitiesClassList)
                .build();
        KotlinFile kotlinFile = KotlinFile.builder(PACKAGE_NAME, "SmartGo").addType(smartGo).build();
        kotlinFile.writeTo(filer);
    }

    private void createInjectors() throws Exception {
        for (Map.Entry<String, SmartGoEntity> entry : map.entrySet()) {
            String fullClassName = entry.getKey();
            String packageName = entry.getValue().packageName;
            String className = entry.getValue().className;
            FunSpec.Builder builder = FunSpec.builder("inject");
            builder.addModifiers(KModifier.OVERRIDE)
                    .addParameter("a", ClassName.bestGuess(fullClassName))
                    .addParameter("i", TypeNameKt.ANY.asNullable())
                    .addStatement("val intent = i as? %T ?: a.getIntent()", ClassName.bestGuess("android.content.Intent"));
            for (SmartGoEntity.FieldEntity field : entry.getValue().fields) {
                getExtras(builder, field);
            }
            TypeSpec typeSpec = TypeSpec.classBuilder(className + "_SmartGo")
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.bestGuess(SmartGoInjector.class.getCanonicalName()), ClassName.bestGuess(fullClassName)))
                    .addFun(builder.build())
                    .build();
            KotlinFile kotlinFile = KotlinFile.builder(packageName, className + "_SmartGo").addType(typeSpec).build();
            kotlinFile.writeTo(filer);
        }
    }

    private void getExtras(FunSpec.Builder builder, SmartGoEntity.FieldEntity field) {
        builder.addCode("if (intent.hasExtra(%S)) {\n", field.fieldValue);
        String[] typeArray = {"boolean", "byte", "short", "int", "long", "double", "float", "char"};
        if (Arrays.asList(typeArray).contains(field.fieldType)) {
            String statement = "  a.%s = intent.get%sExtra(\"%s\", %s)";
            String defaultValue = "";
            switch (field.fieldType) {
                case "int":
                case "long":
                case "byte":
                case "short":
                    defaultValue = "0";
                    break;
                case "double":
                    defaultValue = "0.0";
                    break;
                case "float":
                    defaultValue = "0f";
                    break;
                case "boolean":
                    defaultValue = "false";
                    break;
                case "char":
                    defaultValue = "'\0'";
                    break;
            }
            String extraType = field.fieldType.toUpperCase().substring(0, 1) + field.fieldType.substring(1, field.fieldType.length());
            builder.addStatement(String.format(statement, field.fieldName, extraType, field.fieldValue, defaultValue));
        } else {
            if (field.fieldType.contains("[]")) {
                String extraType = field.fieldType.replace("[]", "Array");
                String paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                if (Arrays.asList(typeArray).contains(extraType.replace("Array", ""))) {
                    //基本类型的数组
                    extraType = extraType.substring(0, 1).toUpperCase() + extraType.substring(1, extraType.length());
                } else {
                    String type = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length());
                    extraType = type.substring(0, 1).toUpperCase() + type.substring(1, type.length()).replace("[]", "Array");
                }
                if (extraType.contentEquals("ParcelableArray")) {
                    //ParcelableArray不能强转为其它类型数组，需要单独处理
                    ClassName originalTypeName = ClassName.bestGuess(field.originalType);
                    builder.addStatement("  val array = intent.getParcelableArrayExtra(%S)", field.fieldValue);
                    builder.addStatement("  val list = arrayListOf<%T>()", originalTypeName);
                    builder.addStatement("  array.forEach { list.add(it as %T) }", originalTypeName);
                    builder.addStatement("  a.%L = list.toTypedArray()", field.fieldName);
                } else {
                    builder.addStatement("  a.%L = intent.get%LExtra(%S)", field.fieldName, paramType + extraType, field.fieldValue);
                }
            } else {
                //ArrayList或非基本类型的Extra
                String[] params = {"Integer", "String", "CharSequence", ""};
                String extraType = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length());
                String paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length());
                if (!Arrays.asList(params).contains(paramType)) {
                    paramType = "Parcelable";
                }
                builder.addStatement("  a.%L = intent.get%LExtra(%S)", field.fieldName, paramType + extraType, field.fieldValue);
            }
        }
        builder.addCode("}\n");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(IntentExtra.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private TypeName getFieldType(SmartGoEntity.FieldEntity field) {
        TypeName typeName;
        switch (field.fieldType) {
            case "boolean":
                typeName = TypeNameKt.BOOLEAN;
                break;
            case "boolean[]":
                typeName = ClassName.get("kotlin", "BooleanArray");
                break;
            case "byte":
                typeName = TypeNameKt.BYTE;
                break;
            case "byte[]":
                typeName = ClassName.get("kotlin", "ByteArray");
                break;
            case "short":
                typeName = TypeNameKt.SHORT;
                break;
            case "short[]":
                typeName = ClassName.get("kotlin", "ShortArray");
                break;
            case "int":
                typeName = TypeNameKt.INT;
                break;
            case "int[]":
                typeName = ClassName.get("kotlin", "IntArray");
                break;
            case "long":
                typeName = TypeNameKt.LONG;
                break;
            case "long[]":
                typeName = ClassName.get("kotlin", "LongArray");
                break;
            case "char":
                typeName = TypeNameKt.CHAR;
                break;
            case "char[]":
                typeName = ClassName.get("kotlin", "CharArray");
                break;
            case "float":
                typeName = TypeNameKt.FLOAT;
                break;
            case "float[]":
                typeName = ClassName.get("kotlin", "FloatArray");
                break;
            case "double":
                typeName = TypeNameKt.DOUBLE;
                break;
            case "double[]":
                typeName = ClassName.get("kotlin", "DoubleArray");
                break;
            case "java.lang.CharSequence":
                typeName = ClassName.get("kotlin", "CharSequence");
                break;
            case "java.lang.CharSequence[]":
                typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.get("kotlin", "CharSequence"));
                break;
            case "java.lang.String":
                typeName = ClassName.get("kotlin", "String");
                break;
            case "java.lang.String[]":
                typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.get("kotlin", "String"));
                break;
            case "android.os.Parcelable":
                typeName = ClassName.bestGuess("android.os.Parcelable");
                break;
            case "android.os.Parcelable[]":
                typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.bestGuess("android.os.Parcelable"));
                break;
            case "android.os.Bundle":
                typeName = ClassName.bestGuess("android.os.Bundle");
                break;
            default:
                if (!field.fieldParam.isEmpty()) {
                    ClassName className = null;
                    switch (field.fieldParam) {
                        case "java.lang.Integer":
                            className = TypeNameKt.INT;
                            break;
                        case "java.lang.String":
                            className = ClassName.get("kotlin", "String");
                            break;
                        case "java.lang.CharSequence":
                            className = ClassName.get("kotlin", "CharSequence");
                            break;
                    }
                    if (field.fieldType.contentEquals("java.util.ArrayList")) {
                        field.fieldType = "kotlin.collections.ArrayList";
                    }
                    typeName = ParameterizedTypeName.get(ClassName.bestGuess(field.fieldType), className == null ? ClassName.bestGuess(field.fieldParam) : className);
                } else {
                    typeName = ClassName.bestGuess(field.fieldType);
                }
                break;
        }
        return typeName;
    }
}
