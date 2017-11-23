package pers.victor.smartgo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by Victor on 22/11/2017. (ง •̀_•́)ง
 */

public final class SmartPath {
    private static SmartPathEntity entity;

    private SmartPath() {}

    public static BuildProps from(Context context) {
        entity = new SmartPathEntity();
        entity.context = context;
        entity.intent = new Intent();
        return new BuildProps();
    }

    public static final class BuildProps {
        private BuildProps() {}

        public BuildProps setAnim(int enterAnimId, int exitAnimId) {
            entity.isTransition = true;
            entity.enterAnim = enterAnimId;
            entity.exitAnim = exitAnimId;
            return this;
        }

        public BuildProps addFlags(int flags) {
            ((Intent) entity.intent).addFlags(flags);
            return this;
        }

        public BuildExtras path(String path) {
            entity.path = path;
            return new BuildExtras();
        }
    }

    public static final class BuildExtras {
        private BuildExtras() {}

        public BuildExtras putBoolean(String name, boolean value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putBoolean(String name, boolean[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putInt(String name, int value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putInt(String name, int[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putInt(String name, ArrayList<Integer> value) {
            ((Intent) entity.intent).putIntegerArrayListExtra(name, value);
            return this;
        }

        public BuildExtras putFloat(String name, float value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putFloat(String name, float[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putDouble(String name, double value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putDouble(String name, double[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putChar(String name, char value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putChar(String name, char[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putShort(String name, short value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putShort(String name, short[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putLong(String name, long value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putLong(String name, long[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putByte(String name, byte value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putByte(String name, byte[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putCharSequence(String name, CharSequence value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putCharSequence(String name, CharSequence[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putCharSequence(String name, ArrayList<CharSequence> value) {
            ((Intent) entity.intent).putCharSequenceArrayListExtra(name, value);
            return this;
        }

        public BuildExtras putString(String name, String value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putString(String name, String[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putString(String name, ArrayList<String> value) {
            ((Intent) entity.intent).putStringArrayListExtra(name, value);
            return this;
        }

        public BuildExtras putParcelable(String name, Parcelable value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putParcelable(String name, Parcelable[] value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public BuildExtras putParcelable(String name, ArrayList<? extends Parcelable> value) {
            ((Intent) entity.intent).putParcelableArrayListExtra(name, value);
            return this;
        }

        public BuildExtras putBundle(String name, Bundle value) {
            ((Intent) entity.intent).putExtra(name, value);
            return this;
        }

        public void go() {
            ServiceLoader<SmartPathInjector> serviceLoader = ServiceLoader.load(SmartPathInjector.class);
            Iterator<SmartPathInjector> iterator = serviceLoader.iterator();
            while (iterator.hasNext()) {
                SmartPathInjector service = iterator.next();
                if (service.goPath(entity)) {
                    break;
                }
            }
            entity.context = null;
            entity.intent = null;
            entity = null;
        }

        public void go(int requestCode) {
            entity.isForResult = true;
            entity.requestCode = requestCode;
            go();
        }
    }
}
