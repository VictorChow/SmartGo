package pers.victor.smartgo;

import android.util.Log;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by Victor on 24/11/2017. (ง •̀_•́)ง
 */

final class LoadPath {


    static void goActivity() {
        Iterator<SmartPathInjector> iterator = getServices();
        while (iterator.hasNext()) {
            SmartPathInjector service = iterator.next();
            if (service.goPath(SmartPath.entity)) {
                return;
            }
        }
        Log.e(SmartPath.TAG, "No path found: " + SmartPath.entity.path);
    }

    static Object newInstanceOrNull(String path) {
        try {
            Iterator<SmartPathInjector> iterator = getServices();
            while (iterator.hasNext()) {
                SmartPathInjector service = iterator.next();
                String originClass = service.getOriginClass(path);
                if (originClass != null) {
                    return Class.forName(originClass).newInstance();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Iterator<SmartPathInjector> getServices() {
        ServiceLoader<SmartPathInjector> serviceLoader = ServiceLoader.load(SmartPathInjector.class);
        return serviceLoader.iterator();
    }
}
