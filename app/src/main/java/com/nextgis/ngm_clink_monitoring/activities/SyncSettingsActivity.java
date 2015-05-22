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
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplibui.NGWLoginActivity;
import com.nextgis.maplibui.NGWSettingsActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.SyncSettingsFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;

import static com.nextgis.maplibui.util.SettingsConstantsUI.KEY_PREF_SYNC_PERIOD;


public class SyncSettingsActivity
        extends NGWSettingsActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // without headers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = getIntent();
            intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
            intent.putExtra(
                    PreferenceActivity.EXTRA_SHOW_FRAGMENT, SyncSettingsFragment.class.getName());
        }

        super.onCreate(savedInstanceState);

        GISApplication app = (GISApplication) getApplication();
        setOnDeleteAccountListener(app);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return SyncSettingsFragment.class.getName().equals(fragmentName);
    }


    @Override
    protected void setStrings()
    {
        mDeleteAccountSummary = getString(R.string.delete_account_summary);
        mDeleteAccountWarnMsg = getString(R.string.delete_account_warn_msg);
    }


    @Override
    protected void createView()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            GISApplication app = (GISApplication) getApplication();
            Account account = app.getAccount();

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
            fillAccountPreferences(screen, account);

            setPreferenceScreen(screen);
        }
    }


    @Override
    public void fillAccountPreferences(
            PreferenceScreen screen,
            Account account)
    {
        final IGISApplication application = (IGISApplication) getApplicationContext();

        // add sync settings group
        PreferenceCategory syncCategory = new PreferenceCategory(this);
        syncCategory.setTitle(com.nextgis.maplibui.R.string.sync);
        screen.addPreference(syncCategory);

        // add auto sync property
        addAutoSyncProperty(account, application, syncCategory);

        // add time for periodic sync
        addPeriodicSyncTime(account, application, syncCategory);

        // add actions group
        PreferenceCategory actionCategory = new PreferenceCategory(this);
        actionCategory.setTitle(com.nextgis.maplibui.R.string.actions);
        screen.addPreference(actionCategory);

        if (null == account) {
            // add "Add account" action
            addAddAccountAction(actionCategory);

        } else {
            // add "Edit account" action
            addEditAccountAction(account, actionCategory);

            // add "Delete account" action
            addDeleteAccountAction(account, actionCategory);
        }
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


    protected void addAddAccountAction(PreferenceCategory actionCategory)
    {
        Preference preference = new Preference(this);
        preference.setTitle(com.nextgis.maplibui.R.string.add_account);
        preference.setSummary(com.nextgis.maplibui.R.string.add_account_summary);

        Intent intent = new Intent(this, SyncLoginActivity.class);
        intent.putExtra(NGWLoginActivity.FOR_NEW_ACCOUNT, true);
        preference.setIntent(intent);

        actionCategory.addPreference(preference);
    }


    @Override
    protected void addEditAccountAction(
            final Account account,
            PreferenceCategory actionCategory)
    {
        Preference preferenceEdit = new Preference(this);
        preferenceEdit.setTitle(R.string.edit_account);
        preferenceEdit.setSummary(R.string.edit_account_summary);

        AccountManager accountManager = AccountManager.get(getApplicationContext());
        String url = accountManager.getUserData(account, "url");
        String login = accountManager.getUserData(account, "login");

        Intent intent = new Intent(this, SyncLoginActivity.class);
        intent.putExtra(NGWLoginActivity.FOR_NEW_ACCOUNT, false);
        intent.putExtra(NGWLoginActivity.ACCOUNT_URL_TEXT, url);
        intent.putExtra(NGWLoginActivity.ACCOUNT_LOGIN_TEXT, login);
        intent.putExtra(NGWLoginActivity.CHANGE_ACCOUNT_URL, false);
        intent.putExtra(NGWLoginActivity.CHANGE_ACCOUNT_LOGIN, false);
        preferenceEdit.setIntent(intent);

        actionCategory.addPreference(preferenceEdit);
    }


    @Override
    protected void onDeleteAccount()
    {
        // do nothing
    }


    @Override
    public void onAccountsUpdated(Account[] accounts)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            GISApplication app = (GISApplication) getApplication();
            Account account = app.getAccount();

            PreferenceScreen screen = getPreferenceScreen();

            if (null != screen) {
                screen.removeAll();
                fillAccountPreferences(screen, account);
            }
        }
    }
}
