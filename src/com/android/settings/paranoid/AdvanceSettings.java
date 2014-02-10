/*
 * Copyright (C) 2012 The Mahdi Rom project
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

package com.android.settings.paranoid;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.CheckBoxPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.IWindowManager;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.DisplayInfo;
import android.view.WindowManager;


public class AdvanceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener{

    private static final String TAG = "AdvanceSettings";   

	private static final String STATUS_BAR_TRAFFIC = "status_bar_traffic";
    private static final String PREF_FLIP_QS_TILES = "flip_qs_tiles";
	private static final String SMART_PULLDOWN = "smart_pulldown";

	// Device types
    private static final int DEVICE_PHONE  = 0;
    private static final int DEVICE_HYBRID = 1;
    private static final int DEVICE_TABLET = 2;

	private CheckBoxPreference mStatusBarTraffic; 
    private CheckBoxPreference mFlipQsTiles;  
	private ListPreference mSmartPulldown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.advance_settings);   

		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

		mStatusBarTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC);
        int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
        intState = setStatusBarTrafficSummary(intState);
        mStatusBarTraffic.setChecked(intState > 0);
        mStatusBarTraffic.setOnPreferenceChangeListener(this);  

		mFlipQsTiles = (CheckBoxPreference) findPreference(PREF_FLIP_QS_TILES);
        mFlipQsTiles.setChecked(Settings.System.getInt(resolver,
                Settings.System.QUICK_SETTINGS_TILES_FLIP, 0) == 1);  
		mSmartPulldown = (ListPreference) findPreference(SMART_PULLDOWN);  

		if (isPhone(getActivity())) {
            int smartPulldown = Settings.System.getInt(resolver,
                    Settings.System.QS_SMART_PULLDOWN, 0);
            mSmartPulldown.setValue(String.valueOf(smartPulldown));
            updateSmartPulldownSummary(smartPulldown);
            mSmartPulldown.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mSmartPulldown);
        }  
    }  

	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver resolver = getContentResolver();
        if (preference == mFlipQsTiles) {
            Settings.System.putInt(resolver,
                    Settings.System.QUICK_SETTINGS_TILES_FLIP,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarTraffic) {

            // Increment the state and then update the label
            int intState = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, 0);
            intState++;
            intState = setStatusBarTrafficSummary(intState);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC, intState);
            if (intState > 1) {return false;}
        } else if (preference == mSmartPulldown) {
            int smartPulldown = Integer.valueOf((String) objValue);
            Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN,
                    smartPulldown);
            updateSmartPulldownSummary(smartPulldown);
        } else {
            return false;
        }
        return true;
    }

    private int setStatusBarTrafficSummary(int intState) {
        // These states must match com.android.systemui.statusbar.policy.Traffic
        if (intState == 1) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bits);
        } else if (intState == 2) {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_bytes);
        } else {
            mStatusBarTraffic.setSummary(R.string.show_network_speed_summary);
            return 0;
        }
        return intState;
    }

	private void updateSmartPulldownSummary(int i) {
        if (i == 0) {
            mSmartPulldown.setSummary(R.string.smart_pulldown_off);
        } else if (i == 1) {
            mSmartPulldown.setSummary(R.string.smart_pulldown_dismissable);
        } else if (i == 2) {
            mSmartPulldown.setSummary(R.string.smart_pulldown_persistent);
        }
    }

    private static int getScreenType(Context con) {
        WindowManager wm = (WindowManager) con.getSystemService(Context.WINDOW_SERVICE);
        DisplayInfo outDisplayInfo = new DisplayInfo();
        wm.getDefaultDisplay().getDisplayInfo(outDisplayInfo);
        int shortSize = Math.min(outDisplayInfo.logicalHeight, outDisplayInfo.logicalWidth);
        int shortSizeDp =
            shortSize * DisplayMetrics.DENSITY_DEFAULT / outDisplayInfo.logicalDensityDpi;
        if (shortSizeDp < 600) {
            return DEVICE_PHONE;
        } else if (shortSizeDp < 720) {
            return DEVICE_HYBRID;
        } else {
            return DEVICE_TABLET;
        }
    }

    public static boolean isPhone(Context con) {
        return getScreenType(con) == DEVICE_PHONE;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
