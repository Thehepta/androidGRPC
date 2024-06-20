#include "obj_ptr.h"
#include "scoped_thread_state_change.h"

template<typename T>
  inline ObjPtr<T> ScopedObjectAccessAlreadyRunnable::Decode(jobject obj) const {
//        Locks::mutator_lock_->AssertSharedHeld(Self());
//        DCHECK(IsRunnable());  // Don't work with raw objects in non-runnable states.
//        return ObjPtr<T>::DownCast(Self()->DecodeJObject(obj));
      }