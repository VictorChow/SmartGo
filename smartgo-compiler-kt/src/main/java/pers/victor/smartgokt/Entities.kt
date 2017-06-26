package pers.victor.smartgokt


/**
 * Created by Victor on 2017/6/26. (ง •̀_•́)ง
 */

class SmartGoEntityKt {
    //包名
    lateinit var packageName: String
    //类名 e.g. MainActivity
    lateinit var className: String
    val fields = arrayListOf<FieldEntityKt>()
}

class FieldEntityKt {
    //activity里的属性名
    lateinit var fieldName: String
    //属性的类型 e.g. java.lang.String
    lateinit var fieldType: String
    //注解的value
    lateinit var fieldValue: String
    //转为Parcelable之前的类型
    var originalType = ""
    //泛型用
    var fieldParam = ""

    constructor()

    constructor(fieldName: String, fieldType: String, fieldValue: String) {
        this.fieldName = fieldName
        this.fieldType = fieldType
        this.fieldValue = fieldValue
    }
}
