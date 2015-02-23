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
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.NGWLoginFragment;
import com.nextgis.maplibui.NGWSettingsActivity;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.ngm_clink_monitoring.activities.SettingsActivity;
import com.nextgis.ngm_clink_monitoring.map.FoclLayerFactory;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;
import com.nextgis.ngm_clink_monitoring.util.UIUpdater;

import java.io.File;


public class GISApplication
        extends Application
        implements IGISApplication, NGWLoginFragment.OnAddAccountListener,
                   NGWSettingsActivity.OnDeleteAccountListener
{
    protected MapDrawable    mMap;
    protected GpsEventSource mGpsEventSource;
    protected SyncReceiver   mSyncReceiver;

    protected Location mCurrentLocation = null;

    protected OnAccountAddedListener   mOnAccountAddedListener   = null;
    protected OnAccountDeletedListener mOnAccountDeletedListener = null;
    protected OnReloadMapListener      mOnReloadMapListener      = null;

    protected UIUpdater mSyncPeriodicRunner;

    protected boolean mIsAccountCreated = false;
    protected boolean mIsAccountDeleted = false;
    protected boolean mIsMapReloaded    = false;


    @Override
    public void onCreate()
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        super.onCreate();

        mGpsEventSource = new GpsEventSource(this);

        getMap();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(FoclSettingsConstants.KEY_PREF_APP_FIRST_RUN, true)) {
            onFirstRun();
            sharedPreferences.edit()
                    .putBoolean(FoclSettingsConstants.KEY_PREF_APP_FIRST_RUN, false)
                    .commit();
        }

        //turn on sync automatically (every 2 sec. on network exist) - to often?
        //ContentResolver.setMasterSyncAutomatically(true);

        //turn on periodic sync. Can be set for each layer individually, but this is simpler
        //this is for get changes from server mainly
        /*if (sharedPreferences.getBoolean(KEY_PREF_SYNC_PERIODICALLY, true)) {
            Bundle params = new Bundle();
            params.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, false);
            params.putBoolean(ContentResolver.SYNC_EXTRAS_DO_NOT_RETRY, false);
            params.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, false);

            SyncAdapter.setSyncPeriod(this, params, sharedPreferences.getLong(KEY_PREF_SYNC_PERIOD,
                                                                              600)); //10 min
        }
        */

        mSyncReceiver = new SyncReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncAdapter.SYNC_START);
        intentFilter.addAction(SyncAdapter.SYNC_FINISH);
        intentFilter.addAction(SyncAdapter.SYNC_CANCELED);
        intentFilter.addAction(SyncAdapter.SYNC_CHANGES);
        registerReceiver(mSyncReceiver, intentFilter);

        if (!isRanAsService() && null != getAccount()) {
            startPeriodicSync();
        }
    }


    protected boolean isRanAsService()
    {
        return getCurrentProcessName().matches(".*:sync$");
    }


    protected String getCurrentProcessName()
    {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }


    public MapDrawable getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);

        if (defaultPath != null) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            String mapPath = sharedPreferences.getString(
                    SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
            String mapName =
                    sharedPreferences.getString(FoclSettingsConstants.KEY_PREF_MAP_NAME, "default");

            File mapFullPath = new File(mapPath, mapName + Constants.MAP_EXT);

            final Bitmap bkBitmap = BitmapFactory.decodeResource(
                    getResources(), com.nextgis.maplibui.R.drawable.bk_tile);
            mMap = new MapDrawable(bkBitmap, this, mapFullPath, new FoclLayerFactory());
            mMap.setName(mapName);
            mMap.load();
        }

        return mMap;
    }


    protected void onFirstRun()
    {
        //add OpenStreetMap layer on application first run
        String layerName = getString(R.string.osm);
        String layerURL = getString(R.string.osm_url);
        RemoteTMSLayerUI layer =
                new RemoteTMSLayerUI(getApplicationContext(), mMap.createLayerStorage());
        layer.setName(layerName);
        layer.setURL(layerURL);
        layer.setTMSType(GeoConstants.TMSTYPE_OSM);
        layer.setVisible(true);

        mMap.addLayer(layer);
        mMap.save();
    }


    public void addFoclProject()
    {
        Account account = getAccount();

        if (null == account) {
            return;
        }

        AccountManager accountManager = AccountManager.get(this);

        String accountName = account.name;
        String url = accountManager.getUserData(account, "url");
        String password = accountManager.getPassword(account);
        String login = accountManager.getUserData(account, "login");

        File foclPath = mMap.createLayerStorage();
        FoclProject foclProject =
                new FoclProject(mMap.getContext(), foclPath, new FoclLayerFactory());

        foclProject.setName(FoclConstants.FOCL_PROJECT);
        foclProject.setAccountName(accountName);
        foclProject.setURL(url);
        foclProject.setLogin(login);
        foclProject.setPassword(password);
        foclProject.setVisible(true);

        mMap.addLayer(foclProject);
        mMap.save();
    }


    public FoclProject getFoclProject()
    {
        if (mMap == null) {
            return null;
        }

        for (int i = 0; i < mMap.getLayerCount(); i++) {
            ILayer layer = mMap.getLayer(i);
            if (layer.getType() == FoclConstants.LAYERTYPE_FOCL_PROJECT) {
                FoclProject foclProject = (FoclProject) layer;

                if (foclProject.getLayerCount() > 0) {
                    return foclProject;
                }

                return null;
            }
        }

        return null;
    }


    @Override
    public String getAuthority()
    {
        return FoclSettingsConstants.AUTHORITY;
    }


    public Account getAccount()
    {
        AccountManager accountManager = AccountManager.get(this);
        for (Account account : accountManager.getAccountsByType(Constants.NGW_ACCOUNT_TYPE)) {
            if (account.name.equals(FoclConstants.FOCL_ACCOUNT_NAME)) {
                return account;
            }
        }
        return null;
    }


    public GpsEventSource getGpsEventSource()
    {
        return mGpsEventSource;
    }


    public void setSyncPeriod(int seconds)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putInt(FoclSettingsConstants.KEY_PREF_SYNC_PERIOD_SEC, seconds)
                .commit();

        mSyncPeriodicRunner.setUpdateInterval(seconds * 1000);
    }


    public boolean startPeriodicSync()
    {
        final Account account = getAccount();

        if (null == account) {
            return false;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int syncPriod = sharedPreferences.getInt(
                FoclSettingsConstants.KEY_PREF_SYNC_PERIOD_SEC,
                FoclConstants.DEFAULT_SYNC_PERIOD_SEC);

        mSyncPeriodicRunner = new UIUpdater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runSync(account);
                    }
                }, syncPriod * 1000);

        mSyncPeriodicRunner.startUpdates();
        return true;
    }


    public void stopPeriodicSync()
    {
        if (null == mSyncPeriodicRunner) {
            return;
        }

        mSyncPeriodicRunner.stopUpdates();
    }


    public boolean runSync(Account account)
    {
        if (null == account) {
            return false;
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(account, FoclSettingsConstants.AUTHORITY, settingsBundle);
        return true;
    }


    @Override
    public void showSettings()
    {
        Intent intentSet = new Intent(this, SettingsActivity.class);
        intentSet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Intent.FLAG_ACTIVITY_CLEAR_TOP |
        startActivity(intentSet);
    }


    public Location getCurrentLocation()
    {
        return mCurrentLocation;
    }


    public void setCurrentLocation(Location currentLocation)
    {
        mCurrentLocation = currentLocation;
    }


    @Override
    public void onAddAccount(
            Account account,
            String token,
            boolean accountAdded)
    {
        if (accountAdded) {
            mIsAccountCreated = true;
            addFoclProject();
            startPeriodicSync();

            if (null != mOnAccountAddedListener) {
                mOnAccountAddedListener.onAccountAdded();
            }
        }
    }


    @Override
    public void onDeleteAccount(Account account)
    {
        mIsAccountDeleted = true;
        stopPeriodicSync();
        mMap.load(); // reload map without listener

        if (null != mOnAccountDeletedListener) {
            mOnAccountDeletedListener.onAccountDeleted();
        }
    }


    public void reloadMap()
    {
        mMap.load();

        mIsMapReloaded = true;

        if (null != mOnReloadMapListener) {
            mOnReloadMapListener.onReloadMap();
        }
    }


    public boolean isAccountAdded()
    {
        boolean isCreated = mIsAccountCreated;
        mIsAccountCreated = false;
        return isCreated;
    }


    public boolean isAccountDeleted()
    {
        boolean isDeleted = mIsAccountDeleted;
        mIsAccountDeleted = false;
        return isDeleted;
    }


    public boolean isMapReloaded()
    {
        boolean isReloaded = mIsMapReloaded;
        mIsMapReloaded = false;
        return isReloaded;
    }


    public void setOnAccountAddedListener(OnAccountAddedListener onAccountAddedListener)
    {
        mOnAccountAddedListener = onAccountAddedListener;
    }


    public interface OnAccountAddedListener
    {
        public void onAccountAdded();
    }


    public void setOnAccountDeletedListener(OnAccountDeletedListener onAccountDeletedListener)
    {
        mOnAccountDeletedListener = onAccountDeletedListener;
    }


    public interface OnAccountDeletedListener
    {
        public void onAccountDeleted();
    }


    public void setOnReloadMapListener(OnReloadMapListener onReloadMapListener)
    {
        mOnReloadMapListener = onReloadMapListener;
    }


    public interface OnReloadMapListener
    {
        public void onReloadMap();
    }


    protected class SyncReceiver
            extends BroadcastReceiver
    {
        @Override
        public void onReceive(
                Context context,
                Intent intent)
        {
            String action = intent.getAction();

            switch (action) {
                case SyncAdapter.SYNC_START:
                    break;

                case SyncAdapter.SYNC_FINISH:
                    reloadMap();
                    break;

                case SyncAdapter.SYNC_CANCELED:
                    Log.d(Constants.TAG, "SYNC_CANCELED is received");
                    break;

                case SyncAdapter.SYNC_CHANGES:
                    break;
            }
        }
    }
}
