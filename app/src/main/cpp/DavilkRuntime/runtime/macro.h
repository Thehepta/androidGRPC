//
// Created by thehepta on 2023/8/12.
//

#pragma once

#include <mutex>

#if defined(HAVE_THREAD_SAFETY_ATTRIBUTES)
#define THREAD_ANNOTATION_ATTRIBUTE__(x) __attribute__((x))
#else
#define THREAD_ANNOTATION_ATTRIBUTE__(x)  // no-op
#endif

#define REQUIRES_SHARED(...) \
  THREAD_ANNOTATION_ATTRIBUTE__(requires_shared_capability(__VA_ARGS__))

#ifndef NDEBUG
#define ALWAYS_INLINE
#else
#define ALWAYS_INLINE  __attribute__ ((always_inline))
#endif


#define CAPABILITY(...)


#define PACKED(x) __attribute__ ((__aligned__(x), __packed__))

#define REQUIRES(...)


#define MANAGED PACKED(4)

#if !defined(DISALLOW_COPY_AND_ASSIGN)
#define DISALLOW_COPY_AND_ASSIGN(TypeName) \
  TypeName(const TypeName&) = delete;  \
  void operator=(const TypeName&) = delete
#endif


#define LOCKABLE CAPABILITY("mutex")


#define SHARED_LOCK_FUNCTION(...) THREAD_ANNOTATION_ATTRIBUTE__(shared_lock(__VA_ARGS__))

#define UNLOCK_FUNCTION(...) THREAD_ANNOTATION_ATTRIBUTE__(unlock(__VA_ARGS__))
