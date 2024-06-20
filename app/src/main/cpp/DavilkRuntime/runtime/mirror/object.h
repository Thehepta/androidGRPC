//
// Created by thehepta on 2023/8/12.
//

#pragma once
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
