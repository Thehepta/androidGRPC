//
// Created by thehepta on 2024/2/15.
//
#include "art_method.h"

#define UNLIKELY(x) __builtin_expect(!!(x), 0)

uint32_t DecodeUnsignedLeb128(const uint8_t** data) {
    const uint8_t* ptr = *data;
    int result = *(ptr++);
    if (UNLIKELY(result > 0x7f)) {
        int cur = *(ptr++);
        result = (result & 0x7f) | ((cur & 0x7f) << 7);
        if (cur > 0x7f) {
            cur = *(ptr++);
            result |= (cur & 0x7f) << 14;
            if (cur > 0x7f) {
                cur = *(ptr++);
                result |= (cur & 0x7f) << 21;
                if (cur > 0x7f) {
                    // Note: We don't check to see if cur is out of range here,
                    // meaning we tolerate garbage in the four high-order bits.
                    cur = *(ptr++);
                    result |= cur << 28;
                }
            }
        }
    }
    *data = ptr;
    return static_cast<uint32_t>(result);
}

static inline int32_t DecodeSignedLeb128(const uint8_t** data) {
    const uint8_t* ptr = *data;
    int32_t result = *(ptr++);
    if (result <= 0x7f) {
        result = (result << 25) >> 25;
    } else {
        int cur = *(ptr++);
        result = (result & 0x7f) | ((cur & 0x7f) << 7);
        if (cur <= 0x7f) {
            result = (result << 18) >> 18;
        } else {
            cur = *(ptr++);
            result |= (cur & 0x7f) << 14;
            if (cur <= 0x7f) {
                result = (result << 11) >> 11;
            } else {
                cur = *(ptr++);
                result |= (cur & 0x7f) << 21;
                if (cur <= 0x7f) {
                    result = (result << 4) >> 4;
                } else {
                    // Note: We don't check to see if cur is out of range here,
                    // meaning we tolerate garbage in the four high-order bits.
                    cur = *(ptr++);
                    result |= cur << 28;
                }
            }
        }
    }
    *data = ptr;
    return result;
}

uint8_t *codeitem_end(const uint8_t ** pData) {
    uint32_t encoded_catch_handler_list_size = DecodeUnsignedLeb128(pData);
    for (; encoded_catch_handler_list_size > 0; encoded_catch_handler_list_size--) {
        int32_t encoded_catch_handler =DecodeSignedLeb128(pData);      // struct sleb128 size
        int encoded_catch_handler_size = encoded_catch_handler;
        if (encoded_catch_handler <= 0) {
            encoded_catch_handler_size = -encoded_catch_handler;
        }
        for (; encoded_catch_handler_size > 0; encoded_catch_handler_size--) {
            DecodeUnsignedLeb128(pData);    //struct uleb128 type_idx
            DecodeUnsignedLeb128(pData);    // struct uleb128 addr
        }
        if (encoded_catch_handler <= 0) {
            DecodeUnsignedLeb128(pData);
        }
    }
    return (uint8_t *) (*pData);
}

uint8_t * get_encoded_catch_handler_list(CodeItem *code_item){

    uint16_t* insns_end_ = reinterpret_cast<uint16_t *>(&code_item->insns_[code_item->insns_size_in_code_units_]);
    uint8_t* padding_end =  (uint8_t*)((reinterpret_cast<uintptr_t>(insns_end_)+3)&~3);
    uint8_t* encoded_catch_handler_list_data = padding_end + code_item->tries_size_*8;
    return encoded_catch_handler_list_data;
}