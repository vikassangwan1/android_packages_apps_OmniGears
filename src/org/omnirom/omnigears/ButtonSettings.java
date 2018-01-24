/*
 *  Copyright (C) 2017 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package org.omnirom.omnigears;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import java.util.List;
import java.util.ArrayList;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.util.omni.OmniSwitchConstants;
import com.android.internal.util.omni.PackageUtils;
import com.android.internal.util.omni.DeviceUtils;

public class ButtonSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {

    private static final String CATEGORY_KEYS = "button_keys";
    private static final String KEYS_SHOW_NAVBAR_KEY = "navigation_bar_show";
    private static final String KEYS_DISABLE_HW_KEY = "hardware_keys_disable";
    private static final String LONG_PRESS_RECENTS_ACTION = "long_press_recents_action";
    private static final String LONG_PRESS_HOME_ACTION = "long_press_home_action";
    private static final String DOUBLE_PRESS_HOME_ACTION = "double_press_home_action";

    private ListPreference mLongPressRecentsAction;
    private ListPreference mLongPressHomeAction;
    private ListPreference mDoublePressHomeAction;
    private SwitchPreference mEnableNavBar;
    private SwitchPreference mDisabkeHWKeys;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final PreferenceCategory keysCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_KEYS);

        if (deviceKeys == 0) {
            prefScreen.removePreference(keysCategory);
        } else {
            mEnableNavBar = (SwitchPreference) prefScreen.findPreference(
                   KEYS_SHOW_NAVBAR_KEY);

            mDisabkeHWKeys = (SwitchPreference) prefScreen.findPreference(
                    KEYS_DISABLE_HW_KEY);

            boolean showNavBarDefault = DeviceUtils.deviceSupportNavigationBar(getActivity());
            boolean showNavBar = Settings.System.getInt(resolver,
                        Settings.System.NAVIGATION_BAR_SHOW, showNavBarDefault ? 1:0) == 1;
            mEnableNavBar.setChecked(showNavBar);

            boolean harwareKeysDisable = Settings.System.getInt(resolver,
                        Settings.System.HARDWARE_KEYS_DISABLE, 0) == 1;
            mDisabkeHWKeys.setChecked(harwareKeysDisable);
        }


        mLongPressRecentsAction = (ListPreference) findPreference(LONG_PRESS_RECENTS_ACTION);
        int longPressRecentsAction = Settings.System.getInt(resolver,
                Settings.System.BUTTON_LONG_PRESS_RECENTS, 0);

        mLongPressRecentsAction.setValue(Integer.toString(longPressRecentsAction));
        mLongPressRecentsAction.setSummary(mLongPressRecentsAction.getEntry());
        mLongPressRecentsAction.setOnPreferenceChangeListener(this);

        // for navbar devices default is always assist LONG_PRESS_HOME_ASSIST = 2
        int defaultLongPressOnHomeBehavior = (deviceKeys == 0) ? 2 : getResources().getInteger(com.android.internal.R.integer.config_longPressOnHomeBehavior);
        mLongPressHomeAction = (ListPreference) findPreference(LONG_PRESS_HOME_ACTION);
        int longPressHomeAction = Settings.System.getInt(resolver,
                Settings.System.BUTTON_LONG_PRESS_HOME, defaultLongPressOnHomeBehavior);

        mLongPressHomeAction.setValue(Integer.toString(longPressHomeAction));
        mLongPressHomeAction.setSummary(mLongPressHomeAction.getEntry());
        mLongPressHomeAction.setOnPreferenceChangeListener(this);

        int defaultDoublePressOnHomeBehavior = getResources().getInteger(com.android.internal.R.integer.config_doubleTapOnHomeBehavior);
        mDoublePressHomeAction = (ListPreference) findPreference(DOUBLE_PRESS_HOME_ACTION);
        int doublePressHomeAction = Settings.System.getInt(resolver,
                Settings.System.BUTTON_DOUBLE_PRESS_HOME, defaultDoublePressOnHomeBehavior);

        mDoublePressHomeAction.setValue(Integer.toString(doublePressHomeAction));
        mDoublePressHomeAction.setSummary(mDoublePressHomeAction.getEntry());
        mDoublePressHomeAction.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mEnableNavBar) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, checked ? 1:0);
            // remove hw button disable if we disable navbar
            if (!checked) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.HARDWARE_KEYS_DISABLE, 0);
                mDisabkeHWKeys.setChecked(false);
            }
            return true;
        } else if (preference == mDisabkeHWKeys) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.HARDWARE_KEYS_DISABLE, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLongPressRecentsAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mLongPressRecentsAction.findIndexOfValue((String) newValue);
            mLongPressRecentsAction.setSummary(mLongPressRecentsAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.BUTTON_LONG_PRESS_RECENTS, value);
            return true;
        } else if (preference == mLongPressHomeAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mLongPressHomeAction.findIndexOfValue((String) newValue);
            mLongPressHomeAction.setSummary(mLongPressHomeAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.BUTTON_LONG_PRESS_HOME, value);
            return true;
        } else if (preference == mDoublePressHomeAction) {
            int value = Integer.valueOf((String) newValue);
            int index = mDoublePressHomeAction.findIndexOfValue((String) newValue);
            mDoublePressHomeAction.setSummary(mDoublePressHomeAction.getEntries()[index]);
            Settings.System.putInt(getContentResolver(), Settings.System.BUTTON_DOUBLE_PRESS_HOME, value);
            return true;
        }
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.button_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    final Resources res = context.getResources();
                    final int deviceKeys = res.getInteger(
                            com.android.internal.R.integer.config_deviceHardwareKeys);

                    if (deviceKeys == 0) {
                        result.add(CATEGORY_KEYS);
                    }
                    return result;
                }
            };
}
