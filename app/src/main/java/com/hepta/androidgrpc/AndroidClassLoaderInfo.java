package com.hepta.androidgrpc;

import android.os.Build;

import dalvik.system.DexClassLoader;
import dalvik.system.InMemoryDexClassLoader;
import dalvik.system.PathClassLoader;

public class AndroidClassLoaderInfo {

    String ClassType;
    String FilePatch;


    public void setClassType(String classType) {
        ClassType = classType;
    }

    public void setFilePatch(String filePatch) {
        FilePatch = filePatch;
    }
}
