//
// Created by thehepta on 2023/10/2.
//

#ifndef ANDJVMENV_DEX_FILE_H
#define ANDJVMENV_DEX_FILE_H

static constexpr size_t kSha1DigestSize = 20;
struct Header {
    uint8_t magic_[8] = {};
    uint32_t checksum_ = 0;  // See also location_checksum_
    uint8_t signature_[kSha1DigestSize] = {};
    uint32_t file_size_ = 0;  // size of entire file
    uint32_t header_size_ = 0;  // offset to start of next section
    uint32_t endian_tag_ = 0;
    uint32_t link_size_ = 0;  // unused
    uint32_t link_off_ = 0;  // unused
    uint32_t map_off_ = 0;  // map list offset from data_off_
    uint32_t string_ids_size_ = 0;  // number of StringIds
    uint32_t string_ids_off_ = 0;  // file offset of StringIds array
    uint32_t type_ids_size_ = 0;  // number of TypeIds, we don't support more than 65535
    uint32_t type_ids_off_ = 0;  // file offset of TypeIds array
    uint32_t proto_ids_size_ = 0;  // number of ProtoIds, we don't support more than 65535
    uint32_t proto_ids_off_ = 0;  // file offset of ProtoIds array
    uint32_t field_ids_size_ = 0;  // number of FieldIds
    uint32_t field_ids_off_ = 0;  // file offset of FieldIds array
    uint32_t method_ids_size_ = 0;  // number of MethodIds
    uint32_t method_ids_off_ = 0;  // file offset of MethodIds array
    uint32_t class_defs_size_ = 0;  // number of ClassDefs
    uint32_t class_defs_off_ = 0;  // file offset of ClassDef array
    uint32_t data_size_ = 0;  // size of data section
    uint32_t data_off_ = 0;  // file offset of data section

    // Decode the dex magic version
    uint32_t GetVersion() const;
};


class DexFile {
public:


    void * vptrAdree ;

    const uint8_t* const begin_;

    // The size of the underlying memory allocation in bytes.
    const size_t size_;

    // The base address of the data section (same as Begin() for standard dex).
    const uint8_t* const data_begin_;

    // Typically the dex file name when available, alternatively some identifying string.
    //
    // The ClassLinker will use this to match DexFiles the boot class
    // path to DexCache::GetLocation when loading from an image.
    const std::string location_;

    const uint32_t location_checksum_;

    // Points to the header section.
    const Header* const header_;

    const void* const string_ids_;

    // Points to the base of the type identifier list.
    const void* const type_ids_;

    // Points to the base of the field identifier list.
    const void* const field_ids_;

    // Points to the base of the method identifier list.
    const void* const method_ids_;

    // Points to the base of the prototype identifier list.
    const void* const proto_ids_;

    // Points to the base of the class definition list.
    const void* const class_defs_;

    // Points to the base of the method handles list.
    const void* method_handles_;

    // Number of elements in the method handles list.
    size_t num_method_handles_;


// The size of the data section.
    const size_t data_size_;
};
class OatFile {

};



#endif //ANDJVMENV_DEX_FILE_H
