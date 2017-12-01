package pers.victor.smartgo;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by Victor on 24/11/2017. (ง •̀_•́)ง
 */

final class LoadPath {
    private static List<SmartPathInjector> services = new ArrayList<>();

    static {
        Iterator<SmartPathInjector> iterator = ServiceLoader.load(SmartPathInjector.class).iterator();
        while (iterator.hasNext()) {
            services.add(iterator.next());
        }
    }

    private LoadPath() {}

    static void goActivity() {
        String path = SmartPath.entity.path;
        if (SmartPath.cache.containsKey(path)) {
            Log.i(SmartPath.TAG, "Use cache: " + path);
            SmartPath.cache.get(path).goActivity(SmartPath.entity);
            return;
        }
        for (SmartPathInjector service : services) {
            if (service.getPath().equals(path)) {
                service.goActivity(SmartPath.entity);
                addCache(path, service);
                return;
            }
        }
        Log.e(SmartPath.TAG, "No path found: " + SmartPath.entity.path);
    }

    static <T> T newInstanceOrNull(String path) {
        if (SmartPath.cache.containsKey(path)) {
            Log.i(SmartPath.TAG, "Use cache: " + path);
            return SmartPath.cache.get(path).newInstance();
        }
        for (SmartPathInjector service : services) {
            if (service.getPath().equals(path)) {
                addCache(path, service);
                return service.newInstance();
            }
        }
        return null;
    }

    private static void addCache(String path, SmartPathInjector service) {
        SmartPath.cache.put(path, service);
    }
}
