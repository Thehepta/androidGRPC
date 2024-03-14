#include <jni.h>
#include <string>
#include "hideload/linker.h"
#include <android/log.h>


#define LOG_TAG "RMI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_hepta_rmi_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_hepta_rmi_ServiceHandle_memLoadApk(JNIEnv *env, jobject thiz, jbyteArray args) {

    jsize length = env->GetArrayLength( args);
    jbyte* elements = env->GetByteArrayElements(args, NULL);
    auto classloader = env->FindClass("java/lang/ClassLoader");

    unsigned char* result = (unsigned char*)elements;

    LOGE("Java_com_hepta_rmi_ServiceHandle_memLoadApk");
    jobject g_currentDexLoad = memhideLoadApkModule(env, result, length);
    jmethodID method_loadClass = env->GetMethodID(classloader,"loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
    jstring LoadEntry_cls = env->NewStringUTF("com.hepta.fridaload.LoadEntry");
    jobject LoadEntrycls_obj = env->CallObjectMethod(g_currentDexLoad,method_loadClass,LoadEntry_cls);
    jstring arg1 = env->NewStringUTF("load hiedapk direct call jni native methdo successful");
    jmethodID call_jni_method_mth = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "test", "(Ljava/lang/String;)V");
    jmethodID call_jni_method_mth2 = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "test2", "(Ljava/lang/String;)V");
    jmethodID call_jni_method_mth3 = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "test3", "(Ljava/lang/String;)V");
    jmethodID call_jni_method_mth4 = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "test4", "(Ljava/lang/String;)V");
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_jni_method_mth,arg1);
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_jni_method_mth2,arg1);
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_jni_method_mth3,arg1);
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_jni_method_mth4,arg1);

    jmethodID call_java_method_mth = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "test_java_call_native", "(Ljava/lang/String;)V");
    jstring arg2 = env->NewStringUTF("load hiedapk direct call java methdo successful");
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_java_method_mth,arg2);

    jmethodID call_text_java_method_mth = env->GetStaticMethodID(static_cast<jclass>(LoadEntrycls_obj), "text_java", "()V");
    jstring arg3 = env->NewStringUTF("load call java methdo successful");
    env->CallStaticVoidMethod(static_cast<jclass>(LoadEntrycls_obj), call_text_java_method_mth,arg3);
    return true;

}