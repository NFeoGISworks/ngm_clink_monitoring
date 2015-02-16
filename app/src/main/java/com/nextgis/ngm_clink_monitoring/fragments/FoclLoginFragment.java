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

package com.nextgis.ngm_clink_monitoring.fragments;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextgis.maplibui.NGWLoginFragment;
import com.nextgis.maplibui.util.SettingsConstants;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class FoclLoginFragment
        extends NGWLoginFragment
{
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable
            ViewGroup container,
            @Nullable
            Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mURL.setText(FoclConstants.FOCL_DEFAULT_ACCOUNT_URL);
        return view;
    }


    @Override
    public void onTokenReceived(
            String accountName,
            String token)
    {
        accountName = FoclConstants.FOCL_ACCOUNT_NAME;
        super.onTokenReceived(accountName, token);

        if (!mAccountAlreadyExists) {
            GISApplication app = (GISApplication) getActivity().getApplicationContext();
            app.addFoclProject();

            Account account = app.getAccount();

            if (null == account) {
                return;
            }

            ContentResolver.setSyncAutomatically(account, app.getAuthority(), true);
            ContentResolver.addPeriodicSync(
                    account, app.getAuthority(), Bundle.EMPTY, FoclConstants.DEFAULT_SYNC_PERIOD);

            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putLong(
                            SettingsConstants.KEY_PREF_SYNC_PERIOD_LONG,
                            FoclConstants.DEFAULT_SYNC_PERIOD)
                    .commit();
        }
    }
}
