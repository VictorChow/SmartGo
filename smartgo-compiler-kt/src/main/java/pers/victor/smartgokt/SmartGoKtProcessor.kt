package pers.victor.smartgokt

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import pers.victor.smartgo.IntentExtra
import pers.victor.smartgo.SmartGoInjector
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Created by Victor on 2017/6/26. (ง •̀_•́)ง
 */

private val PACKAGE_NAME = "pers.victor.smartgokt"

@AutoService(Processor::class)
class SmartGoKtProcessor : AbstractProcessor() {
    private lateinit var filer: Filer
    private val map = hashMapOf<String, SmartGoEntityKt>()

    @Synchronized override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        for (element in roundEnvironment.getElementsAnnotatedWith(IntentExtra::class.java)) {
            if (element !is VariableElement) {
                return false
            }
            getEachVariableElement(element)
        }
        try {
            createSmartGo()
            createInjectors()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun getEachVariableElement(element: Element) {
        val variableElement = element as VariableElement
        val packageName = processingEnv.elementUtils.getPackageOf(variableElement).qualifiedName.toString()
        val fieldName = variableElement.simpleName.toString()
        val fieldType = variableElement.asType().toString()
        val className = variableElement.enclosingElement.simpleName.toString()
        val annotation = element.getAnnotation(IntentExtra::class.java)
        val fieldValue = if (annotation.value.isEmpty()) fieldName else annotation.value
        val canonicalClassName = packageName + "." + className
        val smartGoEntity: SmartGoEntityKt
        if (map.containsKey(canonicalClassName)) {
            smartGoEntity = map[canonicalClassName]!!
        } else {
            smartGoEntity = SmartGoEntityKt()
            smartGoEntity.packageName = packageName
            smartGoEntity.className = className
            map.put(canonicalClassName, smartGoEntity)
        }
        if (fieldType.contains("<") && fieldType.contains(">")) {
            val startIndex = fieldType.indexOf("<")
            val endIndex = fieldType.indexOf(">")
            val class1 = fieldType.substring(0, startIndex)
            val class2 = fieldType.substring(startIndex + 1, endIndex)
            val entity = FieldEntityKt()
            entity.fieldName = fieldName
            entity.fieldValue = fieldValue
            entity.fieldType = class1
            entity.fieldParam = class2
            smartGoEntity.fields.add(entity)
        } else {
            val typeArray = arrayOf("boolean", "boolean[]", "byte", "byte[]", "short", "short[]", "int", "int[]", "long", "long[]", "double", "double[]", "float", "float[]", "char", "char[]", "java.lang.CharSequence", "java.lang.CharSequence[]", "java.lang.String", "java.lang.String[]", "android.os.Bundle")
            if (fieldType in typeArray) {
                smartGoEntity.fields.add(FieldEntityKt(fieldName, fieldType, fieldValue))
            } else {
                val type = if (fieldType.contains("[]")) "android.os.Parcelable[]" else "android.os.Parcelable"
                val entity = FieldEntityKt(fieldName, type, fieldValue)
                entity.originalType = fieldType.replace("[]", "")
                smartGoEntity.fields.add(entity)
            }
        }
    }

    @Throws(Exception::class)
    private fun createSmartGo() {
        //SmartGo里GoToXXXActivity类列表
        val targetActivitiesClassList = arrayListOf<TypeSpec>()
        //SmartGo里GoToActivity类里的方法列表
        val goToActivitiesMethodList = arrayListOf<FunSpec>()
        for ((fullClassName, value) in map) {
            val className = value.className
            //SmartGo里GoToXXXActivity类里方法列表
            val targetActivitiesMethodList = arrayListOf<FunSpec>()
            for (field in value.fields) {
                val methodName = "set" + field.fieldValue.substring(0, 1).toUpperCase() + field.fieldValue.substring(1, field.fieldValue.length)
                //ArrayListExtra里的泛型
                var paramName = ""
                if (!field.fieldParam.isEmpty()) {
                    val paramSimpleName = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length)
                    when (paramSimpleName) {
                        "Integer" -> paramName = "IntegerArrayList"
                        "String" -> paramName = "StringArrayList"
                        "CharSequence" -> paramName = "CharSequenceArrayList"
                        else -> paramName = "ParcelableArrayList"
                    }
                }
                val method = FunSpec.builder(methodName)
                        .addParameter(field.fieldValue + "Extra", getFieldType(field))
                        .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                        .addStatement("intent!!.put%LExtra(%S, %L)", paramName, field.fieldValue, field.fieldValue + "Extra")
                        .addStatement("return this")
                        .build()
                targetActivitiesMethodList.add(method)
            }
            //SmartGo里GoToXXXActivity类的go()
            val go = FunSpec.builder("go")
                    .addStatement("gokt(%T::class.java)", ClassName.bestGuess(fullClassName))
                    .build()
            val goForResult = FunSpec.builder("go")
                    .addParameter("requestCode", INT)
                    .addStatement("gokt(%T::class.java, requestCode)", ClassName.bestGuess(fullClassName))
                    .build()
            val title = FunSpec.builder("setTitle")
                    .addParameter("titleExtra", ClassName.get("kotlin", "String"))
                    .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .addStatement("intent!!.putExtra(%S, %L)", "title", "titleExtra")
                    .addStatement("return this")
                    .build()
            //私有构造方法
            //SmartGo里GoToXXXActivity类
            val type = TypeSpec.classBuilder("To" + className)
                    .addFun(title)
                    .addFunctions(targetActivitiesMethodList)
                    .addFun(go)
                    .addFun(goForResult)
                    .build()
            //SmartGo里GoToActivity类里的方法
            val method = FunSpec.builder("to" + className)
                    .addKdoc("@see %T ←方便跳转\n", ClassName.bestGuess(fullClassName))
                    .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .addStatement("return %T()", ClassName.get(PACKAGE_NAME, "SmartGo", "To" + className))
                    .build()
            targetActivitiesClassList.add(type)
            goToActivitiesMethodList.add(method)
        }
        //SmartGo里的GoToActivity类里的addFlags()
        val addFlags = FunSpec.builder("addFlags")
                .addParameter("flags", INT)
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("intent!!.addFlags(flags)")
                .addStatement("return this")
                .build()
        //SmartGo里的GoToActivity类里的setAnim()
        val setAnim = FunSpec.builder("setAnim")
                .addParameter("enterAnimId", INT)
                .addParameter("exitAnimId", INT)
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("enterAnim = enterAnimId")
                .addStatement("exitAnim = exitAnimId")
                .addStatement("return this")
                .build()
        //SmartGo里的GoToActivity类
        val smartGoToActivity = TypeSpec.classBuilder("ToActivity")
                .addFun(setAnim)
                .addFun(addFlags)
                .addFunctions(goToActivitiesMethodList)
                .build()
        //SmartGo类里inject(activity)
        val inject = FunSpec.builder("inject")
                .addParameter("activity", ClassName.bestGuess("android.app.Activity"))
                .addStatement("inject(activity, null)")
                .build()
        //SmartGo类里inject(activity)
        val inject2 = FunSpec.builder("inject")
                .addParameter("activity", ClassName.bestGuess("android.app.Activity"))
                .addParameter("intent", ClassName.bestGuess("android.content.Intent").asNullable())
                .addCode("try {\n" +
                        "  val injectorName = activity::class.java.getCanonicalName() + \"_SmartGo\"\n" +
                        "  (Class.forName(injectorName).newInstance() as %T<%T>).inject(activity, intent)\n" +
                        "} catch (e: Exception) {\n" +
                        "  e.printStackTrace()\n" +
                        "}\n", SmartGoInjector::class.java, ClassName.bestGuess("android.app.Activity"))
                .build()
        //SmartGo类里from()
        val from = FunSpec.builder("from")
                .addParameter("ctx", ClassName.bestGuess("android.content.Context"))
                .returns(ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .addStatement("context = ctx")
                .addStatement("intent = Intent()")
                .addStatement("return %T()", ClassName.get(PACKAGE_NAME, "SmartGo", "ToActivity"))
                .build()
        //SmartGo类里go()
        val go = FunSpec.builder("gokt")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("clazz", ParameterizedTypeName.get(ClassName.bestGuess(Class::class.java.canonicalName), TypeVariableName.get("*")))
                .addStatement("intent!!.setClass(context, clazz)")
                .addStatement("context!!.startActivity(intent)")
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build()
        //SmartGo类里goForResult()
        val goForResult = FunSpec.builder("gokt")
                .addModifiers(KModifier.PRIVATE)
                .addParameter("clazz", ParameterizedTypeName.get(ClassName.bestGuess(Class::class.java.canonicalName), TypeVariableName.get("*")))
                .addParameter("requestCode", INT)
                .addStatement("intent!!.setClass(context, clazz)")
                .addCode("if (context !is %T) {\n" +
                        "  throw %T(\"非Activity的Context，不能startActivityForResult\")\n" +
                        "} else {\n" +
                        "  (context as Activity).startActivityForResult(intent, requestCode)\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("kotlin.IllegalArgumentException"))
                .addStatement("setTransition()")
                .addStatement("reset()")
                .build()
        //SmartGo类里reset()
        val reset = FunSpec.builder("reset")
                .addModifiers(KModifier.PRIVATE)
                .addStatement("intent = null")
                .addStatement("context = null")
                .addStatement("enterAnim = -1")
                .addStatement("exitAnim = -1")
                .build()
        //SmartGo类里setTransition()
        val setTransition = FunSpec.builder("setTransition")
                .addModifiers(KModifier.PRIVATE)
                .addCode("if(enterAnim < 0 || exitAnim < 0){\n" +
                        "  return\n" +
                        "}\n" +
                        "if (context !is %T) {\n" +
                        "  throw %T(\"非Activity的Context，不能overridePendingTransition\")\n" +
                        "} else {\n" +
                        "  (context as Activity).overridePendingTransition(enterAnim, exitAnim)\n" +
                        "}\n", ClassName.bestGuess("android.app.Activity"), ClassName.bestGuess("kotlin.IllegalArgumentException"))
                .build()
        //SmartGo类里enterAnim
        val enterAnim = PropertySpec.builder("enterAnim", INT, KModifier.PRIVATE)
                .mutable(true)
                .initializer("-1")
                .build()
        //SmartGo类里exitAnim
        val exitAnim = PropertySpec.builder("exitAnim", INT, KModifier.PRIVATE)
                .mutable(true)
                .initializer("-1")
                .build()
        //SmartGo类
        val propertySpecs = arrayListOf<PropertySpec>()
        propertySpecs.add(PropertySpec.builder("context", ClassName.bestGuess("android.content.Context").asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build())
        propertySpecs.add(PropertySpec.builder("intent", ClassName.bestGuess("android.content.Intent").asNullable(), KModifier.PRIVATE).mutable(true).initializer("null").build())
        propertySpecs.add(enterAnim)
        propertySpecs.add(exitAnim)
        val smartGo = TypeSpec.objectBuilder("SmartGo")
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
                .build()
        val kotlinFile = KotlinFile.builder(PACKAGE_NAME, "SmartGo").addType(smartGo).build()
        kotlinFile.writeTo(filer)
    }

    @Throws(Exception::class)
    private fun createInjectors() {
        for ((fullClassName, value) in map) {
            val packageName = value.packageName
            val className = value.className
            val builder = FunSpec.builder("inject")
            builder.addModifiers(KModifier.OVERRIDE)
                    .addParameter("a", ClassName.bestGuess(fullClassName))
                    .addParameter("i", ANY.asNullable())
                    .addStatement("val intent = i as? %T ?: a.getIntent()", ClassName.bestGuess("android.content.Intent"))
            for (field in value.fields) {
                getExtras(builder, field)
            }
            val typeSpec = TypeSpec.classBuilder(className + "_SmartGo")
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.bestGuess(SmartGoInjector::class.java.canonicalName), ClassName.bestGuess(fullClassName)))
                    .addFun(builder.build())
                    .build()
            val kotlinFile = KotlinFile.builder(packageName, className + "_SmartGo").addType(typeSpec).build()
            kotlinFile.writeTo(filer)
        }
    }

    private fun getExtras(builder: FunSpec.Builder, field: FieldEntityKt) {
        builder.addCode("if (intent.hasExtra(%S)) {\n", field.fieldValue)
        val typeArray = arrayOf("boolean", "byte", "short", "int", "long", "double", "float", "char")
        if (field.fieldType in typeArray) {
            val statement = "  a.%s = intent.get%sExtra(\"%s\", %s)"
            var defaultValue = ""
            when (field.fieldType) {
                "int", "long", "byte", "short" -> defaultValue = "0"
                "double" -> defaultValue = "0.0"
                "float" -> defaultValue = "0f"
                "boolean" -> defaultValue = "false"
                "char" -> defaultValue = "'\u0000'"
            }
            val extraType = field.fieldType.toUpperCase().substring(0, 1) + field.fieldType.substring(1, field.fieldType.length)
            builder.addStatement(String.format(statement, field.fieldName, extraType, field.fieldValue, defaultValue))
        } else {
            if (field.fieldType.contains("[]")) {
                var extraType = field.fieldType.replace("[]", "Array")
                val paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length)
                if (extraType.replace("Array", "") in typeArray) {
                    //基本类型的数组
                    extraType = extraType.substring(0, 1).toUpperCase() + extraType.substring(1, extraType.length)
                } else {
                    val type = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length)
                    extraType = type.substring(0, 1).toUpperCase() + type.substring(1, type.length).replace("[]", "Array")
                }
                if (extraType.contentEquals("ParcelableArray")) {
                    //ParcelableArray不能强转为其它类型数组，需要单独处理
                    val originalTypeName = ClassName.bestGuess(field.originalType)
                    builder.addStatement("  val array = intent.getParcelableArrayExtra(%S)", field.fieldValue)
                    builder.addStatement("  val list = arrayListOf<%T>()", originalTypeName)
                    builder.addStatement("  array.forEach { list.add(it as %T) }", originalTypeName)
                    builder.addStatement("  a.%L = list.toTypedArray()", field.fieldName)
                } else {
                    builder.addStatement("  a.%L = intent.get%LExtra(%S)", field.fieldName, paramType + extraType, field.fieldValue)
                }
            } else {
                //ArrayList或非基本类型的Extra
                val params = arrayOf("Integer", "String", "CharSequence", "")
                val extraType = field.fieldType.substring(field.fieldType.lastIndexOf(".") + 1, field.fieldType.length)
                var paramType = field.fieldParam.substring(field.fieldParam.lastIndexOf(".") + 1, field.fieldParam.length)
                if (paramType !in params) {
                    paramType = "Parcelable"
                }
                builder.addStatement("  a.%L = intent.get%LExtra(%S)", field.fieldName, paramType + extraType, field.fieldValue)
            }
        }
        builder.addCode("}\n")
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val set = linkedSetOf<String>()
        set.add(IntentExtra::class.java.canonicalName)
        return set
    }

    override fun getSupportedSourceVersion() = SourceVersion.latestSupported()!!

    private fun getFieldType(field: FieldEntityKt): TypeName {
        val typeName: TypeName
        when (field.fieldType) {
            "boolean" -> typeName = BOOLEAN
            "boolean[]" -> typeName = ClassName.get("kotlin", "BooleanArray")
            "byte" -> typeName = BYTE
            "byte[]" -> typeName = ClassName.get("kotlin", "ByteArray")
            "short" -> typeName = SHORT
            "short[]" -> typeName = ClassName.get("kotlin", "ShortArray")
            "int" -> typeName = INT
            "int[]" -> typeName = ClassName.get("kotlin", "IntArray")
            "long" -> typeName = LONG
            "long[]" -> typeName = ClassName.get("kotlin", "LongArray")
            "char" -> typeName = CHAR
            "char[]" -> typeName = ClassName.get("kotlin", "CharArray")
            "float" -> typeName = FLOAT
            "float[]" -> typeName = ClassName.get("kotlin", "FloatArray")
            "double" -> typeName = DOUBLE
            "double[]" -> typeName = ClassName.get("kotlin", "DoubleArray")
            "java.lang.CharSequence" -> typeName = ClassName.get("kotlin", "CharSequence")
            "java.lang.CharSequence[]" -> typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.get("kotlin", "CharSequence"))
            "java.lang.String" -> typeName = ClassName.get("kotlin", "String")
            "java.lang.String[]" -> typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.get("kotlin", "String"))
            "android.os.Parcelable" -> typeName = ClassName.bestGuess("android.os.Parcelable")
            "android.os.Parcelable[]" -> typeName = ParameterizedTypeName.get(ClassName.bestGuess("kotlin.Array"), ClassName.bestGuess("android.os.Parcelable"))
            "android.os.Bundle" -> typeName = ClassName.bestGuess("android.os.Bundle")
            else -> if (!field.fieldParam.isEmpty()) {
                var className: ClassName? = null
                when (field.fieldParam) {
                    "java.lang.Integer" -> className = INT
                    "java.lang.String" -> className = ClassName.get("kotlin", "String")
                    "java.lang.CharSequence" -> className = ClassName.get("kotlin", "CharSequence")
                }
                if (field.fieldType.contentEquals("java.util.ArrayList")) {
                    field.fieldType = "kotlin.collections.ArrayList"
                }
                typeName = ParameterizedTypeName.get(ClassName.bestGuess(field.fieldType), if (className == null) ClassName.bestGuess(field.fieldParam) else className)
            } else {
                typeName = ClassName.bestGuess(field.fieldType)
            }
        }
        return typeName
    }
}
