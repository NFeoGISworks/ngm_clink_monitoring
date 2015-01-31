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

import android.content.Intent;
import android.os.IBinder;
import com.nextgis.maplib.service.NGWSyncService;


public class FoclSyncService
        extends NGWSyncService
{
    // Object to use as a thread-safe lock
    private static final Object          sSyncAdapterLock = new Object();
    private static       FoclSyncAdapter foclSyncAdapter  = null;


    @Override
    public void onCreate()
    {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (foclSyncAdapter == null) {
                foclSyncAdapter = new FoclSyncAdapter(getApplicationContext(), true);
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return foclSyncAdapter.getSyncAdapterBinder();
    }
}
