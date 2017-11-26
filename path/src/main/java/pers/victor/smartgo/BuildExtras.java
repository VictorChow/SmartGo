package pers.victor.smartgo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Victor on 23/11/2017. (ง •̀_•́)ง
 */

public final class BuildExtras {

    public BuildExtras putBoolean(String name, boolean value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putBoolean(String name, boolean[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putInt(String name, int value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putInt(String name, int[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putInt(String name, ArrayList<Integer> value) {
        ((Intent) SmartPath.entity.intent).putIntegerArrayListExtra(name, value);
        return this;
    }

    public BuildExtras putFloat(String name, float value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putFloat(String name, float[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putDouble(String name, double value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putDouble(String name, double[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putChar(String name, char value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putChar(String name, char[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putShort(String name, short value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putShort(String name, short[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putLong(String name, long value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putLong(String name, long[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putByte(String name, byte value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putByte(String name, byte[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putCharSequence(String name, CharSequence value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putCharSequence(String name, CharSequence[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putCharSequence(String name, ArrayList<CharSequence> value) {
        ((Intent) SmartPath.entity.intent).putCharSequenceArrayListExtra(name, value);
        return this;
    }

    public BuildExtras putString(String name, String value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putString(String name, String[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putString(String name, ArrayList<String> value) {
        ((Intent) SmartPath.entity.intent).putStringArrayListExtra(name, value);
        return this;
    }

    public BuildExtras putParcelable(String name, Parcelable value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putParcelable(String name, Parcelable[] value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public BuildExtras putParcelable(String name, ArrayList<? extends Parcelable> value) {
        ((Intent) SmartPath.entity.intent).putParcelableArrayListExtra(name, value);
        return this;
    }

    public BuildExtras putBundle(String name, Bundle value) {
        ((Intent) SmartPath.entity.intent).putExtra(name, value);
        return this;
    }

    public void go() {
        LoadPath.goActivity();
        SmartPath.entity.context = null;
        SmartPath.entity.intent = null;
        SmartPath.entity = null;
    }

    public void go(int requestCode) {
        SmartPath.entity.isForResult = true;
        SmartPath.entity.requestCode = requestCode;
        go();
    }
}