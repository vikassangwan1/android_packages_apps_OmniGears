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
package org.omnirom.omnigears.interfacesettings;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import org.omnirom.omnigears.TelephonyUtils;

import org.omnirom.omnigears.preference.CustomSeekBarPreference;

import java.util.List;
import java.util.ArrayList;

public class NotiSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
			
	private static final String INCALL_VIB_OPTIONS = "incall_vib_options";
	private static final String FLASH_ON_CALL_WAITING_DELAY = "flash_on_call_waiting_delay";

	private ListPreference mAnnoyingNotification;
	private CustomSeekBarPreference mFlashOnCallWaitingDelay;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.noti_settings);
        
        final PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

		mAnnoyingNotification = (ListPreference) findPreference("less_notification_sounds");
        mAnnoyingNotification.setOnPreferenceChangeListener(this);
        int threshold = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD,
                0, UserHandle.USER_CURRENT);
        mAnnoyingNotification.setValue(String.valueOf(threshold));
        mAnnoyingNotification.setSummary(mAnnoyingNotification.getEntry());
        
        PreferenceCategory incallVibCategory = (PreferenceCategory) findPreference(INCALL_VIB_OPTIONS);
        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefSet.removePreference(incallVibCategory);
        }
        
        mFlashOnCallWaitingDelay = (CustomSeekBarPreference) findPreference(FLASH_ON_CALL_WAITING_DELAY);
        mFlashOnCallWaitingDelay.setValue(Settings.System.getInt(resolver, Settings.System.FLASH_ON_CALLWAITING_DELAY, 200));
        mFlashOnCallWaitingDelay.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
		if (preference.equals(mAnnoyingNotification)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, mode, UserHandle.USER_CURRENT);
            int index = mAnnoyingNotification.findIndexOfValue((String) newValue);
            mAnnoyingNotification.setSummary(
                    mAnnoyingNotification.getEntries()[index]);
            return true;
        } else if (preference == mFlashOnCallWaitingDelay) {
            int val = (Integer) newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.FLASH_ON_CALLWAITING_DELAY, val);
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
                    sir.xmlResId = R.xml.noti_settings;
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
