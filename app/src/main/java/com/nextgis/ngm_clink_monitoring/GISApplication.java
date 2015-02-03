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

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.preference.PreferenceManager;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.api.ILayer;
import com.nextgis.maplib.datasource.ngw.SyncAdapter;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplib.util.Constants;
import com.nextgis.maplib.util.GeoConstants;
import com.nextgis.maplib.util.SettingsConstants;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.ngm_clink_monitoring.map.FoclLayerFactory;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;
import com.nextgis.ngm_clink_monitoring.util.FoclConstants;
import com.nextgis.ngm_clink_monitoring.util.FoclSettingsConstants;

import java.io.File;


public class GISApplication
        extends Application
        implements IGISApplication
{
    protected MapDrawable    mMap;
    protected GpsEventSource mGpsEventSource;
    protected SyncReceiver   mSyncReceiver;

    protected Location mCurrentLocation = null;


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
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(FoclSettingsConstants.KEY_PREF_APP_FIRST_RUN, false);
            edit.commit();
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
        registerReceiver(mSyncReceiver, intentFilter);
    }


    public MapDrawable getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(SettingsConstants.KEY_PREF_MAP);

        if (defaultPath != null) {
            String mapPath = sharedPreferences.getString(
                    SettingsConstants.KEY_PREF_MAP_PATH, defaultPath.getPath());
            String mapName =
                    sharedPreferences.getString(FoclSettingsConstants.KEY_PREF_MAP_NAME, "default");

            File mapFullPath = new File(mapPath, mapName + Constants.MAP_EXT);

            final Bitmap bkBitmap = BitmapFactory.decodeResource(
                    getResources(), com.nextgis.maplibui.R.drawable.bk_tile);
            mMap = new MapDrawable(bkBitmap, this, mapFullPath, new FoclLayerFactory(mapFullPath));
            mMap.setName(mapName);
            mMap.load();
        }

        return mMap;
    }


    public void reloadMap()
    {
        mMap.load();
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

        File foclPath = mMap.createLayerStorage();
        FoclProject foclProject =
                new FoclProject(mMap.getContext(), foclPath, new FoclLayerFactory(foclPath));
        foclProject.setName("FOCL");
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
                return (FoclProject) layer;
            }
        }

        return null;
    }


    public boolean isLoadedFoclProject()
    {
        FoclProject foclProject = getFoclProject();
        return null != foclProject && foclProject.getLayerCount() > 0;
    }


    @Override
    public String getAuthority()
    {
        return FoclSettingsConstants.AUTHORITY;
    }


    public GpsEventSource getGpsEventSource()
    {
        return mGpsEventSource;
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


    protected class SyncReceiver
            extends BroadcastReceiver
    {
        @Override
        public void onReceive(
                Context context,
                Intent intent)
        {
            if (intent.getAction().equals(SyncAdapter.SYNC_FINISH)) {
                reloadMap();
            }
        }
    }
}
