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
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncInfo;
import android.content.SyncStatusObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;

import java.io.File;


public class MainActivity
        extends ActionBarActivity
{
    public static final String DATA_DIR_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "ngm_clink_monitoring";

    public static final String PHOTO_DIR_PATH = DATA_DIR_PATH + File.separator + "foto";

    protected SyncStatusObserver mSyncStatusObserver;
    protected Object             mSyncHandle;

    protected boolean mIsUILocked      = false;
    protected boolean mIsSynchronizing = false;
    protected boolean mIsMapReloading  = false;


    private static boolean isSyncActive(
            Account account,
            String authority)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return isSyncActiveHoneycomb(account, authority);
        } else {
            SyncInfo currentSync = ContentResolver.getCurrentSync();
            return currentSync != null && currentSync.account.equals(account) &&
                   currentSync.authority.equals(authority);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static boolean isSyncActiveHoneycomb(
            Account account,
            String authority)
    {
        for (SyncInfo syncInfo : ContentResolver.getCurrentSyncs()) {
            if (syncInfo.account.equals(account) && syncInfo.authority.equals(authority)) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSyncStatusObserver = new SyncStatusObserver()
        {
            @Override
            public void onStatusChanged(int which)
            {
                runOnUiThread(
                        new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Account account = getAccount();

                                if (null != account) {
                                    mIsSynchronizing =
                                            isSyncActive(account, FoclSettingsConstants.AUTHORITY);

                                    if (!mIsMapReloading) {
                                        setUILocked(mIsSynchronizing);
                                    }
                                }
                            }
                        });
            }
        };

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

        setUILocked(false);
    }


    public void setUILocked(boolean isUILocked)
    {
        mIsUILocked = isUILocked;

        if (isUILocked) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            PerformSyncFragment performSyncFragment =
                    (PerformSyncFragment) getSupportFragmentManager().findFragmentByTag(
                            "PerformSync");

            if (performSyncFragment == null) {
                performSyncFragment = new PerformSyncFragment();
            }

            ft.replace(R.id.work_fragment, performSyncFragment, "PerformSync");
            ft.commit();
            getSupportFragmentManager().executePendingTransactions();

        } else {
            TypeWorkFragment typeWorkFragment =
                    (TypeWorkFragment) getSupportFragmentManager().findFragmentByTag("TypeWork");

            if (typeWorkFragment == null) {
                typeWorkFragment = new TypeWorkFragment();

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.work_fragment, typeWorkFragment, "TypeWork");
                ft.commit();
            }

            typeWorkFragment.setOnButtonsClickListener(
                    new TypeWorkFragment.OnButtonsClickListener()
                    {
                        @Override
                        public void OnButtonsClick(int workType)
                        {
                            FragmentTransaction frTr =
                                    getSupportFragmentManager().beginTransaction();

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

        switchMenuView();
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        // Refresh synchronization status
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for synchronization status changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                         ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }


    @Override
    protected void onPause()
    {
        // Remove our synchronization listener if registered
        if (mSyncHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncHandle);
            mSyncHandle = null;
        }

        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // TODO: remove menu_sync for LineWorkFragment

        if (mIsUILocked) {
            menu.findItem(R.id.menu_map).setEnabled(false);
            menu.findItem(R.id.menu_sync).setEnabled(false);
            menu.findItem(R.id.menu_settings).setEnabled(false);
        }

        return true;
    }


    public void switchMenuView()
    {
        supportInvalidateOptionsMenu();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {

            case R.id.menu_map:
                onMenuMapClick();
                return true;

            case R.id.menu_sync:
                onMenuSyncClick();
                return true;

            case R.id.menu_settings:
                onMenuSettingsClick();
                return true;

            case R.id.menu_about:
                onMenuAboutClick();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onMenuMapClick()
    {
        GISApplication app = (GISApplication) getApplication();
        FoclProject foclProject = app.getFoclProject();
        foclProject.setVisible(true);
        app.getMap().save();

        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    public void onMenuSyncClick()
    {
        GISApplication app = (GISApplication) getApplication();
        FoclProject foclProject = app.getFoclProject();

        if (null == foclProject) {
            return;
        }

        Account account = getAccount();

        if (null == account) {
            Toast.makeText(this, "NO connection", Toast.LENGTH_LONG).show();
            return;
        }

        AccountManager accountManager = AccountManager.get(this);

        if (!app.isLoadedFoclProject()) {
            String accountName = account.name;
            String url = accountManager.getUserData(account, "url");
            String password = accountManager.getPassword(account);
            String login = accountManager.getUserData(account, "login");

            if (null == url || null == login || null == password) {
                Toast.makeText(this, "NO connection", Toast.LENGTH_LONG).show();
                return;
            }

            foclProject.setAccountName(accountName);
            foclProject.setURL(url);
            foclProject.setLogin(login);
            foclProject.setPassword(password);
            foclProject.save();
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(account, FoclSettingsConstants.AUTHORITY, settingsBundle);
    }


    public void onMenuSettingsClick()
    {
        final IGISApplication app = (IGISApplication) getApplication();
        app.showSettings();
    }


    public void onMenuAboutClick()
    {
        Intent intentAbout = new Intent(this, AboutActivity.class);
        startActivity(intentAbout);
    }


    public Account getAccount()
    {
        AccountManager accountManager = AccountManager.get(this);
        if (accountManager.getAccountsByType(Constants.NGW_ACCOUNT_TYPE).length > 0) {
            // we work only with one account
            return accountManager.getAccountsByType(Constants.NGW_ACCOUNT_TYPE)[0];
        }
        return null;
    }
}
