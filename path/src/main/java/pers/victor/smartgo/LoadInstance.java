package pers.victor.smartgo;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by Victor on 24/11/2017. (ง •̀_•́)ง
 */

final class LoadInstance {
    private static Map<String, InstanceInjector> cache = new HashMap<>();
    private static List<InstanceInjector> services = new ArrayList<>();

    static {
        Iterator<InstanceInjector> iterator = ServiceLoader.load(InstanceInjector.class).iterator();
        while (iterator.hasNext()) {
            services.add(iterator.next());
        }
    }

    private LoadInstance() {}

    static <T> T newInstanceOrNull(String path) {
        if (cache.containsKey(path)) {
            Log.i(SmartPath.TAG, "Use cache: " + path);
            return cache.get(path).newInstance();
        }
        for (InstanceInjector service : services) {
            if (service.getPath().equals(path)) {
                addCache(path, service);
                return service.newInstance();
            }
        }
        return null;
    }

    private static void addCache(String path, InstanceInjector service) {
        cache.put(path, service);
    }
}
