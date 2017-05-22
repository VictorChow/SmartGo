package pers.victor.smartgokt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 2017/2/4. (ง •̀_•́)ง
 */

class SmartGoEntity {
    String packageName;//包名
    String className;//类名 e.g. MainActivity
    List<FieldEntity> fields = new ArrayList<>();

    static class FieldEntity {
        String fieldName;//activity里的属性名
        String fieldType;//属性的类型 e.g. java.lang.String
        String fieldValue;//注解的value
        String originalType = "";//转为Parcelable之前的类型
        String fieldParam = "";//泛型用

        FieldEntity() {
        }

        FieldEntity(String fieldName, String fieldType, String fieldValue) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.fieldValue = fieldValue;
        }

    }
}
