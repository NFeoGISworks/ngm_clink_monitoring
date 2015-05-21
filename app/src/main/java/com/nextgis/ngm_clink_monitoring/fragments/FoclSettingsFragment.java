/*******************************************************************************
 * Project:  NextGIS mobile apps for Compulink
 * Purpose:  Mobile GIS for Android
 * Authors:  Dmitry Baryshnikov (aka Bishop), polimax@mail.ru
 *           NikitaFeodonit, nfeodonit@yandex.com
 * *****************************************************************************
 * Copyright (C) 2014-2015 NextGIS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.nextgis.ngm_clink_monitoring.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.BaseAdapter;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.activities.FoclSettingsActivity;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;

import java.io.IOException;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FoclSettingsFragment
        extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        String settings = getArguments().getString(FoclSettingsConstantsUI.PREFS_SETTINGS);

        switch (settings) {
            case FoclSettingsConstantsUI.ACTION_PREFS_GENERAL:
                addPreferencesFromResource(R.xml.preferences_general);
                Preference dataPathPreference =
                        findPreference(FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH);
                FoclSettingsActivity.initDataPathPreference(
                        (PreferenceActivity) getActivity(), this, dataPathPreference);
                break;

            case FoclSettingsConstantsUI.ACTION_PREFS_MAP:
                addPreferencesFromResource(R.xml.preferences_map);
                break;
        }
    }


    @Override
    public void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data)
    {
        if (requestCode == FoclSettingsActivity.DATA_FOLDER_SELECT_CODE &&
                resultCode == Activity.RESULT_OK) {

            FoclSettingsActivity.changeDataFolder(
                    (FoclSettingsActivity) getActivity(), this, data.getExtras());
        }
    }


    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences,
            String key)
    {
        if (key.equals(FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH)) {
            GISApplication app = (GISApplication) getActivity().getApplication();

            Preference preference = findPreference(key);

            try {
                preference.setSummary(app.getDataPath());
            } catch (IOException e) {
                Log.d(Constants.TAG, e.getLocalizedMessage());
                preference.setSummary(e.getLocalizedMessage());
            }

            PreferenceScreen prefScr = (PreferenceScreen) findPreference(
                    FoclSettingsConstantsUI.KEY_PREF_GENERAL_ROOT);

            if (prefScr != null) {
                ((BaseAdapter) prefScr.getRootAdapter()).notifyDataSetChanged();
            }
        }
    }
}
