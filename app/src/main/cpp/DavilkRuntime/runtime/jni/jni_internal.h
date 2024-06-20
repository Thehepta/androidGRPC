//
// Created by thehepta on 2024/6/20.
//

#pragma once

#include <jni.h>
#include "../macro.h"

namespace art {

  class ArtField;
  class ArtMethod;

  const JNINativeInterface* GetJniNativeInterface();
  const JNINativeInterface* GetRuntimeShutdownNativeInterface();

  int ThrowNewException(JNIEnv* env, jclass exception_class, const char* msg, jobject cause);

  // Enables native stack checking for field and method resolutions via JNI. This should be called
  // during runtime initialization after libjavacore and libopenjdk have been dlopen()'ed.
  void JniInitializeNativeCallerCheck();

  // Removes native stack checking state.
  void JniShutdownNativeCallerCheck();

  namespace jni {

          static inline ArtField* DecodeArtField(jfieldID fid) {
              return reinterpret_cast<ArtField*>(fid);
            }

          static inline jfieldID EncodeArtField(ArtField* field) {
             return reinterpret_cast<jfieldID>(field);
           }

          static inline jmethodID EncodeArtMethod(ArtMethod* art_method) {
              return reinterpret_cast<jmethodID>(art_method);
            }

          static inline ArtMethod* DecodeArtMethod(jmethodID method_id) {
              return reinterpret_cast<ArtMethod*>(method_id);
            }

          }  // namespace jni
      }  // namespace art

std::ostream& operator<<(std::ostream& os, const jobjectRefType& rhs);