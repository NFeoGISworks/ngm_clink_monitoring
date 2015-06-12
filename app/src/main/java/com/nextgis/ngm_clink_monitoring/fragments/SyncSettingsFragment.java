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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OnAccountsUpdateListener;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import com.nextgis.maplibui.activity.NGWSettingsActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SyncSettingsFragment
        extends PreferenceFragment
        implements OnAccountsUpdateListener
{
    protected AccountManager mAccountManager;
    protected final Handler mHandler = new Handler();


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (null == mAccountManager) {
            mAccountManager = AccountManager.get(getActivity().getApplicationContext());
        }

        addPreferencesFromResource(com.nextgis.maplibui.R.xml.preferences_empty);
        createView();
    }


    protected void createView()
    {
        NGWSettingsActivity activity = (NGWSettingsActivity) getActivity();
        GISApplication app = (GISApplication) activity.getApplication();
        Account account = app.getAccount();
        activity.fillAccountPreferences(getPreferenceScreen(), account);
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (null != mAccountManager) {
            mAccountManager.addOnAccountsUpdatedListener(this, mHandler, true);
        }
    }


    @Override
    public void onPause()
    {
        if (null != mAccountManager) {
            mAccountManager.removeOnAccountsUpdatedListener(this);
        }
        super.onPause();
    }


    @Override
    public void onAccountsUpdated(Account[] accounts)
    {
        PreferenceScreen screen = getPreferenceScreen();

        if (null != screen) {
            screen.removeAll();
            createView();
        }
    }
}
