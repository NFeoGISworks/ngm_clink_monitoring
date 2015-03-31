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

package com.nextgis.ngm_clink_monitoring.activities;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplibui.NGWSettingsActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;

import java.util.List;

import static com.nextgis.maplibui.util.SettingsConstantsUI.KEY_PREF_SYNC_PERIOD;


public class NGWSettingsActivityProxy
        extends NGWSettingsActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        GISApplication app = (GISApplication) getApplication();
        setOnDeleteAccountListener(app);
    }


    @Override
    protected void fillPreferences(PreferenceScreen screen)
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();
        Preference preference = new Preference(this);

        if (null != account) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);
            Intent intent = new Intent(this, NGWSettingsActivity.class);
            intent.putExtras(bundle);
            intent.setAction(ACCOUNT_ACTION);

            preference.setIntent(intent);
            preference.setTitle(account.name);

        } else {
            //add "Add account" preference
            Intent intent = new Intent(this, FoclLoginActivity.class);

            preference.setIntent(intent);
            preference.setTitle(com.nextgis.maplibui.R.string.add_account);
            preference.setSummary(com.nextgis.maplibui.R.string.add_account_summary);
        }

        screen.addPreference(preference);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();
        Header header = new Header();

        if (null != account) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);

            header.title = account.name;
            header.fragment = com.nextgis.maplibui.NGWSettingsFragment.class.getName();
            header.fragmentArguments = bundle;

        } else {
            //add "Add account" header
            header.title = getString(com.nextgis.maplibui.R.string.add_account);
            header.summary = getString(com.nextgis.maplibui.R.string.add_account_summary);
            header.intent = new Intent(this, FoclLoginActivity.class);
        }

        target.add(header);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return super.isValidFragment(fragmentName);
    }


    @Override
    protected boolean isAccountSyncEnabled(
            Account account,
            String authority)
    {
        GISApplication app = (GISApplication) getApplication();
        return app.isAutoSyncEnabled();
    }


    @Override
    protected void setAccountSyncEnabled(
            Account account,
            String authority,
            boolean isEnabled)
    {
        GISApplication app = (GISApplication) getApplication();
        app.setAutoSyncEnabled(isEnabled);
    }


    @Override
    protected void addPeriodicSyncTime(
            final Account account,
            final IGISApplication application,
            PreferenceCategory syncCategory)
    {
        final GISApplication app = (GISApplication) application;
        String prefValue = "" + app.getSyncPeriodSec();

        final CharSequence[] keys = {
                getString(com.nextgis.maplibui.R.string.five_minutes),
                getString(com.nextgis.maplibui.R.string.ten_minutes),
                getString(com.nextgis.maplibui.R.string.fifteen_minutes),
                getString(com.nextgis.maplibui.R.string.thirty_minutes),
                getString(com.nextgis.maplibui.R.string.one_hour),
                getString(com.nextgis.maplibui.R.string.two_hours)};
        final CharSequence[] values = {
                "" + FoclConstants.DEFAULT_SYNC_PERIOD_SEC_LONG,
                "600",
                "900",
                "1800",
                "3600",
                "7200"};

        final ListPreference timeInterval = new ListPreference(this);
        timeInterval.setKey(KEY_PREF_SYNC_PERIOD);
        timeInterval.setTitle(com.nextgis.maplibui.R.string.sync_interval);
        timeInterval.setDialogTitle(com.nextgis.maplibui.R.string.sync_set_interval);
        timeInterval.setEntries(keys);
        timeInterval.setEntryValues(values);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(prefValue)) {
                timeInterval.setValueIndex(i);
                timeInterval.setSummary(keys[i]);
                break;
            }
        }

        timeInterval.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener()
                {
                    @Override
                    public boolean onPreferenceChange(
                            Preference preference,
                            Object newValue)
                    {
                        app.setSyncPeriodSec(Long.parseLong((String) newValue));

                        for (int i = 0; i < values.length; i++) {
                            if (values[i].equals(newValue)) {
                                timeInterval.setSummary(keys[i]);
                                break;
                            }
                        }

                        return true;
                    }
                });

        syncCategory.addPreference(timeInterval);
    }


    @Override
    protected void addAccountLayers(
            PreferenceScreen screen,
            Account account)
    {
        // We do not need to list of layers
    }
}
