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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.NetworkUtil;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.NGWLoginFragment;
import com.nextgis.maplibui.NGWSettingsActivity;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.activities.FoclSettingsActivity;
import com.nextgis.ngm_clink_monitoring.map.FoclLayerFactory;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.UIUpdater;

import java.io.File;
import java.io.IOException;


public class GISApplication
        extends Application
        implements IGISApplication, NGWLoginFragment.OnAddAccountListener,
                   NGWSettingsActivity.OnDeleteAccountListener
{
    protected MapDrawable    mMap;
    protected NetworkUtil mNet;
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

        mNet = new NetworkUtil(this);
        mGpsEventSource = new GpsEventSource(this);

        getMap();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(FoclSettingsConstantsUI.KEY_PREF_APP_FIRST_RUN, true)) {
            onFirstRun();
            sharedPreferences.edit()
                    .putBoolean(FoclSettingsConstantsUI.KEY_PREF_APP_FIRST_RUN, false)
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

        if (isAutoSyncEnabled()) {
            if (!isRanAsService()) {
                startPeriodicSync();
            }
        }
    }


    public boolean isRanAsService()
    {
        return getCurrentProcessName().matches(".*:sync$");
    }


    public String getCurrentProcessName()
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
            String mapName = sharedPreferences.getString(
                    FoclSettingsConstantsUI.KEY_PREF_MAP_NAME, "default");

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
        return FoclSettingsConstantsUI.AUTHORITY;
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


    public boolean hasAccount()
    {
        return null != getAccount();
    }


    public boolean isOriginalPhotoSaving()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(
                FoclSettingsConstantsUI.KEY_PREF_ORIGINAL_PHOTO_SAVING, false);
    }


    public void setDataParentPath(String newDataParentPath)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(
                FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH, newDataParentPath).commit();
    }


    public String getDataParentPath()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultDataParentPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        return sharedPreferences.getString(
                FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH, defaultDataParentPath);
    }


    public String getDataPath()
    {
        return getDataParentPath() + File.separator + FoclConstants.FOCL_DATA_DIR;
    }


    public String getPhotoPath()
    {
        return getDataPath() + File.separator + FoclConstants.FOCL_PHOTO_DIR;
    }


    public File getDataDir()
            throws IOException
    {
        return FileUtil.getDirWithCreate(getDataPath());
    }


    public File getPhotoDir()
            throws IOException
    {
        return FileUtil.getDirWithCreate(getPhotoPath());
    }


    public GpsEventSource getGpsEventSource()
    {
        return mGpsEventSource;
    }


    public boolean isNetworkAvailable()
    {
        return mNet.isNetworkAvailable();
    }


    public boolean isAutoSyncEnabled()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean(
                FoclSettingsConstantsUI.KEY_PREF_AUTO_SYNC_ENABLED, true);
    }


    public void setAutoSyncEnabled(boolean isEnabled)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putBoolean(FoclSettingsConstantsUI.KEY_PREF_AUTO_SYNC_ENABLED, isEnabled)
                .commit();

        if (isEnabled) {
            startPeriodicSync();
        } else {
            stopPeriodicSync();
        }
    }


    public long getSyncPeriodSec()
    {
        return PreferenceManager.getDefaultSharedPreferences(this).getLong(
                SettingsConstantsUI.KEY_PREF_SYNC_PERIOD_SEC_LONG,
                FoclConstants.DEFAULT_SYNC_PERIOD_SEC_LONG);
    }


    public void setSyncPeriodSec(long seconds)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putLong(SettingsConstantsUI.KEY_PREF_SYNC_PERIOD_SEC_LONG, seconds)
                .commit();

        if (null != mSyncPeriodicRunner) {
            mSyncPeriodicRunner.setUpdateIntervalMillisec(seconds * 1000);
        }

        refreshSyncQueue();
    }


    public void startPeriodicSync()
    {
        if (isRanAsService()) {
            Log.d(Constants.TAG, "!!! trying run auto sync from service, startPeriodicSync() !!!");
            return;
        }

        if (!hasAccount()) {
            return;
        }

        clearSyncQueue();

        mSyncPeriodicRunner = new UIUpdater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runSync();
                    }
                }, getSyncPeriodSec() * 1000);

        mSyncPeriodicRunner.startUpdates();
    }


    public void stopPeriodicSync()
    {
        clearSyncQueue();
        resetSystemSyncQueue();
    }


    public boolean runSyncManually()
    {
        refreshSyncQueue();
        return runSync();
    }


    protected boolean runSync()
    {
        if (!isNetworkAvailable()) {
            return false;
        }

        Account account = getAccount();

        if (null == account) {
            return false;
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(account, FoclSettingsConstantsUI.AUTHORITY, settingsBundle);
        return true;
    }


    public void refreshSyncQueue()
    {
        if (null != mSyncPeriodicRunner && isAutoSyncEnabled()) {
            mSyncPeriodicRunner.refreshUpdaterQueue();
        }
    }


    public void clearSyncQueue()
    {
        if (null != mSyncPeriodicRunner) {
            mSyncPeriodicRunner.stopUpdates();
        }
    }


    protected boolean resetSystemSyncQueue()
    {
        Account account = getAccount();

        if (null == account) {
            return false;
        }

        ContentResolver.removePeriodicSync(account, getAuthority(), Bundle.EMPTY);
        ContentResolver.setSyncAutomatically(account, getAuthority(), false);
        return true;
    }


    @Override
    public void showSettings()
    {
        Intent intentSet = new Intent(this, FoclSettingsActivity.class);
        intentSet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Intent.FLAG_ACTIVITY_CLEAR_TOP |
        startActivity(intentSet);
    }


    public boolean isLocationShowing()
    {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(FoclSettingsConstantsUI.KEY_PREF_SHOW_LOCATION, true);
    }


    public String getLocationOverlayMode()
    {
        return isLocationShowing() ? "1" : "0";
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

            if (isAutoSyncEnabled()) {
                startPeriodicSync();
            } else {
                runSyncManually();
            }

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


    public void moveProgramData(
            String oldDataParentPath,
            String newDataParentPath)
    {
        File oldDataPath =
                new File(oldDataParentPath + File.separator + FoclConstants.FOCL_DATA_DIR);
        File newDataPath =
                new File(newDataParentPath + File.separator + FoclConstants.FOCL_DATA_DIR);

        if (!oldDataPath.exists()) {
            return;
        }

        if (oldDataPath.equals(newDataPath)) {
            return;
        }

        if (!newDataPath.exists()) {
            if (!newDataPath.mkdirs()) {
                // TODO: make Toast
                return;
            }
        }

// TODO: remove it
//        FileUtil.move(oldDataPath, newDataPath);
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
            if (isRanAsService()) {
                return;
            }

            String action = intent.getAction();

            switch (action) {
                case SyncAdapter.SYNC_START:
                    break;

                case SyncAdapter.SYNC_FINISH:
                    resetSystemSyncQueue();
                    reloadMap();
                    break;

                case SyncAdapter.SYNC_CANCELED:
                    Log.d(Constants.TAG, "GISApplication - SYNC_CANCELED is received");
                    break;

                case SyncAdapter.SYNC_CHANGES:
                    break;
            }
        }
    }
}
