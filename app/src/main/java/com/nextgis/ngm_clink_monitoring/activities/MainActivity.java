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
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.content.SyncStatusObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.GeoMultiPoint;
import com.nextgis.maplib.datasource.GeoPoint;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.LayerGroup;
import com.nextgis.maplib.map.MapBase;
import com.nextgis.maplib.map.NGWVectorLayer;
import com.nextgis.maplib.map.VectorLayer;
import com.nextgis.maplib.util.AccountUtil;
import com.nextgis.maplib.util.Constants;
import com.nextgis.ngm_clink_monitoring.GISApplication;
import com.nextgis.ngm_clink_monitoring.R;
import com.nextgis.ngm_clink_monitoring.dialogs.SetLineStatusDialog;
import com.nextgis.ngm_clink_monitoring.dialogs.YesNoDialog;
import com.nextgis.ngm_clink_monitoring.fragments.LineListFragment;
import com.nextgis.ngm_clink_monitoring.fragments.MapFragment;
import com.nextgis.ngm_clink_monitoring.fragments.Perform1stSyncFragment;
import com.nextgis.ngm_clink_monitoring.fragments.SyncLoginFragment;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclLocationUtil;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstantsUI;
import com.nextgis.ngm_clink_monitoring.util.SntpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.nextgis.maplib.util.Constants.FIELD_GEOM;
import static com.nextgis.maplib.util.Constants.TAG;
import static com.nextgis.maplib.util.GeoConstants.CRS_WEB_MERCATOR;
import static com.nextgis.maplib.util.GeoConstants.CRS_WGS84;


public class MainActivity
        extends AppCompatActivity
        implements GISApplication.OnReloadMapListener, GISApplication.OnAccountAddedListener,
                   GISApplication.OnAccountDeletedListener, GpsEventListener

{
    protected static final int VIEW_STATE_UNKNOWN  = 0;
    protected static final int VIEW_STATE_LOGIN    = 1;
    protected static final int VIEW_STATE_1ST_SYNC = 2;
    protected static final int VIEW_STATE_OBJECTS  = 3;
    protected static final int VIEW_STATE_MAP      = 4;

    protected SyncStatusObserver mSyncStatusObserver;
    protected Object             mSyncHandle;
    protected GpsEventSource     mGpsEventSource;

    protected Long mClockOffset   = null;
    protected Long mGpsTimeOffset = null;

    protected int     mViewState = VIEW_STATE_LOGIN;
    protected boolean mIsSyncing = false;

    protected Toolbar   mMainToolbar;
    protected Toolbar   mBottomToolbar;
    protected TextView  mCustomToolbarTitle;
    protected TextView  mCustomToolbarButton;
    protected ImageView mCustomToolbarImage;
    // TODO: remove it
//    protected StatusBarFragment mStatusBarFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final GISApplication app = (GISApplication) getApplication();

        mGpsEventSource = app.getGpsEventSource();
        if (null != mGpsEventSource) {
            mGpsEventSource.addListener(this);
        }

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

        mMainToolbar = (Toolbar) findViewById(R.id.main_toolbar_cl);
        mMainToolbar.setTitle(""); // needed for screen rotation
        mMainToolbar.getBackground().setAlpha(255);
        setSupportActionBar(mMainToolbar);

        mBottomToolbar = (Toolbar) findViewById(R.id.bottom_toolbar_cl);
        mBottomToolbar.setContentInsetsAbsolute(0, 0);
        mBottomToolbar.getBackground().setAlpha(255);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Inflate a custom action bar that contains the "done" button
            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.editor_custom_action_bar, null);

            mCustomToolbarTitle =
                    (TextView) customActionBarView.findViewById(R.id.custom_toolbar_title);
            mCustomToolbarButton =
                    (TextView) customActionBarView.findViewById(R.id.custom_toolbar_button);
            mCustomToolbarImage =
                    (ImageView) customActionBarView.findViewById(R.id.custom_toolbar_image);

            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(customActionBarView);
        }


        // TODO: remove it
//        FragmentManager fm = getSupportFragmentManager();
//        mStatusBarFragment =
//                (StatusBarFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_STATUS_BAR);
//        if (null == mStatusBarFragment) {
//            mStatusBarFragment = new StatusBarFragment();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.replace(
//                    R.id.status_bar_fragment, mStatusBarFragment,
//                    FoclConstants.FRAGMENT_STATUS_BAR);
//            ft.hide(mStatusBarFragment);
//            ft.commit();
//        }

        if (!app.hasAccount()) {
            mViewState = VIEW_STATE_LOGIN;
        } else if (null == app.getFoclProject()) {
            mViewState = VIEW_STATE_1ST_SYNC;
        } else {
            mViewState = VIEW_STATE_OBJECTS;
        }

        setActivityView();

        // workaround for YesNoDialog destroying by the screen rotation
        FragmentManager fm = getSupportFragmentManager();
        Fragment fr =
                fm.findFragmentByTag(FoclConstants.FRAGMENT_YES_NO_DIALOG + "CancelObjectCreating");
        if (null != fr) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(fr);
            ft.commit();
        }
    }


    public Toolbar getBottomToolbar()
    {
        return mBottomToolbar;
    }


    @Override
    protected void onDestroy()
    {
        if (null != mGpsEventSource) {
            mGpsEventSource.removeListener(this);
        }
        super.onDestroy();
    }


    protected void setActivityView()
    {
        GISApplication app = (GISApplication) getApplication();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        switch (mViewState) {
            case VIEW_STATE_LOGIN:
                SyncLoginFragment syncLoginFragment =
                        (SyncLoginFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_SYNC_LOGIN);

                if (null == syncLoginFragment) {
                    syncLoginFragment = new SyncLoginFragment();
                    syncLoginFragment.setOnAddAccountListener(app);
                    ft.replace(
                            R.id.main_fragment, syncLoginFragment,
                            FoclConstants.FRAGMENT_SYNC_LOGIN);
                }

                break;

            case VIEW_STATE_1ST_SYNC:
                Perform1stSyncFragment perform1stSyncFragment =
                        (Perform1stSyncFragment) fm.findFragmentByTag(
                                FoclConstants.FRAGMENT_PERFORM_1ST_SYNC);

                if (null == perform1stSyncFragment) {
                    perform1stSyncFragment = new Perform1stSyncFragment();
                    ft.replace(
                            R.id.main_fragment, perform1stSyncFragment,
                            FoclConstants.FRAGMENT_PERFORM_1ST_SYNC);
                }

                break;

            case VIEW_STATE_OBJECTS:
                LineListFragment lineListFragment =
                        (LineListFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_LINE_LIST);

                if (lineListFragment == null) {
                    lineListFragment = new LineListFragment();
                    ft.replace(
                            R.id.main_fragment, lineListFragment, FoclConstants.FRAGMENT_LINE_LIST);
                }

                break;

            case VIEW_STATE_MAP:
                String tag = getMainFragmentTag();

                if (TextUtils.isEmpty(tag)) {
                    tag = "";
                }

                final FoclProject foclProject = app.getFoclProject();

                switch (tag) {
                    case FoclConstants.FRAGMENT_SYNC_LOGIN:
                    case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
                    case FoclConstants.FRAGMENT_LINE_LIST:
                    case FoclConstants.FRAGMENT_OBJECT_LIST:
                    case FoclConstants.FRAGMENT_OBJECT_STATUS:
                    case FoclConstants.FRAGMENT_MAP:
                    default:
                        foclProject.setVisible(true);
                        break;

                    case FoclConstants.FRAGMENT_OBJECT_TYPES:
                    case FoclConstants.FRAGMENT_CREATE_OBJECT:
                        if (null != app.getSelectedFoclStruct()) {
                            foclProject.setVisible(false);
                            app.getSelectedFoclStruct().setVisible(true);
                        }
                        break;
                }

                MapFragment mapFragment =
                        (MapFragment) fm.findFragmentByTag(FoclConstants.FRAGMENT_MAP);

                if (mapFragment == null) {
                    mapFragment = new MapFragment();

                    ft.replace(R.id.main_fragment, mapFragment, FoclConstants.FRAGMENT_MAP);
                    ft.addToBackStack(null);
                }
                break;
        }

        ft.commit();
        switchMenuView();
    }


    protected String getMainFragmentTag()
    {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentById(R.id.main_fragment).getTag();
    }


    protected int getActivityViewStateByMainFragment()
    {
        String tag = getMainFragmentTag();

        if (TextUtils.isEmpty(tag)) {
            return VIEW_STATE_UNKNOWN;
        }

        switch (tag) {
            case FoclConstants.FRAGMENT_SYNC_LOGIN:
                return VIEW_STATE_LOGIN;

            case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
                return VIEW_STATE_1ST_SYNC;

            case FoclConstants.FRAGMENT_OBJECT_TYPES:
            case FoclConstants.FRAGMENT_LINE_LIST:
            case FoclConstants.FRAGMENT_OBJECT_LIST:
            case FoclConstants.FRAGMENT_OBJECT_STATUS:
            case FoclConstants.FRAGMENT_CREATE_OBJECT:
                return VIEW_STATE_OBJECTS;

            case FoclConstants.FRAGMENT_MAP:
                return VIEW_STATE_MAP;

            default:
                return VIEW_STATE_UNKNOWN;
        }
    }


    public void setBarsView(String toolbarTitle)
    {
        switchMenuView();

        // TODO: remove commented
        if (null == mMainToolbar /*|| null == mStatusBarFragment*/) {
            return;
        }

        String tag = getMainFragmentTag();

        if (TextUtils.isEmpty(tag)) {
            tag = "";
        }


        ActionBar actionBar = getSupportActionBar();

        switch (tag) {
            case FoclConstants.FRAGMENT_SYNC_LOGIN:
            case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
            case FoclConstants.FRAGMENT_LINE_LIST:
            case FoclConstants.FRAGMENT_OBJECT_TYPES:
            case FoclConstants.FRAGMENT_OBJECT_LIST:
            case FoclConstants.FRAGMENT_OBJECT_STATUS:
            default:
                if (actionBar != null) {
                    // We want the UP affordance but no app icon.
                    // Setting HOME_AS_UP, SHOW_TITLE and clearing SHOW_HOME does the trick.
                    actionBar.setDisplayOptions(
                            ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                            ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE |
                                    ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_CUSTOM);
                    mMainToolbar.setTitle(toolbarTitle == null ? getTitle() : toolbarTitle);
                }
                break;

            case FoclConstants.FRAGMENT_CREATE_OBJECT:
                if (actionBar != null) {
                    mCustomToolbarTitle.setText(toolbarTitle == null ? getTitle() : toolbarTitle);
                    setCustomToolbarButtonView(
                            actionBar, R.string.menu_done, R.drawable.ic_action_apply);
                }
                break;

            case FoclConstants.FRAGMENT_MAP:
                if (actionBar != null) {
                    mCustomToolbarTitle.setText(toolbarTitle == null ? getTitle() : toolbarTitle);
                    setCustomToolbarButtonView(
                            actionBar, R.string.menu_back, R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                }
                break;
        }


        switch (tag) {
            case FoclConstants.FRAGMENT_SYNC_LOGIN:
            case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
            case FoclConstants.FRAGMENT_LINE_LIST:
            case FoclConstants.FRAGMENT_OBJECT_STATUS:
            case FoclConstants.FRAGMENT_CREATE_OBJECT:
            case FoclConstants.FRAGMENT_MAP:
            default:
                mMainToolbar.setNavigationIcon(null);
                break;

            case FoclConstants.FRAGMENT_OBJECT_TYPES:
            case FoclConstants.FRAGMENT_OBJECT_LIST:
                mMainToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                break;
        }


        switch (tag) {
            case FoclConstants.FRAGMENT_SYNC_LOGIN:
            case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
            case FoclConstants.FRAGMENT_LINE_LIST:
            case FoclConstants.FRAGMENT_OBJECT_TYPES:
            case FoclConstants.FRAGMENT_OBJECT_LIST:
            case FoclConstants.FRAGMENT_OBJECT_STATUS:
            case FoclConstants.FRAGMENT_MAP:
            default:
                mBottomToolbar.setVisibility(View.GONE);
                break;

            case FoclConstants.FRAGMENT_CREATE_OBJECT:
                mBottomToolbar.setVisibility(View.VISIBLE);
                break;
        }


        // TODO: remove it
//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//
//        switch (tag) {
//            case FoclConstants.FRAGMENT_SYNC_LOGIN:
//            case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
//            case FoclConstants.FRAGMENT_LINE_LIST:
//            case FoclConstants.FRAGMENT_OBJECT_TYPES:
//            case FoclConstants.FRAGMENT_OBJECT_LIST:
//            case FoclConstants.FRAGMENT_OBJECT_STATUS:
//            case FoclConstants.FRAGMENT_CREATE_OBJECT:
//            case FoclConstants.FRAGMENT_MAP:
//            default:
//                ft.hide(mStatusBarFragment);
//                break;
//
////                ft.show(mStatusBarFragment);
////                break;
//        }
//
//        ft.commit();
    }


    protected void setCustomToolbarButtonView(
            ActionBar actionBar,
            int textResId,
            int iconResId)
    {
        // Show the custom action bar but hide the home icon and title
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME |
                        ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE |
                        ActionBar.DISPLAY_USE_LOGO);
        mCustomToolbarButton.setText(textResId);
        mCustomToolbarImage.setImageResource(iconResId);
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

        if (!FoclLocationUtil.isOnlyGpsLocationModeEnabled(this)) {
            FoclLocationUtil.showSettingsLocationAlert(this);
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
            case VIEW_STATE_LOGIN:
                menu.findItem(R.id.menu_full_sync).setEnabled(false);
                menu.findItem(R.id.menu_sync).setEnabled(false);
                break;

            case VIEW_STATE_OBJECTS:
                String tag = getMainFragmentTag();
                switch (tag) {
                    case FoclConstants.FRAGMENT_SYNC_LOGIN:
                    case FoclConstants.FRAGMENT_PERFORM_1ST_SYNC:
                    case FoclConstants.FRAGMENT_LINE_LIST:
                    case FoclConstants.FRAGMENT_OBJECT_LIST:
                    case FoclConstants.FRAGMENT_OBJECT_STATUS:
                    case FoclConstants.FRAGMENT_MAP:
                    default:
                        break;

                    case FoclConstants.FRAGMENT_OBJECT_TYPES:
                        menu.findItem(R.id.menu_line_status).setVisible(true);
                        break;

                    case FoclConstants.FRAGMENT_CREATE_OBJECT:
                        MenuItemCompat.setShowAsAction(
                                menu.findItem(R.id.menu_map), MenuItem.SHOW_AS_ACTION_ALWAYS);
                        break;
                }
                break;

            case VIEW_STATE_MAP:
                menu.findItem(R.id.menu_map).setVisible(false);
                menu.findItem(R.id.menu_location).setVisible(true);
                menu.findItem(R.id.menu_refresh_map).setVisible(true);
                break;
        }

        if (mIsSyncing) {
            menu.findItem(R.id.menu_full_sync).setEnabled(false);
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
                onBackPressed();
                return true;

            case R.id.menu_location:
                onMenuLocationClick();
                return true;

            case R.id.menu_refresh_map:
                onMenuRefreshMapClick();
                return true;

            case R.id.menu_map:
                onMenuMapClick();
                return true;

            case R.id.menu_full_sync:
                onMenuSyncClick(true);
                return true;

            case R.id.menu_sync:
                onMenuSyncClick(false);
                return true;

/*
            case R.id.menu_reports:
                onMenuReportsClick();
                return true;
*/

            case R.id.menu_send_work_data:
                onMenuSendWorkDataClick();
                return true;

            case R.id.menu_line_status:
                onMenuLineStatusClick();
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


    @Override
    public void onBackPressed()
    {
        String tag = getMainFragmentTag();

        if (TextUtils.isEmpty(tag)) {
            tag = "";
        }

        if (tag.equals(FoclConstants.FRAGMENT_CREATE_OBJECT)) {
            YesNoDialog yesNoDialog = new YesNoDialog();
            yesNoDialog.setIcon(R.drawable.ic_action_warning)
                    .setTitle(R.string.confirmation)
                    .setMessage(R.string.confirm_cancel_object_creating)
                    .setPositiveText(R.string.yes)
                    .setNegativeText(R.string.no)
                    .setOnPositiveClickedListener(
                            new YesNoDialog.OnPositiveClickedListener()
                            {
                                @Override
                                public void onPositiveClicked()
                                {
                                    getSupportFragmentManager().popBackStackImmediate();
                                    mViewState = getActivityViewStateByMainFragment();
                                    switchMenuView();
                                }
                            })
                    .setOnNegativeClickedListener(
                            new YesNoDialog.OnNegativeClickedListener()
                            {
                                @Override
                                public void onNegativeClicked()
                                {
                                    // cancel
                                }
                            })
                    .show(
                            getSupportFragmentManager(),
                            FoclConstants.FRAGMENT_YES_NO_DIALOG + "CancelObjectCreating");

            return;
        }

        super.onBackPressed();
        mViewState = getActivityViewStateByMainFragment();
        switchMenuView();
    }


    public void onMenuLocationClick()
    {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FoclConstants.FRAGMENT_MAP);

        if (null != fragment) {
            ((MapFragment) fragment).locateCurrentPositionAndZoom();
        }
    }


    public void onMenuRefreshMapClick()
    {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FoclConstants.FRAGMENT_MAP);

        if (null != fragment) {
            ((MapFragment) fragment).refresh();
        }
    }


    public void onMenuMapClick()
    {
        mViewState = VIEW_STATE_MAP;
        setActivityView();
    }


    public void onMenuSyncClick(boolean isFullSync)
    {
        GISApplication app = (GISApplication) getApplication();

        if (!app.isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.no_network_no_sync), Toast.LENGTH_LONG).show();
            return;
        }

        if (!app.hasAccount()) {
            Toast.makeText(this, getString(R.string.no_account), Toast.LENGTH_LONG).show();
            return;
        }

        app.runSyncManually(isFullSync);
    }


    public void onMenuReportsClick()
    {
        GISApplication app = (GISApplication) getApplication();
        try {
            app.sendReportsOnServer(true, false);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void onMenuSendWorkDataClick()
    {
        GISApplication app = (GISApplication) getApplication();
        try {
            app.sendReportsOnServer(true, true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void onMenuLineStatusClick()
    {
        SetLineStatusDialog lineStatusDialog = new SetLineStatusDialog();
        lineStatusDialog.setKeepInstance(true)
                .show(getSupportFragmentManager(), FoclConstants.FRAGMENT_SET_LINE_STATUS_DIALOG);
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


    @Override
    public void onLocationChanged(Location location)
    {
        if (null == mClockOffset) {
            new GetNtpAsyncTask().execute();
        }

        if (null == mGpsTimeOffset && null != mClockOffset) {
            GISApplication app = (GISApplication) getApplication();

            long currTime = System.currentTimeMillis();
            long gpsLocalTimeOffset = location.getTime() - currTime;
            mGpsTimeOffset = mClockOffset - gpsLocalTimeOffset;
            app.setGpsTimeOffset(mGpsTimeOffset);

            Log.d(Constants.TAG, "GpsTimeOffset: " + mGpsTimeOffset);
            Log.d(Constants.TAG, "GpsTime: " + location.getTime());
            Log.d(Constants.TAG, "gpsLocalTimeOffset: " + gpsLocalTimeOffset);
            Log.d(Constants.TAG, "currTime: " + currTime);
        }
    }


    @Override
    public void onBestLocationChanged(Location location)
    {

    }


    @Override
    public void onGpsStatusChanged(int event)
    {

    }


    class GetNtpAsyncTask
            extends AsyncTask<String, Void, SntpClient>
    {
        @Override
        protected SntpClient doInBackground(String... params)
        {
            GISApplication app = (GISApplication) getApplication();

            if (app.isNetworkAvailable()) {
                SntpClient sntpClient = new SntpClient();

                if (sntpClient.requestTime(
                        FoclConstants.FOCL_NTP_URL, FoclConstants.FOCL_NTP_TIMEOUT_MILLIS)) {

                    return sntpClient;
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(SntpClient sntpClient)
        {
            if (null != sntpClient) {
                mClockOffset = sntpClient.getClockOffset();
                Log.d(Constants.TAG, "ClockOffset: " + mClockOffset);
                Log.d(Constants.TAG, "currentTimeMillis: " + System.currentTimeMillis());
                Log.d(Constants.TAG, "NtpTime: " + sntpClient.getNtpTime());
            }
        }
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
                values.put(FIELD_GEOM, mpt.toBlob());
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
                values.put(FIELD_GEOM, mpt.toBlob());
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
