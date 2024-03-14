//
// Created by thehepta on 2023/8/13.
//

#include "jvmti.h"
#include "RuntimePart.h"
#include "android/log.h"

#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_INFO, "JVMTI", __VA_ARGS__))


jvmtiEnv *CreateJvmtiEnv(JavaVM *vm) {
    jvmtiEnv *jvmti_env;
    jint result = vm->GetEnv((void **) &jvmti_env, JVMTI_VERSION_1_2);
    if (result != JNI_OK) {
        return nullptr;
    }

    return jvmti_env;

}

static jint JNICALL heapIterationCallback(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* user_data) {

    *tag_ptr = 1;   //设置tag，然后在下面获取？？
    return JVMTI_VISIT_OBJECTS;
}

void JNICALL ClassFileLoadHook(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jclass class_being_redefined, jobject loader, const char *name, jobject protection_domain, jint class_data_len, const unsigned char *class_data, jint *new_class_data_len, unsigned char **new_class_data) {
    // 在类加载时被调用
    // 获取类名等信息
}

void JNICALL MethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method) {
    // 在方法进入时被调用
    // 获取方法名和类名等信息
//    LOGV("%s","MethodEntry");

}
jclass hook;
//jmethodID hoomethod;
void JNICALL MethodExit(jvmtiEnv *jvmti_env, JNIEnv *jni_env, jthread thread, jmethodID method, jboolean was_popped_by_exception, jvalue return_value) {
    // 在方法退出时被调用
    // 获取方法名和类名等信息
//    LOGV("%s","MethodExit");
    char *methodName;
    char *sign;
    char *generic;
    jclass clazz;

    // 获取方法名，签名
    jvmti_env->GetMethodName(method, &methodName, &sign, &generic);

    jvmti_env->GetMethodDeclaringClass(method,&clazz);
//    jclass hook = jni_env->FindClass("android/util/Log");

    char *class_signature_ptr;
    jvmti_env->GetClassSignature(clazz, &class_signature_ptr, NULL);

    if(jni_env->IsSameObject(hook,clazz)){
        // 获取方法所在线程
//        jvmtiThreadInfo info;
//        jvmti_env->GetThreadInfo(thread, &info);
        LOGV("==========ART方法进入:%s-%s-%s",class_signature_ptr, methodName, sign);
    }
}


void LoadJvmTI(JNIEnv *env) {
    LOGV("%s","entry LoadJvmTI");
    hook = static_cast<jclass>(env->NewGlobalRef(env->FindClass("android/app/Activity")));
//    hoomethod = env->GetStaticMethodID(hook,"e","(Ljava/lang/String;Ljava/lang/String;)I");
    AndroidRunAPI* androidRunApi = AndroidRunAPI::getInstance();

    androidRunApi->ensurePluginLoaded = (bool (*)(void *, const char*,std::string*))resolve_elf_internal_symbol("libart.so","_ZN3art7Runtime18EnsurePluginLoadedEPKcPNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEE");


    auto (*SetJdwpAllowed)(bool) = reinterpret_cast<void (*)(bool)>(resolve_elf_internal_symbol("libart.so","_ZN3art3Dbg14SetJdwpAllowedEb"));

    auto (*setJavaDebuggable)(void *, bool) = reinterpret_cast<void (*)(void *, bool)>(resolve_elf_internal_symbol( "libart.so","_ZN3art7Runtime17SetJavaDebuggableEb"));


    if (SetJdwpAllowed != nullptr) {
        SetJdwpAllowed(true);
    } else{
        LOGV("SetJdwpAllowed  ==  null ");

    }


    JavaVM *javaVM;

    env->GetJavaVM(&javaVM);
    JavaVMExt *javaVMExt = (JavaVMExt *) javaVM;
    void *runtime = javaVMExt->runtime;
    constexpr const char* plugin_name = "libopenjdkjvmti.so";
    LOGV("stdout","%s","ensurePluginLoaded successful");
    std::string error_msg;

    if (setJavaDebuggable != nullptr) {
        setJavaDebuggable(runtime, true);
    }
    bool success = androidRunApi->ensurePluginLoaded(runtime,plugin_name,&error_msg);

    if(!success){
        LOGV("ensurePluginLoaded Failed");
    }


    LOGV("-1");

    jvmtiEnv *jvmti_env = CreateJvmtiEnv(javaVM);

    LOGV("0");

    jvmtiEventCallbacks callbacks;
    jvmtiCapabilities capabilities;

    LOGV("1");

    // 设置JVMTI能力
    memset(&capabilities, 0, sizeof(capabilities));
    LOGV("2");

    capabilities.can_generate_all_class_hook_events = 1;
    LOGV("3");

    capabilities.can_generate_method_entry_events = 1;
    capabilities.can_generate_method_exit_events = 1;

    jvmti_env->AddCapabilities( &capabilities);
    LOGV("4");


    // 设置JVMTI回调函数
    memset(&callbacks, 0, sizeof(callbacks));
    callbacks.ClassFileLoadHook = &ClassFileLoadHook;
    callbacks.MethodEntry = &MethodEntry;
    callbacks.MethodExit = &MethodExit;
    jvmti_env->SetEventCallbacks( &callbacks, sizeof(callbacks));



    jvmti_env->SetEventCallbacks(&callbacks, sizeof(callbacks));

    jvmti_env->SetEventNotificationMode( JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);
    jvmti_env->SetEventNotificationMode( JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL);
    jvmti_env->SetEventNotificationMode( JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, NULL);



//    printf("%s","ensurePluginLoaded successful");
//    int count = 0;
//    jvmtiHeapCallbacks callbacks;
//    (void)memset(&callbacks, 0, sizeof(callbacks));
//    callbacks.heap_iteration_callback = &heapIterationCallback;
//    jvmtiError error = jvmti_env->IterateThroughHeap(0, NULL, &callbacks, &count);
//
//    jlong tag = 1;
//    jobject* instances;
//
//    jvmti_env->GetObjectsWithTags( 1, &tag, &count, &instances, NULL);
//
//    if (instances != NULL) {
//        jobject result = *instances;
//        jvmti_env->Deallocate( (unsigned char*) instances);
//    }

}





