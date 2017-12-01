package pers.victor.smartgo;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

public interface InstanceInjector {
    <T> T newInstance();

    String getPath();
}
