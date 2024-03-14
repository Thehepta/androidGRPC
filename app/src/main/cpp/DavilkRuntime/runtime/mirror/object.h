//
// Created by thehepta on 2023/8/12.
//

#ifndef JVMTI_OBJECT_H
#define JVMTI_OBJECT_H

#include "runtime/macro.h"
#include "object_reference.h"
class MANAGED LOCKABLE Object {

public:
    // The number of vtable entries in java.lang.Object.

    // The Class representing the type of the object.
    HeapReference<void> klass_;
    // Monitor and hash code information.
    uint32_t monitor_;

};



#endif //JVMTI_OBJECT_H
