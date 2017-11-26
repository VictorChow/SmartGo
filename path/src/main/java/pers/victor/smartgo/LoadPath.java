package pers.victor.smartgo;

import android.util.Log;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by Victor on 24/11/2017. (ง •̀_•́)ง
 */

final class LoadPath {

    static void goActivity() {
        String path = SmartPath.entity.path;
        if (SmartPath.pathCache.containsKey(path)) {
            Log.e(SmartPath.TAG, "USE CACHE : " + path);
            SmartPath.pathCache.get(path).goPath(SmartPath.entity);
            return;
        }
        Iterator<SmartPathInjector> iterator = getServices();
        while (iterator.hasNext()) {
            SmartPathInjector service = iterator.next();
            if (service.goPath(SmartPath.entity)) {
                addCache(path, service);
                return;
            }
        }
        Log.e(SmartPath.TAG, "No path found: " + SmartPath.entity.path);
    }

    static Object newInstanceOrNull(String path) {
        if (SmartPath.pathCache.containsKey(path)) {
            Log.e(SmartPath.TAG, "USE CACHE : " + path);
            return SmartPath.pathCache.get(path).newInstance(path);
        }
        try {
            Iterator<SmartPathInjector> iterator = getServices();
            while (iterator.hasNext()) {
                SmartPathInjector service = iterator.next();
                Object target = service.newInstance(path);
                if (target != null) {
                    addCache(path, service);
                    return target;
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

    private static void addCache(String path, SmartPathInjector service) {
        SmartPath.pathCache.put(path, service);
    }
}
