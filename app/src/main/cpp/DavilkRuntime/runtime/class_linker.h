//
// Created by thehepta on 2023/8/12.
//

#pragma once

#include "macro.h"
#include "obj_ptr.h"
#include "mirror/object.h"

#define OVERRIDE override

class ClassVisitor {
public:
    virtual ~ClassVisitor() {}
    // Return true to continue visiting.
    virtual bool operator()(ObjPtr<Object> klass) = 0;
};

class ClassLoaderVisitor {
public:
    virtual ~ClassLoaderVisitor() {}
    virtual void Visit(ObjPtr<Object> class_loader)
    REQUIRES_SHARED(Locks::classlinker_classes_lock_, Locks::mutator_lock_) = 0;
};

