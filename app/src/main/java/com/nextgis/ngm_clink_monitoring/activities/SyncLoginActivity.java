/*
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
 */

package com.nextgis.ngm_clink_monitoring.activities;

import android.accounts.Account;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import com.nextgis.maplibui.activity.NGWLoginActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.SyncLoginFragment;


public class SyncLoginActivity
        extends NGWLoginActivity
{
    @Override
    protected NGWLoginFragment getNewLoginFragment()
    {
        return new SyncLoginFragment();
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


    @Override
    protected void createView()
    {
        setContentView(R.layout.activity_sync_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.focl_login_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        NGWLoginFragment ngwLoginFragment = (NGWLoginFragment) fm.findFragmentByTag("SyncLogin");

        if (ngwLoginFragment == null) {
            ngwLoginFragment = getNewLoginFragment();
            ngwLoginFragment.setForNewAccount(mForNewAccount);
            ngwLoginFragment.setUrlText(mUrlText);
            ngwLoginFragment.setLoginText(mLoginText);
            ngwLoginFragment.setChangeAccountUrl(mChangeAccountUrl);
            ngwLoginFragment.setChangeAccountLogin(mChangeAccountLogin);
        }

        ngwLoginFragment.setOnAddAccountListener(this);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.focl_login_frame, ngwLoginFragment, "SyncLogin");
        ft.commit();
    }
}
