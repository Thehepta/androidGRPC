//
// Created by thehepta on 2023/8/12.
//

#ifndef JVMTI_GLOBALS_H
#define JVMTI_GLOBALS_H





static constexpr bool GlobalsReturnSelf(bool self) { return self; }

#if defined(NDEBUG) && !defined(__CLION_IDE__)
static constexpr bool kIsDebugBuild = GlobalsReturnSelf(false);
#else
static constexpr bool kIsDebugBuild = GlobalsReturnSelf(true);
#endif


// If true, references within the heap are poisoned (negated).
#ifdef USE_HEAP_POISONING
static constexpr bool kPoisonHeapReferences = true;
#else
static constexpr bool kPoisonHeapReferences = false;
#endif


static constexpr size_t kObjectAlignmentShift = 3;

static constexpr size_t kBitsPerByte = 8;



static constexpr size_t kHeapReferenceSize = sizeof(uint32_t);

#endif //JVMTI_GLOBALS_H
