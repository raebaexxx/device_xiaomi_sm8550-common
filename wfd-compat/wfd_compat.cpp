/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Compatibility shim for the stock Xiaomi WFD blobs.
 *
 * The blobs were built against older libgui and libc++ versions and still
 * reference these symbols, which Android 16 no longer exports:
 *
 *   SurfaceComposerClient::Transaction::setDisplaySurface(sp<IBinder>, sp<Surface>)
 *   SurfaceComposerClient::createVirtualDisplay(..., uint32_t, float)
 *   std::__1::__hash_memory()
 */

#include <cstddef>
#include <cstdint>
#include <functional>
#include <string>

#include <binder/IBinder.h>
#include <gui/Surface.h>
#include <gui/SurfaceComposerClient.h>
#include <utils/StrongPointer.h>

#define WFD_COMPAT_EXPORT __attribute__((visibility("default")))

namespace std {
inline namespace __1 {

WFD_COMPAT_EXPORT size_t __hash_memory(const void* data, size_t size) noexcept {
    return __murmur2_or_cityhash<size_t>()(data, size);
}

}  // namespace __1
}  // namespace std

WFD_COMPAT_EXPORT android::status_t WFDCompat_setDisplaySurface(
        android::SurfaceComposerClient::Transaction* transaction,
        const android::sp<android::IBinder>& displayToken,
        const android::sp<android::Surface>& surface)
        asm("_ZN7android21SurfaceComposerClient11Transaction17setDisplaySurface"
            "ERKNS_2spINS_7IBinderEEERKNS2_INS_7SurfaceEEE");

android::status_t WFDCompat_setDisplaySurface(
        android::SurfaceComposerClient::Transaction* transaction,
        const android::sp<android::IBinder>& displayToken,
        const android::sp<android::Surface>& surface) {
    android::sp<android::IGraphicBufferProducer> producer;
    if (surface != nullptr) {
        producer = surface->getIGraphicBufferProducer();
    }
    return transaction->setDisplaySurface(displayToken, producer);
}

/*
 * The prebuilt blob passes an extra task id argument which the current
 * SurfaceFlinger API no longer accepts. It is not needed for WFD.
 */
WFD_COMPAT_EXPORT android::sp<android::IBinder> WFDCompat_createVirtualDisplay(
        const std::string& displayName,
        bool isSecure,
        bool optimizeForPower,
        const std::string& uniqueId,
        uint32_t /* systemUiTaskId */,
        float requestedRefreshRate)
        asm("_ZN7android21SurfaceComposerClient20createVirtualDisplay"
            "ERKNSt3__112basic_stringIcNS1_11char_traitsIcEENS1_9allocatorIcEEEEbbS9_jf");

android::sp<android::IBinder> WFDCompat_createVirtualDisplay(
        const std::string& displayName,
        bool isSecure,
        bool optimizeForPower,
        const std::string& uniqueId,
        uint32_t /* systemUiTaskId */,
        float requestedRefreshRate) {
    return android::SurfaceComposerClient::createVirtualDisplay(
            displayName, isSecure, optimizeForPower, uniqueId, requestedRefreshRate);
}
