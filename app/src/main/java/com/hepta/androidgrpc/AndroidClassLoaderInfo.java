package com.hepta.androidgrpc;

import android.os.Build;

import dalvik.system.DexClassLoader;
import dalvik.system.InMemoryDexClassLoader;
import dalvik.system.PathClassLoader;

public class AndroidClassLoaderInfo {

    String ClassType = "null";
    String FilePatch = "null";

    long[] cookie;

    public void setClassType(String classType) {
        ClassType = classType;
    }

    public void setFilePatch(String filePatch) {
        FilePatch = filePatch;
    }

    public void setCookie(long[] cookie) {
        this.cookie = cookie;
    }
}
