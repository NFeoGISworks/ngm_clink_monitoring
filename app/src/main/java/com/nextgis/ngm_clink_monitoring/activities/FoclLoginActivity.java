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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import com.nextgis.maplibui.NGWLoginActivity;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.fragments.FoclLoginFragment;


public class FoclLoginActivity
        extends NGWLoginActivity
{
    @Override
    protected void createView()
    {
        setContentView(com.nextgis.maplibui.R.layout.activity_ngw_login);

        Toolbar toolbar = (Toolbar) findViewById(com.nextgis.maplibui.R.id.main_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        FoclLoginFragment foclLoginFragment = (FoclLoginFragment) fm.findFragmentByTag("FoclLogin");

        if (foclLoginFragment == null) {
            foclLoginFragment = new FoclLoginFragment();
        }

        foclLoginFragment.setOnAddAccountListener(this);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(com.nextgis.maplibui.R.id.login_frame, foclLoginFragment, "FoclLogin");
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
