//
// Created by thehepta on 2023/8/13.
//



#pragma once
#include "runtime/macro.h"

#define NO_RETURN [[ noreturn ]]  // NOLINT[whitespace/braces] [5]
#define UNREACHABLE  __builtin_unreachable


#define DISALLOW_ALLOCATION() \
  public: \
    NO_RETURN ALWAYS_INLINE void operator delete(void*, size_t) { UNREACHABLE(); } \
    ALWAYS_INLINE void* operator new(size_t, void* ptr) noexcept { return ptr; } \
    ALWAYS_INLINE void operator delete(void*, void*) noexcept { } \
  private: \
    void* operator new(size_t) = delete


class ValueObject {
private:
    DISALLOW_ALLOCATION();
};
