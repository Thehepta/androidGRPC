//
// Created by thehepta on 2023/8/9.
//

#ifndef JVMTI_RUNTIMEPART_H
#define JVMTI_RUNTIMEPART_H


#include <jni.h>
#include <string>
#include "elf_symbol_resolver.h"
#include "runtime/macro.h"
#include <mutex>
#include "runtime/thread_state.h"
#include "runtime/base/value_object.h"
#include "runtime/obj_ptr.h"
#include "runtime/mirror/object.h"



jobjectArray getClassLoaders(JNIEnv *env, jint targetSdkVersion);
jobjectArray getBaseDexClassLoaders(JNIEnv *env, jint targetSdkVersion);





















/*
 * class Runtime {
 * ...
 * gc::Heap* heap_;                <-- we need to find this
 * std::unique_ptr<ArenaPool> jit_arena_pool_;     <----- API level >= 24
 * std::unique_ptr<ArenaPool> arena_pool_;             __
 * std::unique_ptr<ArenaPool> low_4gb_arena_pool_; <--|__ API level >= 23
 * std::unique_ptr<LinearAlloc> linear_alloc_;         \_
 * size_t max_spins_before_thin_lock_inflation_;
 * MonitorList* monitor_list_;
 * MonitorPool* monitor_pool_;
 * ThreadList* thread_list_;        <--- and these
 * InternTable* intern_table_;      <--/
 * ClassLinker* class_linker_;      <-/
 * SignalCatcher* signal_catcher_;
 * SmallIrtAllocator* small_irt_allocator_; <------------ API level >= 33 or Android Tiramisu Developer Preview
 * std::unique_ptr<jni::JniIdManager> jni_id_manager_; <- API level >= 30 or Android R Developer Preview
 * bool use_tombstoned_traces_;     <-------------------- API level 27/28
 * std::string stack_trace_file_;   <-------------------- API level <= 28
 * JavaVMExt* java_vm_;             <-- so we find this then calculate our way backwards
 * ...
 * }
 */







struct JavaVMExt {
    void *functions;
    void *runtime;
};


struct JNIEnvExt {
    void*  self_;
    JavaVMExt*  vm_;
};



struct PartialRuntime {
    void* heap_;
    std::unique_ptr<void> jit_arena_pool_;
    std::unique_ptr<void> arena_pool_;
    std::unique_ptr<void> low_4gb_arena_pool_;
    std::unique_ptr<void> linear_alloc_;
    size_t max_spins_before_thin_lock_inflation_;
    void* monitor_list_;
    void* monitor_pool_;
    void* thread_list_;
    void* intern_table_;
    void* class_linker_;
    void* signal_catcher_;
};


struct AndroidRunAPI{
    void *runtime;
    PartialRuntime * partialRuntime;

    void (*VisitClassLoaders)(void *ClassLinker_this, void *visitor);
    jobject (*AddGlobalRef)(void *JavaVMExt_this, void *thread_self,ObjPtr<Object> obj);
    bool (*ensurePluginLoaded)(void *Runtime_this, const char* plugin_name, std::string* error_msg);


    static AndroidRunAPI * androidRunApi;

    static AndroidRunAPI *getInstance(){

        if(androidRunApi == nullptr){
            androidRunApi = new AndroidRunAPI();
        }
        return androidRunApi;
    }


};

//https://android.googlesource.com/platform/art/+/refs/tags/android-13.0.0_r69/runtime/runtime.h#1193
struct PartialRuntime13 : public PartialRuntime{


    void* small_irt_allocator_;
    std::unique_ptr<void> jni_id_manager_;
    std::unique_ptr<JavaVMExt> java_vm_;
};


//https://android.googlesource.com/platform/art/+/refs/tags/android-12.1.0_r26/runtime/runtime.h#1151

struct PartialRuntime12_1: public PartialRuntime {


    std::unique_ptr<void> jni_id_manager_;
    std::unique_ptr<JavaVMExt> java_vm_;

};


//https://android.googlesource.com/platform/art/+/refs/tags/android-12.1.0_r26/runtime/runtime.h#1151

struct PartialRuntime12: public PartialRuntime {


    std::unique_ptr<void> jni_id_manager_;
    std::unique_ptr<JavaVMExt> java_vm_;

};

// Android R: https://android.googlesource.com/platform/art/+/refs/tags/android-11.0.0_r3/runtime/runtime.h#1182
struct PartialRuntime11: public PartialRuntime {


    std::unique_ptr<void> jni_id_manager_;
    std::unique_ptr<JavaVMExt> java_vm_;

};
//https://android.googlesource.com/platform/art/+/refs/tags/android-10.0.0_r3/runtime/runtime.h#975
struct PartialRuntime10: public PartialRuntime {


    std::unique_ptr<JavaVMExt> java_vm_;

};

//https://android.googlesource.com/platform/art/+/refs/tags/android-9.0.0_r3/runtime/runtime.h#860
struct PartialRuntime9: public PartialRuntime {


    bool use_tombstoned_traces_;
    std::string stack_trace_file_;
    std::unique_ptr<JavaVMExt> java_vm_;

};

//https://android.googlesource.com/platform/art/+/refs/tags/android-8.1.0_r3/runtime/runtime.h#760
struct PartialRuntime8_1: public PartialRuntime {


    bool use_tombstoned_traces_;
    std::string stack_trace_file_;
    std::unique_ptr<JavaVMExt> java_vm_;

};

//https://android.googlesource.com/platform/art/+/refs/tags/android-8.0.0_r3/runtime/runtime.h#778
struct PartialRuntime8: public PartialRuntime {


    std::string stack_trace_file_;
    std::unique_ptr<JavaVMExt> java_vm_;

};



#endif //JVMTI_RUNTIMEPART_H
