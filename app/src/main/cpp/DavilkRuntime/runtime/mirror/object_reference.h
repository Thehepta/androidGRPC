//
// Created by thehepta on 2023/8/12.
//

#ifndef JVMTI_OBJECT_REFERENCE_H
#define JVMTI_OBJECT_REFERENCE_H
#include <atomic>
#include "runtime/macro.h"
template<class MirrorType>
class MANAGED HeapReference {

private:
    std::atomic<uint32_t> reference_;

};



#endif //JVMTI_OBJECT_REFERENCE_H
