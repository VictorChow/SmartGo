package pers.victor.smartgo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 2017/2/4. (ง •̀_•́)ง
 */

class SmartGoEntity {
    String packageName;
    String className;
    List<FieldEntity> fields = new ArrayList<>();

    static class FieldEntity {
        String fieldName;
        String fieldType;
        String fieldValue;
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
