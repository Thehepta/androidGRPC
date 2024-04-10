package org.example;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class JNISignatureConverter {

    // 方法将Java参数类型转换为JNI签名
    // 方法将Java参数类型和返回类型转换为JNI签名
    public static String convertToJNISignature(Method method) {
        StringBuilder signature = new StringBuilder("(");

        // 处理参数类型
        for (Parameter parameter : method.getParameters()) {
            Class<?> type = parameter.getType();
            signature.append(getTypeSignature(type));
        }

        // 处理返回类型
        Class<?> returnType = method.getReturnType();
        signature.append(")").append(getTypeSignature(returnType));

        return signature.toString();
    }

    // 辅助方法，将Java类型转换为JNI签名
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

    public static void main(String[] args) {
        try {
            Method method = JNISignatureConverter.class.getDeclaredMethod("convertToJNISignature", Method.class);
            String jniSignature = convertToJNISignature(method);
            System.out.println("JNI Signature: " + jniSignature);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}