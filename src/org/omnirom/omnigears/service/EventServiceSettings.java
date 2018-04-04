/*
 *  Copyright (C) 2018 The OmniROM Project
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

package org.omnirom.omnigears.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchIndexableResource;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import org.omnirom.omnigears.preference.AppMultiSelectListPreference;
import org.omnirom.omnigears.preference.ScrollAppsViewPreference;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventServiceSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    public static final String EVENTS_PREFERENCES_NAME = "event_service";

    public static final String EVENT_A2DP_CONNECT = "bt_a2dp_connect_app_list";
    public static final String EVENT_WIRED_HEADSET_CONNECT = "headset_connect_app_list";
    public static final String EVENT_SERVICE_ENABLED = "event_service_enabled";
    public static final String EVENT_MEDIA_PLAYER_START = "media_player_autostart";
    public static final String EVENT_MUSIC_ACTIVE = "media_player_music_active";
    public static final String EVENT_AUTORUN_SINGLE = "autorun_single_app";
    public static final String A2DP_APP_LIST = "a2dp_app_list";
    public static final String HEADSET_APP_LIST = "headset_app_list";

    // -- For backward compatibility
    public static final String OLD_EVENT_A2DP_CONNECT = "bt_a2dp_connect_app";
    public static final String OLD_EVENT_WIRED_HEADSET_CONNECT = "headset_connect_app";
    // -- End backward compatibility

    private AppMultiSelectListPreference mA2DPappSelect;
    private AppMultiSelectListPreference mWiredHeadsetAppSelect;
    private ScrollAppsViewPreference mA2DPApps;
    private ScrollAppsViewPreference mHeadsetApps;
    private SwitchPreference mEnable;
    private SwitchPreference mAutoStart;
    private SwitchPreference mMusicActive;
    private SwitchPreference mAutorun;
    private Handler mHandler = new Handler();
    private String mServiceRunning;
    private String mServiceStopped;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    private SharedPreferences getPrefs() {
        return getActivity().getSharedPreferences(EVENTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.event_service_settings);

        // -- For backward compatibility
        String old_value = getPrefs().getString(OLD_EVENT_A2DP_CONNECT, null);
        if (old_value != null) {
            fixOldPreference(OLD_EVENT_A2DP_CONNECT, EVENT_A2DP_CONNECT, old_value);
        }
        old_value = getPrefs().getString(OLD_EVENT_WIRED_HEADSET_CONNECT, null);
        if (old_value != null) {
            fixOldPreference(OLD_EVENT_WIRED_HEADSET_CONNECT, EVENT_WIRED_HEADSET_CONNECT, old_value);
        }
        // -- End backward compatibility

        mEnable = (SwitchPreference) findPreference(EVENT_SERVICE_ENABLED);
        mEnable.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_SERVICE_ENABLED, false));
        mEnable.setOnPreferenceChangeListener(this);
        mServiceRunning = getResources().getString(R.string.event_service_running);
        mServiceStopped = getResources().getString(R.string.event_service_stopped);
        mEnable.setSummary(isServiceRunning() ? mServiceRunning : mServiceStopped);

        mAutoStart = (SwitchPreference) findPreference(EVENT_MEDIA_PLAYER_START);
        mAutoStart.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_MEDIA_PLAYER_START, false));
        mAutoStart.setOnPreferenceChangeListener(this);

        mMusicActive = (SwitchPreference) findPreference(EVENT_MUSIC_ACTIVE);
        mMusicActive.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_MUSIC_ACTIVE, false));
        mMusicActive.setOnPreferenceChangeListener(this);

        mAutorun = (SwitchPreference) findPreference(EVENT_AUTORUN_SINGLE);
        mAutorun.setChecked(getPrefs().getBoolean(EventServiceSettings.EVENT_AUTORUN_SINGLE, true));
        mAutorun.setOnPreferenceChangeListener(this);

        mA2DPappSelect = (AppMultiSelectListPreference) findPreference(EVENT_A2DP_CONNECT);
        Set<String> value = getPrefs().getStringSet(EVENT_A2DP_CONNECT, null);
        if (value != null) mA2DPappSelect.setValues(value);
        mA2DPappSelect.setOnPreferenceChangeListener(this);

        mA2DPApps = (ScrollAppsViewPreference) findPreference(A2DP_APP_LIST);
        if (value == null) {
            mA2DPApps.setVisible(false);
        } else {
            mA2DPApps.setVisible(true);
            mA2DPApps.setValues(value);
        }

        mWiredHeadsetAppSelect = (AppMultiSelectListPreference) findPreference(EVENT_WIRED_HEADSET_CONNECT);
        value = getPrefs().getStringSet(EVENT_WIRED_HEADSET_CONNECT, null);
        if (value != null) mWiredHeadsetAppSelect.setValues(value);
        mWiredHeadsetAppSelect.setOnPreferenceChangeListener(this);

        mHeadsetApps = (ScrollAppsViewPreference) findPreference(HEADSET_APP_LIST);
        if (value == null) {
            mHeadsetApps.setVisible(false);
        } else {
            mHeadsetApps.setValues(value);
            mHeadsetApps.setVisible(true);
        }
    }

    private void fixOldPreference(String old_event, String new_event, String value) {
        Set<String> mValues = new HashSet<String>();
        mValues.add(value);
        getPrefs().edit().putStringSet(new_event, mValues).commit();
        getPrefs().edit().putString(old_event, null).commit();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mA2DPappSelect) {
            Set<String> value = (Set<String>) newValue;

            getPrefs().edit().putStringSet(EVENT_A2DP_CONNECT, value).commit();

            mA2DPApps.setVisible(false);
            if (value != null) {
                mA2DPApps.setValues(value);
                mA2DPApps.setVisible(true);
            }

            return true;
        } else if (preference == mWiredHeadsetAppSelect) {
            Set<String> value = (Set<String>) newValue;

            getPrefs().edit().putStringSet(EVENT_WIRED_HEADSET_CONNECT, value).commit();

            mHeadsetApps.setVisible(false);
            if (value != null) {
                mHeadsetApps.setValues(value);
                mHeadsetApps.setVisible(true);
            }

            return true;
        } else if (preference == mEnable) {
            boolean value = ((Boolean) newValue).booleanValue();
            if (value) {
                getActivity().startService(new Intent(getActivity(), EventService.class));
            } else {
                getActivity().stopService(new Intent(getActivity(), EventService.class));
            }
            getPrefs().edit().putBoolean(EVENT_SERVICE_ENABLED, value).commit();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mEnable.setSummary(isServiceRunning() ? mServiceRunning : mServiceStopped);
                    } catch (Exception e) {
                    }
                }
            }, 1000);
            return true;
        } else if (preference == mAutoStart) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_MEDIA_PLAYER_START, value).commit();
            return true;
        } else if (preference == mMusicActive) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_MUSIC_ACTIVE, value).commit();
            return true;
        } else if (preference == mAutorun) {
            boolean value = ((Boolean) newValue).booleanValue();
            getPrefs().edit().putBoolean(EVENT_AUTORUN_SINGLE, value).commit();
            return true;
        }
        return false;
    }

    private boolean isServiceRunning() {
        return EventService.isRunning();
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.event_service_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}
