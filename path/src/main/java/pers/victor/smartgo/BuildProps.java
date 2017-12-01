package pers.victor.smartgo;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Victor on 23/11/2017. (ง •̀_•́)ง
 */

public final class BuildProps {

    BuildProps(Context context) {
        SmartPath.entity = new SmartPathEntity();
        SmartPath.entity.context = context;
        SmartPath.entity.intent = new Intent();
    }

    public BuildProps setAnim(int enterAnimId, int exitAnimId) {
        SmartPath.entity.isTransition = true;
        SmartPath.entity.enterAnim = enterAnimId;
        SmartPath.entity.exitAnim = exitAnimId;
        return this;
    }

    public BuildProps addFlags(int flags) {
        ((Intent) SmartPath.entity.intent).addFlags(flags);
        return this;
    }

    public BuildExtras toPath(String path) {
        SmartPath.entity.path = path;
        return new BuildExtras();
    }
}
