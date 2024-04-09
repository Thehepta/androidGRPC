package com.hepta.androidgrpc;

import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class JNISignatureConverter {

    public static String convertToJNISignature(Method method) {
        StringBuilder signature = new StringBuilder("(");

        // 处理参数类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Parameter parameter : method.getParameters()) {
                Class<?> type = parameter.getType();
                signature.append(getTypeSignature(type));
            }
        }

        Class<?> returnType = method.getReturnType();
        signature.append(")").append(getTypeSignature(returnType));
        return signature.toString();
    }
    public static String convertToJNISignature(Constructor method) {
        StringBuilder signature = new StringBuilder("(");

        // 处理参数类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Parameter parameter : method.getParameters()) {
                Class<?> type = parameter.getType();
                signature.append(getTypeSignature(type));
            }
        }

        signature.append(")V");
        return signature.toString();
    }

    private static String getTypeSignature(Class<?> type) {
        Map<Class<?>, String> typeMap = new HashMap<>();
        typeMap.put(int.class, "I");
        typeMap.put(boolean.class, "Z");
        typeMap.put(byte.class, "B");
        typeMap.put(char.class, "C");
        typeMap.put(short.class, "S");
        typeMap.put(long.class, "J");
        typeMap.put(float.class, "F");
        typeMap.put(double.class, "D");
        typeMap.put(void.class, "V");

        if (type.isArray()) {
            return "[" + getTypeSignature(type.getComponentType());
        } else if (typeMap.containsKey(type)) {
            return typeMap.get(type);
        } else {
            return "L" + type.getName().replace(".", "/") + ";";
        }
    }
}