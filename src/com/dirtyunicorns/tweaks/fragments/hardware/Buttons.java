/*
 * Copyright (C) 2017-2019 The Dirty Unicorns Project
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

package com.dirtyunicorns.tweaks.fragments.hardware;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.*;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.du.Utils;
import com.android.settingslib.search.SearchIndexable;

import android.text.TextUtils;
import android.view.View;
import com.dirtyunicorns.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

@SearchIndexable
public class Buttons extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String DOUBLE_TAP_POWER_FLASHLIGHT = "double_tap_power_flashlight";
    private static final String KEY_VOLUME_PANEL_ON_LEFT = "audio_panel_view_position";

    private ListPreference mDoubleTapPowerFlashlight;
    private SystemSettingSwitchPreference mVolumePanelOnLeft;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.buttons);
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mDoubleTapPowerFlashlight =
                (ListPreference) prefScreen.findPreference(DOUBLE_TAP_POWER_FLASHLIGHT);
        if (deviceHasFlashlight()) {
            mDoubleTapPowerFlashlight.setOnPreferenceChangeListener(this);
            mDoubleTapPowerFlashlight.setValue(Integer.toString(Settings.Secure.getInt(getContext()
                    .getContentResolver(), Settings.Secure.TORCH_POWER_BUTTON_GESTURE, 0)));
            mDoubleTapPowerFlashlight.setSummary(mDoubleTapPowerFlashlight.getEntry());
        } else {
            prefScreen.removePreference(mDoubleTapPowerFlashlight);
        }

        mVolumePanelOnLeft = (SystemSettingSwitchPreference) findPreference(KEY_VOLUME_PANEL_ON_LEFT);
        boolean volumePanelOnLeft = Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.AUDIO_PANEL_VIEW_POSITION, 0, UserHandle.USER_CURRENT) != 0;
        if (mVolumePanelOnLeft != null) {
            mVolumePanelOnLeft.setChecked(volumePanelOnLeft);
        }
        mVolumePanelOnLeft.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mDoubleTapPowerFlashlight) {
            int torchPowerButtonValue = Integer.parseInt((String) newValue);
            Settings.Secure.putInt(getContext().getContentResolver(),
                    Settings.Secure.TORCH_POWER_BUTTON_GESTURE, torchPowerButtonValue);
            int index = mDoubleTapPowerFlashlight.findIndexOfValue((String) newValue);
            mDoubleTapPowerFlashlight.setSummary(
                    mDoubleTapPowerFlashlight.getEntries()[index]);
            if (torchPowerButtonValue == 1) {
                // if doubletap for torch is enabled, switch off double tap for camera
                Settings.Secure.putInt(getContext().getContentResolver(),
                        Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                        1/*camera gesture is disabled when 1*/);
            }
            return true;
        } else if (preference == mVolumePanelOnLeft) {
            boolean value = (Boolean) newValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.AUDIO_PANEL_VIEW_POSITION,
                    value ? 1 : 0, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private boolean deviceHasFlashlight() {
        return Utils.deviceHasFlashlight(getContext());
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIRTYTWEAKS;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.buttons;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
        }
    };
}
