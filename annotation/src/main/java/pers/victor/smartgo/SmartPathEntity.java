package pers.victor.smartgo;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

public class SmartPathEntity {
    public Object context;
    public Object intent;
    public String path;
    public boolean isForResult;
    public int requestCode;
    public boolean isTransition;
    public int enterAnim;
    public int exitAnim;

    @Override
    public String toString() {
        return "SmartPathEntity{" +
                "context=" + context +
                ", intent=" + intent +
                ", path='" + path + '\'' +
                ", isForResult=" + isForResult +
                ", requestCode=" + requestCode +
                ", isTransition=" + isTransition +
                ", enterAnim=" + enterAnim +
                ", exitAnim=" + exitAnim +
                '}';
    }
}
