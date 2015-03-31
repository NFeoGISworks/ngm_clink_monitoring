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

package com.nextgis.ngm_clink_monitoring.util;

import android.os.Handler;
import android.os.Looper;


/**
 * A class used to perform periodical updates, specified inside a runnable object.
 */
public class UIUpdater
{
    // Create a Handler that uses the Main Looper to run in
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private Runnable mStatusChecker;
    private long     mUpdateInterval;


    /**
     * Creates an UIUpdater object, that can be used to perform UIUpdates on a specified time
     * intervalMillisec.
     *
     * @param runnable
     *         A runnable containing the update routine.
     * @param intervalMillisec
     *         The intervalMillisec over which the routine should run (milliseconds).
     */
    public UIUpdater(
            final Runnable runnable,
            final long intervalMillisec)
    {
        mUpdateInterval = intervalMillisec;
        mStatusChecker = new Runnable()
        {
            @Override
            public void run()
            {
                // Run the passed runnable
                runnable.run();
                // Re-run it after the update intervalMillisec
                mHandler.postDelayed(this, mUpdateInterval);
            }
        };
    }


    public void setUpdateIntervalMillisec(long millisec)
    {
        mUpdateInterval = millisec;
    }


    public void refreshUpdaterQueue()
    {
        mHandler.removeCallbacks(mStatusChecker);
        mHandler.postDelayed(mStatusChecker, mUpdateInterval);
    }


    /**
     * Starts the periodical update routine (mStatusChecker adds the callback to the handler).
     */
    public synchronized void startUpdates()
    {
        mStatusChecker.run();
    }


    /**
     * Stops the periodical update routine from running, by removing the callback.
     */
    public synchronized void stopUpdates()
    {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
