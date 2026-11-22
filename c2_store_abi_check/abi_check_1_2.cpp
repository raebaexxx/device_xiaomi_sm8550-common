//
// SPDX-FileCopyrightText: The LineageOS Project
// SPDX-License-Identifier: Apache-2.0
//

#include <codec2/hidl/1.2/ComponentStore.h>

// RefBase is inherited virtually and so sits at the end of the object, where
// any growth moves mRefs past the allocation the QTI c2 blobs make with this
// size baked in. The blobs allocate 288 bytes (the A17 size), which also
// covers the smaller A16 object. Repatch the MOVZ immediate in extract-files.py
// if the object ever outgrows that allocation.
static_assert(
        sizeof(android::hardware::media::c2::V1_2::utils::ComponentStore) <= 288,
        "ComponentStore outgrew the QTI c2 blob allocation; repatch extract-files.py");