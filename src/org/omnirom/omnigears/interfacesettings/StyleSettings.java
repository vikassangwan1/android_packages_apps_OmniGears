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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.SearchIndexableResource;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.List;
import java.util.ArrayList;

public class StyleSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {
    private static final String TAG = "StyleSettings";
    
    private static final String SYSTEMUI_THEME_STYLE = "systemui_theme_style";
    private static final String KEY_SHOW_DASHBOARD_COLUMNS = "show_dashboard_columns";
    private static final String KEY_HIDE_DASHBOARD_SUMMARY = "hide_dashboard_summary";
    private static final String KEY_SCREEN_OFF_ANIMATION = "screen_off_animation";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";

	private ListPreference mSystemUIThemeStyle;
    private SharedPreferences mAppPreferences;
    private ListPreference mScreenOffAnimation;
    private ListPreference mToastAnimation;
    
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.OMNI_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.style_settings);
        
        final ContentResolver resolver = getActivity().getContentResolver();

        mAppPreferences = getActivity().getSharedPreferences(SettingsActivity.APP_PREFERENCES_NAME,
                Context.MODE_PRIVATE);

        SwitchPreference showColumnsLayout = (SwitchPreference) findPreference(KEY_SHOW_DASHBOARD_COLUMNS);
        showColumnsLayout.setChecked(mAppPreferences.getInt(SettingsActivity.KEY_COLUMNS_COUNT, 1) == 2);
        showColumnsLayout.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue ) {
                    mAppPreferences.edit().putInt(SettingsActivity.KEY_COLUMNS_COUNT, 2).commit();
                } else {
                    mAppPreferences.edit().putInt(SettingsActivity.KEY_COLUMNS_COUNT, 1).commit();
                }
                return true;
            }
        });

        SwitchPreference hideColumnSummary = (SwitchPreference) findPreference(KEY_HIDE_DASHBOARD_SUMMARY);
        hideColumnSummary.setChecked(mAppPreferences.getBoolean(SettingsActivity.KEY_HIDE_SUMMARY, false));
        hideColumnSummary.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mAppPreferences.edit().putBoolean(SettingsActivity.KEY_HIDE_SUMMARY, ((Boolean) newValue)).commit();
                return true;
            }
        });
        
        mSystemUIThemeStyle = (ListPreference) findPreference(SYSTEMUI_THEME_STYLE);
        int systemUIThemeStyle = Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEM_UI_THEME, 0);
        int valueIndex = mSystemUIThemeStyle.findIndexOfValue(String.valueOf(systemUIThemeStyle));
        mSystemUIThemeStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mSystemUIThemeStyle.setSummary(mSystemUIThemeStyle.getEntry());
        mSystemUIThemeStyle.setOnPreferenceChangeListener(this);

        mScreenOffAnimation = (ListPreference) findPreference(KEY_SCREEN_OFF_ANIMATION);
        int screenOffAnimation = Settings.Global.getInt(getContentResolver(),
                Settings.Global.SCREEN_OFF_ANIMATION, 0);

        mScreenOffAnimation.setValue(Integer.toString(screenOffAnimation));
        mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntry());
        mScreenOffAnimation.setOnPreferenceChangeListener(this);
        
        mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.System.getInt(resolver, Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mScreenOffAnimation) {
            int value = Integer.valueOf((String) newValue);
            int index = mScreenOffAnimation.findIndexOfValue((String) newValue);
            mScreenOffAnimation.setSummary(mScreenOffAnimation.getEntries()[index]);
            Settings.Global.putInt(getContentResolver(), Settings.Global.SCREEN_OFF_ANIMATION, value);
            return true;
        } else if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) newValue);
            Settings.System.putString(resolver, Settings.System.TOAST_ANIMATION, (String) newValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(getActivity(), R.string.toast_animation_test,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (preference == mSystemUIThemeStyle) {
            String value = (String) newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.SYSTEM_UI_THEME, Integer.valueOf(value));
            int valueIndex = mSystemUIThemeStyle.findIndexOfValue(value);
            mSystemUIThemeStyle.setSummary(mSystemUIThemeStyle.getEntries()[valueIndex]);
            return true;
        }
        return false;
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        Settings.Global.putInt(resolver,
                Settings.Global.SYSTEM_DEFAULT_ANIMATION, 0);
    }
    
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.style_settings;
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
