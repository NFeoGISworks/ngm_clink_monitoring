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

package com.nextgis.ngm_clink_monitoring;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;

import java.io.File;

import static com.nextgis.maplib.util.Constants.NGW_ACCOUNT_TYPE;


public class MainActivity
        extends ActionBarActivity
{
    public static final String DATA_DIR_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "ngm_clink_monitoring";

    public static final String PHOTO_DIR_PATH = DATA_DIR_PATH + File.separator + "foto";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // initialize the default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_general, false);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        StatusBarFragment statusBarFragment =
                (StatusBarFragment) getSupportFragmentManager().findFragmentByTag("StatusBar");

        if (statusBarFragment == null) {
            statusBarFragment = new StatusBarFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.status_bar_fragment, statusBarFragment, "StatusBar");
            ft.commit();
        }

        TypeWorkFragment typeWorkFragment =
                (TypeWorkFragment) getSupportFragmentManager().findFragmentByTag("TypeWork");

        if (typeWorkFragment == null) {
            typeWorkFragment = new TypeWorkFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.work_fragment, typeWorkFragment, "TypeWork");
            ft.commit();
        }

        typeWorkFragment.setOnButtonsClickListener(new TypeWorkFragment.OnButtonsClickListener()
        {
            @Override
            public void OnButtonsClick(int workType)
            {
                FragmentTransaction frTr = getSupportFragmentManager().beginTransaction();

                LineWorkFragment lineWorkFragment =
                        (LineWorkFragment) getSupportFragmentManager().findFragmentByTag(
                                "LineWork");

                if (lineWorkFragment == null) {
                    lineWorkFragment = new LineWorkFragment();
                }

                lineWorkFragment.setParams(workType);

                frTr.replace(R.id.work_fragment, lineWorkFragment, "LineWork");
                frTr.addToBackStack(null);
                frTr.commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.menu_map:
                onMenuMapClick();
                return true;

            case R.id.menu_sync:
                onSync();
                return true;

            case R.id.menu_download:
                downloadRemoteData();
                return true;

            case R.id.menu_settings:
                Intent intentSet = new Intent(this, SettingsActivity.class);
                startActivity(intentSet);
                return true;

            case R.id.menu_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onMenuMapClick()
    {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    public void onSync()
    {
        final AccountManager accountManager = AccountManager.get(this);
        for(Account account : accountManager.getAccountsByType(NGW_ACCOUNT_TYPE)){
            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            ContentResolver.requestSync(account, com.nextgis.ngm_clink_monitoring.util.SettingsConstants.AUTHORITY, settingsBundle);
        }
    }


    protected void downloadRemoteData(){
        //TODO: get the only one account (account name) from preferences (spinner with accounts to select)
        //now hardcoded this one
        String accountName = "176.9.38.120/cl";
        final AccountManager accountManager = AccountManager.get(this);
        String url = null;
        String password = null;
        String login = null;

        for(Account account : accountManager.getAccountsByType(NGW_ACCOUNT_TYPE)){
            if(account.name.equals(accountName)){
                url = accountManager.getUserData(account, "url");
                password = accountManager.getPassword(account);
                login = accountManager.getUserData(account, "login");
                break;
            }
        }

        GISApplication app = (GISApplication) getApplication();
        FoclProject foclProject = app.getFoclProject();

        foclProject.setName("FOCL");
        foclProject.setAccountName(accountName);
        foclProject.setURL(url);
        foclProject.setLogin(login);
        foclProject.setPassword(password);
        foclProject.setVisible(true);

        //init in separate thread
        foclProject.downloadAsync();
    }
}
