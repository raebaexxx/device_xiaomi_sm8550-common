/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaomi.settings.hypercharge;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.xiaomi.settings.Constants;

public class HyperChargeTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();
        boolean next = getQsTile().getState() != Tile.STATE_ACTIVE;
        getSharedPreferences("com.xiaomi.settings", MODE_PRIVATE)
                .edit()
                .putBoolean(Constants.KEY_HYPERCHARGE_STATUS, next)
                .apply();

        startService(new Intent(this, HyperChargeService.class)
                .setAction(HyperChargeService.ACTION_APPLY));

        refresh();
    }

    private void refresh() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean on = getSharedPreferences("com.xiaomi.settings", MODE_PRIVATE)
                .getBoolean(Constants.KEY_HYPERCHARGE_STATUS, false);
        tile.setState(on ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(com.xiaomi.settings.R.string.hypercharge_title));
        tile.updateTile();
    }
}