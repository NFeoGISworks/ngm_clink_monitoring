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
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import com.nextgis.maplibui.NGWLoginActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.fragments.SyncLoginFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;


public class SyncLoginActivity
        extends NGWLoginActivity
{
    public static final String FOR_NEW_ACCOUNT    = "for_new_account";
    public static final String ACCOUNT_URL_TEXT   = "account_url_text";
    public static final String ACCOUNT_LOGIN_TEXT = "account_login_text";

    protected boolean mForNewAccount = true;

    protected String mUrlText   = "";
    protected String mLoginText = "";


    @Override
    protected void onCreate(Bundle icicle)
    {
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(FOR_NEW_ACCOUNT)) {
                mForNewAccount = extras.getBoolean(FOR_NEW_ACCOUNT, true);
            }
            if (extras.containsKey(ACCOUNT_URL_TEXT)) {
                mUrlText = extras.getString(ACCOUNT_URL_TEXT);
            }
            if (extras.containsKey(ACCOUNT_LOGIN_TEXT)) {
                mLoginText = extras.getString(ACCOUNT_LOGIN_TEXT);
            }
        }

        super.onCreate(icicle);
    }


    @Override
    protected void createView()
    {
        setContentView(com.nextgis.maplibui.R.layout.activity_ngw_login);

        Toolbar toolbar = (Toolbar) findViewById(com.nextgis.maplibui.R.id.main_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        SyncLoginFragment syncLoginFragment = (SyncLoginFragment) fm.findFragmentByTag(
                FoclConstants.FRAGMENT_SYNC_LOGIN);

        if (syncLoginFragment == null) {
            syncLoginFragment = new SyncLoginFragment();
            syncLoginFragment.setForNewAccount(mForNewAccount);
            syncLoginFragment.setUrlText(mUrlText);
            syncLoginFragment.setLoginText(mLoginText);
        }

        syncLoginFragment.setOnAddAccountListener(this);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(
                com.nextgis.maplibui.R.id.login_frame, syncLoginFragment,
                FoclConstants.FRAGMENT_SYNC_LOGIN);
        ft.commit();
    }


    @Override
    public void onAddAccount(
            Account account,
            String token,
            boolean accountAdded)
    {
        super.onAddAccount(account, token, accountAdded);
        GISApplication app = (GISApplication) getApplication();
        app.onAddAccount(account, token, accountAdded);
    }
}
