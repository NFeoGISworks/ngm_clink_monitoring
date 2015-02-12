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
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncInfo;
import android.content.SyncStatusObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.ObjectTypesFragment;
import com.nextgis.ngm_clink_monitoring.fragments.StatusBarFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;

import java.io.File;


public class MainActivity
        extends ActionBarActivity
        implements GISApplication.OnReloadMapListener
{
    public static final String DATA_DIR_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "ngm_clink_monitoring";

    public static final String PHOTO_DIR_PATH = DATA_DIR_PATH + File.separator + "foto";

    protected SyncStatusObserver mSyncStatusObserver;
    protected Object             mSyncHandle;

    protected boolean mIsSynchronizing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // initialize the default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_general, false);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.object_types_toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);

        final FragmentManager fm = getSupportFragmentManager();

        StatusBarFragment statusBarFragment = (StatusBarFragment) fm.findFragmentByTag("StatusBar");

        if (statusBarFragment == null) {
            statusBarFragment = new StatusBarFragment();

            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.status_bar_fragment, statusBarFragment, "StatusBar");
            ft.commit();
        }

        ObjectTypesFragment objectTypesFragment =
                (ObjectTypesFragment) fm.findFragmentByTag("ObjectTypes");

        if (objectTypesFragment == null) {
            objectTypesFragment = new ObjectTypesFragment();

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.object_fragment, objectTypesFragment, "ObjectTypes");
            ft.commit();
        }

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
                                GISApplication app = (GISApplication) getApplication();
                                Account account = app.getAccount();

                                if (null != account) {
                                    mIsSynchronizing =
                                            isSyncActive(account, FoclSettingsConstants.AUTHORITY);
                                    switchMenuView();
                                }
                            }
                        });
            }
        };
    }


    public void refreshView()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.object_fragment);
        if (!fragment.isDetached()) {
            getSupportFragmentManager().beginTransaction()
                    .detach(fragment)
                    .attach(fragment)
                    .commit();
        }
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        GISApplication app = (GISApplication) getApplication();
        app.setOnReloadMapListener(this);

        if (app.isMapReloaded()) {
            refreshView();
        }

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
        GISApplication app = (GISApplication) getApplication();
        app.setOnReloadMapListener(null);

        // Remove our synchronization listener if registered
        if (mSyncHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncHandle);
            mSyncHandle = null;
        }

        super.onPause();
    }


    @Override
    public void OnReloadMap()
    {
        refreshView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (mIsSynchronizing) {
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

            case android.R.id.home:
                getSupportFragmentManager().popBackStackImmediate();
                return true;

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
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }


    public void onMenuSyncClick()
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();

        if (null == account) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            return;
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
}
