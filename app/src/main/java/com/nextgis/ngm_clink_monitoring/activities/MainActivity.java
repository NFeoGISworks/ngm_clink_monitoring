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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncResult;
import android.content.SyncStatusObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AccountUtil;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.fragments.FoclLoginFragment;
import com.nextgis.ngm_clink_monitoring.fragments.MapFragment;
import com.nextgis.ngm_clink_monitoring.fragments.ObjectTypesFragment;
import com.nextgis.ngm_clink_monitoring.fragments.PerformSyncFragment;
import com.nextgis.ngm_clink_monitoring.fragments.StatusBarFragment;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;


public class MainActivity
        extends ActionBarActivity
        implements GISApplication.OnReloadMapListener, GISApplication.OnAccountAddedListener,
                   GISApplication.OnAccountDeletedListener
{
    protected static final int VIEW_STATE_ACCOUNT  = 1;
    protected static final int VIEW_STATE_1ST_SYNC = 2;
    protected static final int VIEW_STATE_OBJECTS  = 3;

    public static final int FT_OBJECT_TYPES  = 1;
    public static final int FT_LINE_LIST     = 2;
    public static final int FT_OBJECT_LIST   = 3;
    public static final int FT_OBJECT_STATUS = 4;
    public static final int FT_LOGIN         = 5;
    public static final int FT_1ST_SYNC      = 6;
    public static final int FT_MAP           = 7;

    protected SyncStatusObserver mSyncStatusObserver;
    protected Object             mSyncHandle;

    protected int     mViewState = VIEW_STATE_ACCOUNT;
    protected boolean mIsSyncing = false;

    protected Toolbar           mToolbar;
    protected StatusBarFragment mStatusBarFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final GISApplication app = (GISApplication) getApplication();

        // initialize the default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences_general, false);

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
                                Account account = app.getAccount();

                                if (null != account) {
                                    mIsSyncing = AccountUtil.isSyncActive(
                                            account, FoclSettingsConstantsUI.AUTHORITY);
                                    switchMenuView();
                                }
                            }
                        });
            }
        };

        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.object_types_toolbar);
        mToolbar.setTitle(""); // needed for screen rotation
        mToolbar.getBackground().setAlpha(255);
        setSupportActionBar(mToolbar);

        FragmentManager fm = getSupportFragmentManager();
        mStatusBarFragment = (StatusBarFragment) fm.findFragmentByTag("StatusBar");
        if (null == mStatusBarFragment) {
            mStatusBarFragment = new StatusBarFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.status_bar_fragment, mStatusBarFragment, "StatusBar");
            ft.commit();
        }

        if (null == app.getAccount()) {
            mViewState = VIEW_STATE_ACCOUNT;
        } else if (null == app.getFoclProject()) {
            mViewState = VIEW_STATE_1ST_SYNC;
        } else {
            mViewState = VIEW_STATE_OBJECTS;
        }

        setActivityView();
    }


    protected void setActivityView()
    {
        GISApplication app = (GISApplication) getApplication();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (mViewState) {
            case VIEW_STATE_ACCOUNT:
                FoclLoginFragment foclLoginFragment =
                        (FoclLoginFragment) fm.findFragmentByTag("FoclLogin");

                if (null == foclLoginFragment) {
                    foclLoginFragment = new FoclLoginFragment();
                    foclLoginFragment.setOnAddAccountListener(app);
                    ft.replace(R.id.main_fragment, foclLoginFragment, "FoclLogin");
                }

                break;

            case VIEW_STATE_1ST_SYNC:
                PerformSyncFragment performSyncFragment =
                        (PerformSyncFragment) fm.findFragmentByTag("PerformSync");

                if (null == performSyncFragment) {
                    performSyncFragment = new PerformSyncFragment();
                    ft.replace(R.id.main_fragment, performSyncFragment, "PerformSync");
                }

                break;

            case VIEW_STATE_OBJECTS:
                ObjectTypesFragment objectTypesFragment =
                        (ObjectTypesFragment) fm.findFragmentByTag("ObjectTypes");

                if (null == objectTypesFragment) {
                    objectTypesFragment = new ObjectTypesFragment();
                    ft.replace(R.id.main_fragment, objectTypesFragment, "ObjectTypes");
                }

                break;
        }

        ft.commit();
        switchMenuView();
    }


    public void setBarsView(
            int fragmentType,
            String toolbarTitle)
    {
        if (null == mToolbar || null == mStatusBarFragment) {
            return;
        }

        mToolbar.setTitle(toolbarTitle == null ? getTitle() : toolbarTitle);

        switch (fragmentType) {
            case FT_LOGIN:
            case FT_1ST_SYNC:
            case FT_OBJECT_TYPES:
                mToolbar.setNavigationIcon(null);
                break;

            case FT_LINE_LIST:
            case FT_OBJECT_LIST:
            case FT_OBJECT_STATUS:
            case FT_MAP:
                mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (fragmentType) {
            case FT_LOGIN:
            case FT_LINE_LIST:
            case FT_OBJECT_LIST:
            case FT_OBJECT_STATUS:
                ft.hide(mStatusBarFragment);
                break;

            case FT_1ST_SYNC:
            case FT_OBJECT_TYPES:
            case FT_MAP:
                ft.show(mStatusBarFragment);
                break;
        }

        ft.commit();
    }


    public void refreshFragmentView()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        if (!fragment.isDetached()) {
            getSupportFragmentManager().beginTransaction()
                    .detach(fragment)
                    .attach(fragment)
                    .commit();
        }
        switchMenuView();
    }


    public void refreshActivityView()
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if (!LocationUtil.isLocationEnabled(this)) {
            LocationUtil.showSettingsLocationAlert(this);
        }

        GISApplication app = (GISApplication) getApplication();

        app.setOnAccountAddedListener(this);
        app.setOnAccountDeletedListener(this);
        app.setOnReloadMapListener(this);

        // Refresh synchronization status
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for synchronization status changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        if (app.isAccountAdded() || app.isAccountDeleted()) {
            refreshActivityView();
        }

        if (app.isMapReloaded()) {
            if (VIEW_STATE_1ST_SYNC == mViewState) {
                mViewState = VIEW_STATE_OBJECTS;
                setActivityView();
            } else {
                refreshFragmentView();
            }
        }
    }


    @Override
    protected void onPause()
    {
        GISApplication app = (GISApplication) getApplication();
        app.setOnAccountAddedListener(null);
        app.setOnAccountDeletedListener(null);
        app.setOnReloadMapListener(null);

        // Remove our synchronization listener if registered
        if (mSyncHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncHandle);
            mSyncHandle = null;
        }

        super.onPause();
    }


    @Override
    public void onAccountAdded()
    {
        refreshActivityView();
    }


    @Override
    public void onAccountDeleted()
    {
        refreshActivityView();
    }


    @Override
    public void onReloadMap()
    {
        if (VIEW_STATE_1ST_SYNC == mViewState) {
            mViewState = VIEW_STATE_OBJECTS;
            setActivityView();
        } else {
            refreshFragmentView();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        switch (mViewState) {
            case VIEW_STATE_1ST_SYNC:
                menu.findItem(R.id.menu_map).setEnabled(false);
            case VIEW_STATE_ACCOUNT:
                menu.findItem(R.id.menu_sync).setEnabled(false);
                break;
            case VIEW_STATE_OBJECTS:
                break;
        }

        if (mIsSyncing) {
            menu.findItem(R.id.menu_sync).setEnabled(false);
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

// for debug
/*
            case R.id.menu_test:
                testAttachInsert();
//                testAttachUpdate();
//                testAttachDelete();

                new Thread()
                {
                    @Override
                    public void run()
                    {
                        testSync();
                    }
                }.start();

                return true;
*/

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onMenuMapClick()
    {
        final FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentByTag("Map");

        if (mapFragment == null) {
            mapFragment = new MapFragment();

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.main_fragment, mapFragment, "Map");
            ft.addToBackStack(null);
            ft.commit();
        }
    }


    public void onMenuSyncClick()
    {
        GISApplication app = (GISApplication) getApplication();
        Account account = app.getAccount();

        if (null == account) {
            Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_LONG).show();
            return;
        }

        app.runSyncManually(account);
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


// for debug


    void testSync()
    {
        IGISApplication application = (IGISApplication) getApplication();
        sync(application.getMap(), application.getAuthority(), new SyncResult());
    }


    protected void sync(
            LayerGroup layerGroup,
            String authority,
            SyncResult syncResult)
    {
        for (int i = 0; i < layerGroup.getLayerCount(); i++) {
            ILayer layer = layerGroup.getLayer(i);
            if (layer instanceof LayerGroup) {
                sync((LayerGroup) layer, authority, syncResult);
            } else if (layer instanceof NGWVectorLayer) {
                NGWVectorLayer ngwVectorLayer = (NGWVectorLayer) layer;
                ngwVectorLayer.sync(authority, syncResult);
            }
        }
    }


    void testInsert()
    {
        //test sync
        IGISApplication application = (IGISApplication) getApplication();
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                            ngwVectorLayer.getPath().getName());
            ContentValues values = new ContentValues();
            //values.put(VectorLayer.FIELD_ID, 26);
            values.put("width", 1);
            values.put("azimuth", 2.0);
            values.put("status", "grot");
            values.put("temperatur", -13);
            values.put("name", "get");

            Calendar calendar = new GregorianCalendar(2015, Calendar.JANUARY, 23);
            values.put("datetime", calendar.getTimeInMillis());

            try {
                GeoPoint pt = new GeoPoint(37, 55);
                pt.setCRS(CRS_WGS84);
                pt.project(CRS_WEB_MERCATOR);
                GeoMultiPoint mpt = new GeoMultiPoint();
                mpt.add(pt);
                values.put(VectorLayer.FIELD_GEOM, mpt.toBlob());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri result = getContentResolver().insert(uri, values);
            if (result == null) {
                Log.d(TAG, "insert failed");
            } else {
                Log.d(TAG, result.toString());
            }
        }
    }


    void testUpdate()
    {
        //test sync
        IGISApplication application = (IGISApplication) getApplication();
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                            ngwVectorLayer.getPath().getName());
            Uri updateUri = ContentUris.withAppendedId(uri, 29);
            ContentValues values = new ContentValues();
            values.put("width", 4);
            values.put("azimuth", 8.0);
            values.put("status", "test4");
            values.put("temperatur", -10);
            values.put("name", "xxx");

            Calendar calendar = new GregorianCalendar(2014, Calendar.JANUARY, 23);
            values.put("datetime", calendar.getTimeInMillis());
            try {
                GeoPoint pt = new GeoPoint(67, 65);
                pt.setCRS(CRS_WGS84);
                pt.project(CRS_WEB_MERCATOR);
                GeoMultiPoint mpt = new GeoMultiPoint();
                mpt.add(pt);
                values.put(VectorLayer.FIELD_GEOM, mpt.toBlob());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int result = getContentResolver().update(updateUri, values, null, null);
            if (result == 0) {
                Log.d(TAG, "update failed");
            } else {
                Log.d(TAG, "" + result);
            }
        }
    }


    void testDelete()
    {
        IGISApplication application = (IGISApplication) getApplication();
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                            ngwVectorLayer.getPath().getName());
            Uri deleteUri = ContentUris.withAppendedId(uri, 27);
            int result = getContentResolver().delete(deleteUri, null, null);
            if (result == 0) {
                Log.d(TAG, "delete failed");
            } else {
                Log.d(TAG, "" + result);
            }
        }
    }


    void testAttachInsert()
    {
        IGISApplication application = (IGISApplication) getApplication();

/*
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri uri = Uri.parse(
                    "content://" + FoclSettingsConstantsUI.AUTHORITY + "/" +
                    ngwVectorLayer.getPath().getName() + "/36/attach");
*/

        Uri uri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY +
                        "/layer_20150320215025293/1/attach");

        ContentValues values = new ContentValues();
        values.put(VectorLayer.ATTACH_DISPLAY_NAME, "test_image.jpg");
        values.put(VectorLayer.ATTACH_MIME_TYPE, "image/jpeg");
        values.put(VectorLayer.ATTACH_DESCRIPTION, "test image description");

        Uri result = getContentResolver().insert(uri, values);

        if (result == null) {
            Log.d(TAG, "insert failed");

        } else {

            try {
                OutputStream outStream = getContentResolver().openOutputStream(result);
                Bitmap sourceBitmap = BitmapFactory.decodeResource(
                        getResources(), com.nextgis.maplibui.R.drawable.bk_tile);
                sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, result.toString());
        }
        //}
    }


    void testAttachUpdate()
    {
        IGISApplication application = (IGISApplication) getApplication();

/*
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri updateUri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" +
                    ngwVectorLayer.getPath().getName() + "/36/attach/1000");
*/

        Uri updateUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY +
                        "/layer_20150210140455993/36/attach/2");

        ContentValues values = new ContentValues();
        values.put(VectorLayer.ATTACH_DISPLAY_NAME, "no_image.jpg");
        values.put(VectorLayer.ATTACH_DESCRIPTION, "simple update description");
        //    values.put(VectorLayer.ATTACH_ID, 999);
        int result = getContentResolver().update(updateUri, values, null, null);
        if (result == 0) {
            Log.d(TAG, "update failed");
        } else {
            Log.d(TAG, "" + result);
        }
        //}
    }


    void testAttachDelete()
    {
        IGISApplication application = (IGISApplication) getApplication();

/*
        MapBase map = application.getMap();
        NGWVectorLayer ngwVectorLayer = null;
        for (int i = 0; i < map.getLayerCount(); i++) {
            ILayer layer = map.getLayer(i);
            if (layer instanceof NGWVectorLayer) {
                ngwVectorLayer = (NGWVectorLayer) layer;
            }
        }
        if (null != ngwVectorLayer) {
            Uri deleteUri = Uri.parse(
                    "content://" + SettingsConstants.AUTHORITY + "/" +
                    ngwVectorLayer.getPath().getName() + "/36/attach/1000");
*/

        Uri deleteUri = Uri.parse(
                "content://" + FoclSettingsConstantsUI.AUTHORITY +
                        "/layer_20150210140455993/36/attach/1");
        int result = getContentResolver().delete(deleteUri, null, null);
        if (result == 0) {
            Log.d(TAG, "delete failed");
        } else {
            Log.d(TAG, "" + result);
        }
        //}
    }
}
