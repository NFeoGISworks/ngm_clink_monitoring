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

import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nextgis.maplib.api.GpsEventListener;
import com.nextgis.ngm_clink_monitoring.util.LocationUtil;
import com.nextgis.ngm_clink_monitoring.util.SettingsConstants;

import java.text.DecimalFormat;


public class StatusBarFragment
        extends Fragment
        implements GpsEventListener
{
    protected StatusBarTextView mStatusLine;
    protected TextView          mLatView;
    protected TextView          mLongView;
    protected TextView          mAltView;
    protected TextView          mAccView;

    protected String mStatusLineText;
    protected String mLatText;
    protected String mLongText;
    protected String mAltText;
    protected String mAccText;

    protected long mLastLocationMillis;

    protected boolean mIsGPSFix = false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mStatusLineText = getString(R.string.coordinates_not_defined);
        setLocationDefaultText();
    }


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_status_bar, null);

        mStatusLine = (StatusBarTextView) view.findViewById(R.id.status_line);
        mLatView = (TextView) view.findViewById(R.id.latitude_view);
        mLongView = (TextView) view.findViewById(R.id.longitude_view);
        mAltView = (TextView) view.findViewById(R.id.altitude_view);
        mAccView = (TextView) view.findViewById(R.id.accuracy_view);

        mStatusLine.setText(mStatusLineText);
        setLocationViewsText();

        return view;
    }


    protected void setLocationDefaultText()
    {
        mLatText = getString(R.string.latitude_caption) + " --";
        mLongText = getString(R.string.longitude_caption) + " --";
        mAltText = getString(R.string.altitude_caption) + " --";
        mAccText = getString(R.string.accuracy_caption) + " --";
    }


    protected void setLocationViewsText()
    {
        mLatView.setText(mLatText);
        mLongView.setText(mLongText);
        mAltView.setText(mAltText);
        mAccView.setText(mAccText);
    }


    @Override
    public void onLocationChanged(Location location)
    {
        if (location == null) {
            return;
        }

        GISApplication app = (GISApplication) getActivity().getApplication();
        app.setCurrentLocation(location);
        mLastLocationMillis = SystemClock.elapsedRealtime();

        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        int nFormat = prefs.getInt(
                SettingsConstants.KEY_PREF_COORD_FORMAT + "_int", Location.FORMAT_SECONDS);
        DecimalFormat df = new DecimalFormat("0.0");

        mLatText = getString(R.string.latitude_caption) + " " +
                   LocationUtil.formatLatitude(location.getLatitude(), nFormat, getResources()) +
                   getString(R.string.coord_lat);

        mLongText = getString(R.string.longitude_caption) + " " +
                    LocationUtil.formatLongitude(location.getLongitude(), nFormat, getResources()) +
                    getString(R.string.coord_lon);

        double altitude = location.getAltitude();
        mAltText = getString(R.string.altitude_caption) + " " + df.format(altitude) + " " +
                   getString(R.string.altitude_unit);

        float accuracy = location.getAccuracy();
        mAccText = getString(R.string.accuracy_caption) + " " + df.format(accuracy) + " " +
                   getString(R.string.accuracy_unit);

        if (isVisible()) {
            setLocationViewsText();
        }
    }


    @Override
    public void onGpsStatusChanged(int event)
    {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                GISApplication app = (GISApplication) getActivity().getApplication();

                if (app.getCurrentLocation() != null) {
                    mIsGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 10000;
                }

                if (mIsGPSFix) { // A fix has been acquired.
                    mStatusLineText = getString(R.string.coordinates_defined);

                    if (isVisible()) {
                        mStatusLine.setText(mStatusLineText);
                    }

                } else { // The fix has been lost.
                    mStatusLineText = getString(R.string.coordinates_not_defined);
                    setLocationDefaultText();

                    if (isVisible()) {
                        mStatusLine.setText(mStatusLineText);
                        setLocationViewsText();
                    }
                }

                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
                mIsGPSFix = true;
                mStatusLineText = getString(R.string.coordinates_defined);

                if (isVisible()) {
                    mStatusLine.setText(mStatusLineText);
                }

                break;
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();

        GISApplication app = (GISApplication) getActivity().getApplication();
        app.getGpsEventSource().addListener(this);
        app.getMap().addListener(mStatusLine);

        mStatusLine.setText(mStatusLineText);
        setLocationViewsText();
    }


    @Override
    public void onStop()
    {
        GISApplication app = (GISApplication) getActivity().getApplication();
        app.getGpsEventSource().removeListener(this);
        app.getMap().removeListener(mStatusLine);

        super.onStop();
    }
}
