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
 ******************************************************************************/

package com.nextgis.ngm_clink_monitoring.activities;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import com.nextgis.maplibui.NGWSettingsActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;

import java.util.List;


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
    protected void fillPreferences()
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();

        if (null != account) {
            Preference preference = new Preference(this);
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);
            Intent intent = new Intent(this, NGWSettingsActivity.class);
            intent.putExtras(bundle);
            intent.setAction(ACCOUNT_ACTION);

            preference.setIntent(intent);
            preference.setTitle(account.name);
            getPreferenceScreen().addPreference(preference);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();

        if (null != account) {
            Header header = new Header();
            Bundle bundle = new Bundle();
            bundle.putParcelable("account", account);

            header.title = account.name;
            header.fragment = com.nextgis.maplibui.NGWSettingsFragment.class.getName();
            header.fragmentArguments = bundle;
            target.add(header);
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return super.isValidFragment(fragmentName);
    }
}
