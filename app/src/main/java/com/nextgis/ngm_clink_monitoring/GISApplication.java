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
import android.location.Location;
import com.nextgis.maplib.location.GpsEventSource;


public class GISApplication
        extends Application
{
    protected GpsEventSource mGpsEventSource;

    protected Location mCurrentLocation = null;


    @Override
    public void onCreate()
    {
        super.onCreate();

        mGpsEventSource = new GpsEventSource(this);
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
