package pers.victor.smartgo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Path {
    String value();
}
