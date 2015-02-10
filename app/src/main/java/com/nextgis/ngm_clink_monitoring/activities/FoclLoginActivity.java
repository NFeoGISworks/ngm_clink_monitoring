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
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import com.nextgis.maplibui.NGWLoginActivity;
import com.nextgis.maplibui.util.SettingsConstants;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class FoclLoginActivity
        extends NGWLoginActivity
{
    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);

        View view = getSupportFragmentManager().findFragmentById(R.id.login_frame).getView();

        if (view != null) {
            ((EditText) view.findViewById(com.nextgis.maplibui.R.id.url)).setText(
                    FoclConstants.FOCL_DEFAULT_ACCOUNT_URL);
        }
    }


    @Override
    public void onTokenReceived(
            String accountName,
            String url,
            String login,
            String password,
            String token)
    {
        accountName = FoclConstants.FOCL_ACCOUNT_NAME;

        super.onTokenReceived(accountName, url, login, password, token);

        GISApplication app = (GISApplication) getApplicationContext();
        Account account = app.getAccount();

        if (null != account) {
            app.addFoclProject();

            ContentResolver.setSyncAutomatically(account, app.getAuthority(), true);
            ContentResolver.addPeriodicSync(
                    account, app.getAuthority(), Bundle.EMPTY, FoclConstants.DEFAULT_SYNC_PERIOD);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putLong(
                            SettingsConstants.KEY_PREF_SYNC_PERIOD_LONG,
                            FoclConstants.DEFAULT_SYNC_PERIOD)
                    .commit();
        }
    }
}
