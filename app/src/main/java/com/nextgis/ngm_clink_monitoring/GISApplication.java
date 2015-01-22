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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.preference.PreferenceManager;
import com.nextgis.maplib.api.IGISApplication;
import com.nextgis.maplib.location.GpsEventSource;
import com.nextgis.maplib.map.MapDrawable;
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI;
import com.nextgis.ngm_clink_monitoring.map.FoclLayerFactory;
import com.nextgis.ngm_clink_monitoring.map.FoclProject;

import java.io.File;

import static com.nextgis.maplib.util.Constants.MAP_EXT;
import static com.nextgis.maplib.util.GeoConstants.TMSTYPE_OSM;
import static com.nextgis.maplib.util.SettingsConstants.KEY_PREF_MAP;
import static com.nextgis.maplib.util.SettingsConstants.KEY_PREF_MAP_PATH;
import static com.nextgis.ngm_clink_monitoring.util.SettingsConstants.*;


public class GISApplication
        extends Application
        implements IGISApplication
{
    protected MapDrawable    mMap;
    protected GpsEventSource mGpsEventSource;

    protected Location mCurrentLocation = null;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mGpsEventSource = new GpsEventSource(this);

        getMap();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean(KEY_PREF_APP_FIRST_RUN, true)) {
            onFirstRun();
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(KEY_PREF_APP_FIRST_RUN, false);
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

        getFoclProject();*/
    }


    protected void onFirstRun()
    {
        //add OpenStreetMap layer on application first run
        String layerName = getString(R.string.osm);
        String layerURL = getString(R.string.osm_url);
        RemoteTMSLayerUI layer =
                new RemoteTMSLayerUI(getApplicationContext(), mMap.cretateLayerStorage());
        layer.setName(layerName);
        layer.setURL(layerURL);
        layer.setTMSType(TMSTYPE_OSM);
        layer.setVisible(true);

        mMap.addLayer(layer);
        mMap.save();
    }


    public MapDrawable getMap()
    {
        if (null != mMap) {
            return mMap;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File defaultPath = getExternalFilesDir(KEY_PREF_MAP);

        if (defaultPath != null) {
            String mapPath = sharedPreferences.getString(KEY_PREF_MAP_PATH, defaultPath.getPath());
            String mapName = sharedPreferences.getString(KEY_PREF_MAP_NAME, "default");

            File mapFullPath = new File(mapPath, mapName + MAP_EXT);

            final Bitmap bkBitmap = BitmapFactory.decodeResource(getResources(),
                                                                 com.nextgis.maplibui.R.drawable.bk_tile);
            mMap = new MapDrawable(bkBitmap, this, mapFullPath, new FoclLayerFactory(mapFullPath));
            mMap.setName(mapName);
            mMap.load();
        }

        //NOT NEED as this should loaded from map file (default.ngm or something else)
        if(mMap.getLayerCount() == 0) {
            //The map is the entry point for content provider. All data mast be in map.
            final FoclProject foclProject = new FoclProject(mMap.getContext(), mMap.getPath(),
                                                            new FoclLayerFactory(mMap.getPath()));
            foclProject.load();
            mMap.addLayer(foclProject);
        }
        return mMap;
    }


    /* NOT NEED
    public FoclProject getFoclProject()

    {
        mFoclProject = new FoclProject(mMap.getContext(), mMap.getPath(), new FoclLayerFactory(
                getMap().getPath()));
        mFoclProject.load();
        return mFoclProject;
    }


    public FoclProject getFoclProject_new()
    {
        if (null != mFoclProject) {
            return mFoclProject;
        }

        mFoclProject = new FoclProject(mMap.getContext(), mMap.getPath(), mMap.getLayerFactory());
        mFoclProject.load();

        return mFoclProject;
    }
    */

    @Override
    public String getAuthority()
    {
        return AUTHORITY;
    }


    public GpsEventSource getGpsEventSource()
    {
        return mGpsEventSource;
    }


    public Location getCurrentLocation()
    {
        return mCurrentLocation;
    }


    public void setCurrentLocation(Location currentLocation)
    {
        mCurrentLocation = currentLocation;
    }
}
