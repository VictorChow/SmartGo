package pers.victor.smartgo;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

public final class SmartPath {
    static final String TAG = "SmartPath";
    static SmartPathEntity entity;
    static Map<String, SmartPathInjector> cache = new HashMap<>();

    private SmartPath() {}

    public static BuildProps from(Context context) {
        return new BuildProps(context);
    }

    public static <T> T createInstance(String path) {
        return LoadPath.newInstanceOrNull(path);
    }

}
