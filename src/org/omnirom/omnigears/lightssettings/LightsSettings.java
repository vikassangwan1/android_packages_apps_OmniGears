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

package org.omnirom.omnigears.lightssettings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;

import static android.provider.Settings.System.NOTIFICATION_LIGHT_PULSE;

public class LightsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ligths_settings";

     private boolean mChargingLedsEnabled;
     private boolean mNotificationLedsEnabled;

     private PreferenceCategory mChargingLedsCategory;
     private Preference mChargingLeds;
     private Preference mNotificationLeds;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lights_settings);

         mChargingLedsEnabled = (getResources().getBoolean(
                         com.android.internal.R.bool.config_intrusiveBatteryLed));
         mNotificationLedsEnabled = (getResources().getBoolean(
                         com.android.internal.R.bool.config_intrusiveNotificationLed));
         mChargingLedsCategory = (PreferenceCategory) findPreference("leds");
         mChargingLeds = (Preference) findPreference("charging_light");
         mNotificationLeds = (Preference) findPreference("notification_light");
         if (mChargingLeds != null && mNotificationLeds != null) {
             if (!mChargingLedsEnabled) {
                 mChargingLedsCategory.removePreference(mChargingLeds);
             } else if (!mNotificationLedsEnabled) {
                 mChargingLedsCategory.removePreference(mNotificationLeds);
            } else if (!mChargingLedsEnabled && !mNotificationLedsEnabled) {
                 getPreferenceScreen().removePreference(mChargingLedsCategory);
             }
         }
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    private static class SummaryProvider implements SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;

        private SummaryProvider(Context context, SummaryLoader loader) {
            mContext = context;
            mLoader = loader;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            boolean enabled = Settings.System.getInt(mContext.getContentResolver(),
                    NOTIFICATION_LIGHT_PULSE, 0) == 1;
            mLoader.setSummary(this, mContext.getString( enabled ? R.string.led_category_summary_on
                        : R.string.led_category_summary_off));
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

}
