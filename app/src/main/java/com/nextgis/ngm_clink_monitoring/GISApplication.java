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

package com.nextgis.ngm_clink_monitoring;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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
import com.nextgis.maplibui.activity.NGWSettingsActivity;
import com.nextgis.maplibui.fragment.NGWLoginFragment;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.maplibui.util.SettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.activities.FoclSettingsActivity;
import com.nextgis.ngm_clink_monitoring.map.FoclLayerFactory;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.map.FoclStruct;
import com.nextgis.ngm_clink_monitoring.services.FoclReportService;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclFileUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.UIUpdater;
import ru.elifantiev.android.roboerrorreporter.RoboErrorReporter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.nextgis.maplib.util.Constants.NGW_ACCOUNT_TYPE;
import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.ngm_clink_monitoring.util.FoclConstants.*;


public class GISApplication
        extends Application
        implements IGISApplication, NGWLoginFragment.OnAddAccountListener,
                   NGWSettingsActivity.OnDeleteAccountListener
{
    protected MapDrawable    mMap;
    protected NetworkUtil    mNet;
    protected GpsEventSource mGpsEventSource;
    protected SyncReceiver   mSyncReceiver;

    protected Location mCurrentLocation = null;

    protected Long mGpsTimeOffset = null;

    protected OnAccountAddedListener   mOnAccountAddedListener   = null;
    protected OnAccountDeletedListener mOnAccountDeletedListener = null;
    protected OnReloadMapListener      mOnReloadMapListener      = null;

    protected UIUpdater mSyncPeriodicRunner;

    protected boolean mIsAccountCreated = false;
    protected boolean mIsAccountDeleted = false;
    protected boolean mIsMapReloaded    = false;

    protected FoclStruct mSelectedFoclStruct;

    protected boolean mIsFullSync;


    @Override
    public void onCreate()
    {
        // For service debug
//        android.os.Debug.waitForDebugger();

        super.onCreate();

        // Get logcat after crash
        // http://habrahabr.ru/post/129582/
        // https://github.com/Olegas/RoboErrorReporter
        // http://stackoverflow.com/a/19968400/4727406

        // Setup handler for uncaught exceptions.
        RoboErrorReporter.bindReporter(this);


        if (isRanAsReportService()) {
            return;
        }


        mNet = new NetworkUtil(this);

        if (mNet.isNetworkAvailable()) {
            try {
                sendReportsOnServer(true, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // for debug
//        Integer a = 1;
//        if (isRanAsSyncService()) {
//            a = null;
//        }
//        int x = 6;
//        x = x / a;  // Exception here!


        mGpsEventSource = new GpsEventSource(this);
        getMap();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(FoclSettingsConstantsUI.KEY_PREF_APP_FIRST_RUN, true)) {
            onFirstRun();
            sharedPreferences.edit()
                    .putBoolean(FoclSettingsConstantsUI.KEY_PREF_APP_FIRST_RUN, false)
                    .putInt(SettingsConstants.KEY_PREF_LOCATION_SOURCE, GpsEventSource.GPS_PROVIDER)
                    .putString(SettingsConstants.KEY_PREF_LOCATION_MIN_TIME, "0")
                    .putString(SettingsConstants.KEY_PREF_LOCATION_MIN_DISTANCE, "0")
                    .commit();
        }

        mSyncReceiver = new SyncReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncAdapter.SYNC_START);
        intentFilter.addAction(SyncAdapter.SYNC_FINISH);
        intentFilter.addAction(SyncAdapter.SYNC_CANCELED);
        intentFilter.addAction(SyncAdapter.SYNC_CHANGES);
        registerReceiver(mSyncReceiver, intentFilter);

        if (!isRanAsSyncService()) {
            ContentResolver.cancelSync(getAccount(), getAuthority());
        }

        if (isAutoSyncEnabled()) {
            if (!isRanAsSyncService()) {
                startPeriodicSync();
            }
        }
    }


    public boolean isRanAsSyncService()
    {
        return getCurrentProcessName().matches(".*:sync$");
    }


    public boolean isRanAsReportService()
    {
        return getCurrentProcessName().matches(".*:report$");
    }


    public void sendReportsOnServer(
            boolean sendReportFromMain,
            boolean sendWorkData)
            throws IOException
    {
        if (sendWorkData) {
            Intent intentReportService = new Intent(this, FoclReportService.class);
            intentReportService.putExtra(FOCL_SEND_REPORT_FROM_MAIN, sendReportFromMain);
            intentReportService.putExtra(FOCL_SEND_WORK_DATA, true);
            startService(intentReportService);

        } else {
            String reportsDirPath = getReportsDirPath();
            File reportsDir = new File(reportsDirPath);

            if (reportsDir.isDirectory()) {
                File[] repFiles = reportsDir.listFiles(
                        new FilenameFilter()
                        {
                            @Override
                            public boolean accept(
                                    final File dir,
                                    final String name)
                            {
                                return name.matches(".*\\" + FOCL_REPORT_FILE_EXT);
                            }
                        });

                if (repFiles.length > 0) {
                    Log.d(
                            TAG,
                            "Report service starting, sendReportFromMain: " + sendReportFromMain);
                    Intent intentReportService = new Intent(this, FoclReportService.class);
                    intentReportService.putExtra(FOCL_SEND_REPORT_FROM_MAIN, sendReportFromMain);
                    intentReportService.putExtra(FOCL_SEND_WORK_DATA, false);
                    startService(intentReportService);
                } else if (sendReportFromMain) {
                    throw new IOException(getString(R.string.reports_not_found));
                }
            }
        }
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


    public String getMapPath()
    {
        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String storedPath = sharedPreferences.getString(
                SettingsConstants.KEY_PREF_MAP_PATH, "");

        if (!TextUtils.isEmpty(storedPath)) {
            return storedPath;
        }

        return null == defaultPath ? null : defaultPath.getPath();
    }


    @Override
    public MapDrawable getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        String mapPath = getMapPath();

        if (mapPath != null) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
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
        String layerURL = SettingsConstantsUI.OSM_URL;
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

        File foclPath = mMap.createLayerStorage();
        FoclProject foclProject =
                new FoclProject(mMap.getContext(), foclPath, new FoclLayerFactory());

        foclProject.setName(FoclConstants.FOCL_PROJECT);
        foclProject.setAccountName(account.name);

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


    public FoclStruct getSelectedFoclStruct()
    {
        return mSelectedFoclStruct;
    }


    public void setSelectedFoclStruct(FoclStruct selectedFoclStruct)
    {
        mSelectedFoclStruct = selectedFoclStruct;
    }


    @Override
    public String getAuthority()
    {
        return FoclSettingsConstantsUI.AUTHORITY;
    }


    @Override
    public boolean addAccount(
            String name,
            String url,
            String login,
            String password,
            String token)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return false;
        }

        final Account account = new Account(name, NGW_ACCOUNT_TYPE);

        Bundle userData = new Bundle();
        userData.putString("url", url.trim());
        userData.putString("login", login);

        AccountManager accountManager = AccountManager.get(this);

        boolean accountAdded = accountManager.addAccountExplicitly(account, password, userData);

        if (accountAdded) {
            accountManager.setAuthToken(account, account.type, token);
        }

        return accountAdded;
    }


    @Override
    public void setUserData(
            String name,
            String key,
            String value)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return;
        }

        Account account = getAccount(name);

        if (null != account) {
            AccountManager accountManager = AccountManager.get(this);
            accountManager.setUserData(account, key, value);
        }
    }


    @Override
    public void setPassword(
            String name,
            String value)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return;
        }
        Account account = getAccount(name);

        if (null != account) {
            AccountManager accountManager = AccountManager.get(this);
            accountManager.setPassword(account, value);
        }
    }


    @Override
    public AccountManagerFuture<Boolean> removeAccount(Account account)
    {
        AccountManagerFuture<Boolean> accountManagerFuture = new AccountManagerFuture<Boolean>()
        {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning)
            {
                return false;
            }


            @Override
            public boolean isCancelled()
            {
                return false;
            }


            @Override
            public boolean isDone()
            {
                return false;
            }


            @Override
            public Boolean getResult()
                    throws OperationCanceledException, IOException, AuthenticatorException
            {
                return null;
            }


            @Override
            public Boolean getResult(
                    long timeout,
                    TimeUnit unit)
                    throws OperationCanceledException, IOException, AuthenticatorException
            {
                return null;
            }
        };


        if (!checkPermission(Manifest.permission.MANAGE_ACCOUNTS)) {
            return accountManagerFuture;
        }

        AccountManager accountManager = AccountManager.get(this);
        if (accountManager == null) {
            return accountManagerFuture;
        }

        return accountManager.removeAccount(account, null, new Handler());
    }


    @Override
    public Account getAccount(String accountName)
    {
        if (!checkPermission(Manifest.permission.GET_ACCOUNTS)) {
            return null;
        }

        AccountManager accountManager = AccountManager.get(this);
        if (accountManager == null) {
            return null;
        }

        for (Account account : accountManager.getAccountsByType(Constants.NGW_ACCOUNT_TYPE)) {
            if (account == null) {
                continue;
            }
            if (account.name.equals(accountName)) {
                return account;
            }
        }

        return null;
    }


    @Override
    public String getAccountUrl(Account account)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return "";
        }

        AccountManager accountManager = AccountManager.get(this);
        return accountManager.getUserData(account, "url");
    }


    @Override
    public String getAccountLogin(Account account)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return "";
        }

        AccountManager accountManager = AccountManager.get(this);
        return accountManager.getUserData(account, "login");
    }


    @Override
    public String getAccountPassword(Account account)
    {
        if (!checkPermission(Manifest.permission.AUTHENTICATE_ACCOUNTS)) {
            return "";
        }

        AccountManager accountManager = AccountManager.get(this);
        return accountManager.getPassword(account);
    }


    protected boolean checkPermission(String permission)
    {
        PackageManager pm = getPackageManager();
        if (pm == null) {
            return false;
        }
        int hasPerm = pm.checkPermission(permission, getPackageName());
        return hasPerm == PackageManager.PERMISSION_GRANTED;
    }


    public Account getAccount()
    {
        return getAccount(FoclConstants.FOCL_ACCOUNT_NAME);
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
            throws IOException
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dataParentPath = sharedPreferences.getString(
                FoclSettingsConstantsUI.KEY_PREF_DATA_PARENT_PATH, null);

        if (TextUtils.isEmpty(dataParentPath)) {

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dataParentPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                throw new IOException("External storage is not mounted");
            }
        }

        return dataParentPath;
    }


    public String getDataPath()
            throws IOException
    {
        return getDataParentPath() + File.separator + FoclConstants.FOCL_DATA_DIR;
    }


    public File getDataDir()
            throws IOException
    {
        return FoclFileUtil.getDirWithCreate(getDataPath());
    }


    public String getPhotoPath()
            throws IOException
    {
        return getDataPath() + File.separator + FoclConstants.FOCL_PHOTO_DIR;
    }


    public File getPhotoDir()
            throws IOException
    {
        return FoclFileUtil.getDirWithCreate(getPhotoPath());
    }


    public String getReportsDirPath()
            throws IOException
    {
        return getDataPath() + File.separator + FoclConstants.FOCL_REPORTS_DIR;
    }


    public String getMainLogcatFilePath()
            throws IOException
    {
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        return getReportsDirPath() + File.separator + FoclConstants.FOCL_MAIN_LOGCAT_FILE_NAME +
                "_" + timeStamp;
    }


    public String getSyncLogcatFilePath()
            throws IOException
    {
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        return getReportsDirPath() + File.separator + FoclConstants.FOCL_SYNC_LOGCAT_FILE_NAME +
                "_" + timeStamp;
    }


    public String getErrorLogcatFilePath()
            throws IOException
    {
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        return getReportsDirPath() + File.separator + FoclConstants.FOCL_ERROR_LOGCAT_FILE_NAME +
                "_" + timeStamp;
    }


    public String getWorkDataZipFilePath()
            throws IOException
    {
        String timeStamp =
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(new Date());
        return getReportsDirPath() + File.separator + FoclConstants.FOCL_ZIP_WORK_DATA_FILE_NAME +
                "_" + timeStamp;
    }


    @Override
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
        if (isRanAsSyncService()) {
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
                        runSync(false);
                    }
                }, getSyncPeriodSec() * 1000);

        mSyncPeriodicRunner.startUpdates();
    }


    public void stopPeriodicSync()
    {
        clearSyncQueue();
        resetSystemSyncQueue();
    }


    public boolean runSyncManually(boolean isFullSync)
    {
        refreshSyncQueue();
        return runSync(isFullSync);
    }


    protected boolean runSync(boolean isFullSync)
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
        settingsBundle.putBoolean(FoclConstants.KEY_IS_FULL_SYNC, isFullSync);

        ContentResolver.requestSync(account, FoclSettingsConstantsUI.AUTHORITY, settingsBundle);
        return true;
    }


    public void setFullSync(boolean isFullSync)
    {
        mIsFullSync = isFullSync;
    }


    public boolean isFullSync()
    {
        return mIsFullSync;
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


    public Long getGpsTimeOffset()
    {
        if (null == mGpsTimeOffset) {
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            mGpsTimeOffset = sharedPreferences.getLong(
                    FoclSettingsConstantsUI.KEY_PREF_GPS_TIME_OFFSET, 0);
            Log.d(Constants.TAG, "Stored GpsTimeOffset: " + mGpsTimeOffset);
        }

        return mGpsTimeOffset;
    }


    public void setGpsTimeOffset(Long gpsTimeOffset)
    {
        mGpsTimeOffset = gpsTimeOffset;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit()
                .putLong(FoclSettingsConstantsUI.KEY_PREF_GPS_TIME_OFFSET, mGpsTimeOffset)
                .commit();
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
                runSyncManually(false);
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
        void onAccountAdded();
    }


    public void setOnAccountDeletedListener(OnAccountDeletedListener onAccountDeletedListener)
    {
        mOnAccountDeletedListener = onAccountDeletedListener;
    }


    public interface OnAccountDeletedListener
    {
        void onAccountDeleted();
    }


    public void setOnReloadMapListener(OnReloadMapListener onReloadMapListener)
    {
        mOnReloadMapListener = onReloadMapListener;
    }


    public interface OnReloadMapListener
    {
        void onReloadMap();
    }


    protected class SyncReceiver
            extends BroadcastReceiver
    {
        @Override
        public void onReceive(
                Context context,
                Intent intent)
        {
            if (isRanAsSyncService()) {
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
                default:
                    break;
            }
        }
    }
}
