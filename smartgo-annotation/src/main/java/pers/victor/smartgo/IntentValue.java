package pers.victor.smartgo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Victor on 2017/2/3. (ง •̀_•́)ง
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface IntentValue {
    String value();
}
