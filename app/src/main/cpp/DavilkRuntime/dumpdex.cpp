#include <jni.h>
#include <string>
#include "RuntimePart.h"
#include "runtime/dex_file.h"
#include "vector"
#include "art_method.h"
#include "jvmti.h"
#include <map>
#include <android/log.h>
#include <fcntl.h>
#include <unistd.h>


#define LOGV(...)  ((void)__android_log_print(ANDROID_LOG_INFO, "DUMPDEX", __VA_ARGS__))

#define LIKELY(x) __builtin_expect(!!(x), 1)

inline static bool IsIndexId(jmethodID mid) { return ((reinterpret_cast<uintptr_t>(mid) % 2) != 0); }

static jfieldID field_art_method = nullptr;


jobjectArray getClassLoaderList(JNIEnv *env, jclass thiz) {

    return getClassLoaders(env,android_get_application_target_sdk_version());

}

jobjectArray getBaseDexClassLoaderList(JNIEnv *env, jclass thiz) {

    return getBaseDexClassLoaders(env,android_get_application_target_sdk_version());

}
CodeItem* (*GetCodeItem_fun)(void * this_ptr);
std::map<std::string, const DexFile*> dex_fd_maps;

template <typename Dest, typename Source>
inline Dest reinterpret_cast64(Source source) {

    return reinterpret_cast<Dest>(static_cast<uintptr_t>(source));
}

constexpr size_t kOatFileIndex = 0;
constexpr size_t kDexFileIndexStart = 1;
static bool ConvertJavaArrayToDexFiles(
        JNIEnv* env,
        jobject cookie_arrayObject,
        /*out*/ std::vector<const DexFile*>& dex_files,
        /*out*/ const OatFile*& oat_file) {
    jarray array = reinterpret_cast<jarray>(cookie_arrayObject);
    jsize array_size = env->GetArrayLength(array);
    if (env->ExceptionCheck() == JNI_TRUE) {
        return false;
    }

    // TODO: Optimize. On 32bit we can use an int array.
    jboolean is_long_data_copied;
    jlong* long_data = env->GetLongArrayElements(reinterpret_cast<jlongArray>(cookie_arrayObject),
                                                 &is_long_data_copied);
    if (env->ExceptionCheck() == JNI_TRUE) {
        return false;
    }

    oat_file = reinterpret_cast64<const OatFile*>(long_data[kOatFileIndex]);
    dex_files.reserve(array_size - 1);
    for (jsize i = kDexFileIndexStart; i < array_size; ++i) {
        dex_files.push_back(reinterpret_cast64<const DexFile*>(long_data[i]));
    }
    //b4000079b3af9740
    env->ReleaseLongArrayElements(reinterpret_cast<jlongArray>(array), long_data, JNI_ABORT);
    return env->ExceptionCheck() != JNI_TRUE;
}

jobject dumpDexBuffListByCookie(JNIEnv *env, jclass clazz, jlongArray cookie) {

    jclass arrayListClass = env->FindClass( "java/util/ArrayList");
    jmethodID arrayListInit = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = env->GetMethodID( arrayListClass, "add", "(Ljava/lang/Object;)Z");
    // 创建一个 ArrayList 实例
    jobject arrayList = env->NewObject( arrayListClass, arrayListInit);
    // 获取 byte[] 类和构造函数
    std::vector<const DexFile*> dex_files;
    const OatFile* oat_file;
    bool re = ConvertJavaArrayToDexFiles(env,cookie,dex_files,oat_file);
    if(!re){
        LOGV("dumpDexByCookie ConvertJavaArrayToDexFiles failed\n");
        return arrayList;
    }
    for(int i=0;i<dex_files.size();i++){
        const DexFile* dex_file = dex_files[i];
        if (dex_file != nullptr) {
            jbyteArray byteArray =env->NewByteArray( dex_file->size_);
            env->SetByteArrayRegion(byteArray, 0, dex_file->size_,reinterpret_cast<const jbyte *>(dex_file->begin_));
            env->CallBooleanMethod( arrayList, arrayListAdd, byteArray);
            env->DeleteLocalRef( byteArray);
        }
    }
    return arrayList;

}
void dumpDexToLocalByCookie(JNIEnv *env, jclass thiz, jlongArray cookie,jstring jdumpDir) {
    LOGV("dumpDexByCookie start\n");

    // TODO: implement dumpDexByCookie()
    std::vector<const DexFile*> dex_files;
    std::string dumpDir = env->GetStringUTFChars(jdumpDir, nullptr);
    const OatFile* oat_file;
    bool re = ConvertJavaArrayToDexFiles(env,cookie,dex_files,oat_file);
    for(int i=0;i<dex_files.size();i++){
        const DexFile* dex_file = dex_files[i];
        if (dex_file != nullptr) {
            char dumpdex_path[100]={0} ;
            sprintf(dumpdex_path,(dumpDir+"/%p.dex").c_str(),dex_file->begin_);\
                LOGV("dumpdex_path = %s\n",dumpdex_path);

            int dumpdex_fd = open(dumpdex_path, O_CREAT|O_RDWR,0644);
            if (dumpdex_fd < 0) {
                LOGV("Error opening file.\n");
                return;
            }
            size_t bytes_written = write(dumpdex_fd,dex_file->begin_,  dex_file->size_); // 将内存中的数据写入文件
            if (bytes_written != dex_file->size_) {
//                LOGV("Error writing to file.\n");
            } else {
//                LOGV("Data successfully written to file:%s.\n",dumpdex_path);
            }
            close(dumpdex_fd);
            dex_fd_maps.emplace(std::string(dumpdex_path),dex_file);
        }
    }
    LOGV("dumpDexByCookie end\n");
    return;
}


//Executable 的artMethod 可能是隐藏的，需要注意是否能获取
jfieldID getArtMethod_filed(JNIEnv *env){

    jclass clazz = env->FindClass("java/lang/reflect/Executable");
    auto field = env->GetFieldID(static_cast<jclass>(clazz), "artMethod", "J");
    if (env->ExceptionCheck()) {
        env->ExceptionDescribe();
        env->ExceptionClear();

    }
    return field;
}

//传入的是method索引
ArtMethod * GetArtMethod(JNIEnv *env, jclass clazz, jmethodID methodId) {

    if (android_get_device_api_level() >= __ANDROID_API_R__) {
        if (IsIndexId(methodId)) {
            jobject method = env->ToReflectedMethod(clazz, methodId, true);
            return reinterpret_cast<ArtMethod *>(env->GetLongField(method, field_art_method));
        }
    }
    return reinterpret_cast<ArtMethod *>(methodId);
}


//传入的不是method索引，而是method对象的索引
ArtMethod *GetArtMethod(JNIEnv *env, jclass clazz, jobject methodId) {

    if (android_get_device_api_level() >= __ANDROID_API_R__) {
        jfieldID field_art_method = getArtMethod_filed(env);
        return reinterpret_cast<ArtMethod *>(env->GetLongField(methodId, field_art_method));

    }
    return reinterpret_cast<ArtMethod *>(methodId);
}

bool WriteCodeItemToDexFileByArtMethod(ArtMethod* artMethod, uint32_t code_item_len){
    uint8_t *code_item = reinterpret_cast<uint8_t *>(GetCodeItem_fun(artMethod));
    uintptr_t code_item_addr = reinterpret_cast<uintptr_t>(code_item);
    for(auto dex_fd : dex_fd_maps){
        std::string dumpdex_path = dex_fd.first;
        const DexFile* dexFile = dex_fd.second;
        uintptr_t dex_begin = (uintptr_t) dexFile->begin_;
        uintptr_t dex_end = dex_begin + dexFile->size_;
        if( dex_begin < code_item_addr &&  code_item_addr <  dex_end){
            uintptr_t writ_off = code_item_addr - dex_begin;
            int dumpdex_fd = open(dumpdex_path.c_str(), O_RDWR,0644);
            lseek(dumpdex_fd,writ_off,SEEK_SET);
            write(dumpdex_fd,code_item,code_item_len);
            close(dumpdex_fd);
            LOGV("fix code_item successful dumpdex_path = %s code_item_len = %d  writ_off = %d ",dumpdex_path.c_str(),code_item_len,writ_off);
            return true;
        }
    }
    return false;
}


jbyteArray dumpMethod_code_item(JNIEnv *env,ArtMethod* artMethod){

    CodeItem *code_item = GetCodeItem_fun(artMethod);
    if (LIKELY(code_item != nullptr)) {
        uint32_t code_item_len = 0;
        uint8_t *item = (uint8_t *) code_item;
        if (code_item->tries_size_ > 0) {
            const uint8_t *handler_data  =  get_encoded_catch_handler_list(code_item);
            uint8_t *tail = codeitem_end(&handler_data);
            code_item_len =(int) (tail - item);
        } else {
            code_item_len = 16 + code_item->insns_size_in_code_units_ * 2;
        }

        uint8_t *code_item = reinterpret_cast<uint8_t *>(GetCodeItem_fun(artMethod));

        jbyteArray byteArray =env->NewByteArray( code_item_len);
        env->SetByteArrayRegion(byteArray, 0, code_item_len,reinterpret_cast<const jbyte *>(code_item));
        return byteArray;
    }
}
jbyteArray   dumpMethodByMember(JNIEnv *env, jclass cls, jobject method){

    ArtMethod* artMethod = GetArtMethod(env, cls, method);
    return dumpMethod_code_item(env,artMethod);
}


jmethodID NativeFindJmethod(JNIEnv *env,jclass cls, jstring jmethod_name,jstring jmethod_sign){
    const char* method_name = env->GetStringUTFChars(jmethod_name, nullptr);
    const char* method_sign = env->GetStringUTFChars(jmethod_sign, nullptr);
    jmethodID ret_jmethod =  env->GetStaticMethodID(cls,method_name,method_sign);
    //找不大方法需要清理异常，否则会崩溃
    if(ret_jmethod == nullptr){
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        ret_jmethod = env->GetMethodID(cls,method_name,method_sign);
        if(ret_jmethod == nullptr) {
            if (env->ExceptionCheck()) {
                env->ExceptionClear();
            }
        }
    }
    return ret_jmethod;
}

jbyteArray dumpMethodByString(JNIEnv *env, jclass thiz, jclass cls, jstring method_name,jstring method_sign){

    jmethodID findMethod = NativeFindJmethod(env,cls,method_name,method_sign);
    ArtMethod* artMethod = GetArtMethod(env, cls, findMethod);
    return dumpMethod_code_item(env,artMethod);
}


JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {

    JNIEnv* env;
    if (vm->GetEnv( (void**) &env, JNI_VERSION_1_6) != JNI_OK) {
        return -1;
    }

    GetCodeItem_fun = (CodeItem* (*)(void * ))resolve_elf_internal_symbol("libart.so","_ZN3art9ArtMethod11GetCodeItemEv");
    field_art_method = getArtMethod_filed(env);

    jclass classTest = env->FindClass("com/hepta/androidgrpc/dump");
    JNINativeMethod methods[]= {
            {"getBaseDexClassLoaderList", "()[Ljava/lang/ClassLoader;",(void*) getBaseDexClassLoaderList},
            {"getClassLoaderList", "()[Ljava/lang/ClassLoader;",(void*)getClassLoaderList},
            {"dumpMethodByMember", "(Ljava/lang/reflect/Member;)[B",(void*)dumpMethodByMember},
            {"dumpMethodByString", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;)[B",(void*)dumpMethodByString},
            {"dumpDexToLocalByCookie", "([JLjava/lang/String;)V", (void*)dumpDexToLocalByCookie},
            {"dumpDexBuffListByCookie", "([J)Ljava/util/List;", (void*)dumpDexBuffListByCookie},
    };
    env->RegisterNatives(classTest, methods, sizeof(methods)/sizeof(JNINativeMethod));



    return JNI_VERSION_1_6;
}



