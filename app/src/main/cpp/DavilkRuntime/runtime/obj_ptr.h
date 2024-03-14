//
// Created by thehepta on 2023/8/12.
//

#pragma once
#include "macro.h"
#include "globals.h"
#include <iosfwd>
#include <type_traits>
#define OBJPTR_INLINE __attribute__ ((always_inline))


constexpr bool kObjPtrPoisoning = kIsDebugBuild;
// It turns out that most of the performance overhead comes from copying. Don't validate for now.
// This defers finding stale ObjPtr objects until they are used.
constexpr bool kObjPtrPoisoningValidateOnCopy = false;
// Value type representing a pointer to a mirror::Object of type MirrorType
// Since the cookie is thread based, it is not safe to share an ObjPtr between threads.
template<class MirrorType>
class ObjPtr {
    static constexpr size_t kCookieShift = kHeapReferenceSize * kBitsPerByte - kObjectAlignmentShift;
    static constexpr size_t kCookieBits = sizeof(uintptr_t) * kBitsPerByte - kCookieShift;
    static constexpr uintptr_t kCookieMask = (static_cast<uintptr_t>(1u) << kCookieBits) - 1;
    static_assert(kCookieBits >= kObjectAlignmentShift,"must have a least kObjectAlignmentShift bits");
public:
    OBJPTR_INLINE ObjPtr() REQUIRES_SHARED(Locks::mutator_lock_) : reference_(0u) {}
    // Note: The following constructors allow implicit conversion. This simplifies code that uses
    //       them, e.g., for parameter passing. However, in general, implicit-conversion constructors
    //       are discouraged and detected by clang-tidy.
    OBJPTR_INLINE ObjPtr(std::nullptr_t)
    REQUIRES_SHARED(Locks::mutator_lock_)
            : reference_(0u) {}
    OBJPTR_INLINE ObjPtr& operator=(MirrorType* ptr) REQUIRES_SHARED(Locks::mutator_lock_);
    OBJPTR_INLINE void Assign(MirrorType* ptr) REQUIRES_SHARED(Locks::mutator_lock_);
    OBJPTR_INLINE MirrorType* operator->() const REQUIRES_SHARED(Locks::mutator_lock_);
    OBJPTR_INLINE bool IsNull() const {
        return reference_ == 0;
    }
    // Ptr makes sure that the object pointer is valid.
    OBJPTR_INLINE MirrorType* Ptr() const REQUIRES_SHARED(Locks::mutator_lock_) const {
        AssertValid();
        return PtrUnchecked();
    }
    OBJPTR_INLINE bool IsValid() const REQUIRES_SHARED(Locks::mutator_lock_);
    OBJPTR_INLINE void AssertValid() const REQUIRES_SHARED(Locks::mutator_lock_)const {
        if (kObjPtrPoisoning) {
            CHECK(IsValid()) << "Stale object pointer " << PtrUnchecked() << " , expected cookie "
                             << GetCurrentTrimedCookie() << " but got " << GetCookie();
        }
    }
    // Ptr unchecked does not check that object pointer is valid. Do not use if you can avoid it.
    OBJPTR_INLINE MirrorType* PtrUnchecked() const {
        if (kObjPtrPoisoning) {
            return reinterpret_cast<MirrorType*>(
                    static_cast<uintptr_t>(static_cast<uint32_t>(reference_ << kObjectAlignmentShift)));
        } else {
            return reinterpret_cast<MirrorType*>(reference_);
        }
    }
    // Static function to be friendly with null pointers.
    template <typename SourceType>
    static ObjPtr<MirrorType> DownCast(ObjPtr<SourceType> ptr) REQUIRES_SHARED(Locks::mutator_lock_);
    // Static function to be friendly with null pointers.
    template <typename SourceType>
    static ObjPtr<MirrorType> DownCast(SourceType* ptr) REQUIRES_SHARED(Locks::mutator_lock_);
private:
    // Trim off high bits of thread local cookie.
    OBJPTR_INLINE static uintptr_t GetCurrentTrimedCookie();
    OBJPTR_INLINE uintptr_t GetCookie() const {
        return reference_ >> kCookieShift;
    }
    OBJPTR_INLINE static uintptr_t Encode(MirrorType* ptr) REQUIRES_SHARED(Locks::mutator_lock_);
    // The encoded reference and cookie.
    uintptr_t reference_;
    template <class T> friend class ObjPtr;  // Required for reference_ access in copy cons/operator.
};